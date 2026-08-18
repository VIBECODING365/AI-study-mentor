package com._anhtai.aistudymentor.service;

import java.util.List;

import com._anhtai.aistudymentor.dto.reponse.UserDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com._anhtai.aistudymentor.dao.UserDAO;
import com._anhtai.aistudymentor.dto.reponse.AnswerDTO;
import com._anhtai.aistudymentor.dto.reponse.QuestionHistory;
import com._anhtai.aistudymentor.dto.request.AskDTO;
import com._anhtai.aistudymentor.dto.reponse.QuizDTO;
import com._anhtai.aistudymentor.entity.AIAnswer;
import com._anhtai.aistudymentor.entity.Question;
import com._anhtai.aistudymentor.entity.Subject;
import com._anhtai.aistudymentor.entity.User;
import com._anhtai.aistudymentor.repositoy.QuestionRepository;
import com._anhtai.aistudymentor.repositoy.SubjectRepository;
import com._anhtai.aistudymentor.repositoy.UserDetailsRepository;

import jakarta.transaction.Transactional;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final UserDetailsRepository userDetailsRepository;
    private final UserDAO userDAO;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    public ChatService(SubjectRepository subjectRepository, QuestionRepository questionRepository,
                       UserDAO userDAO, ChatClient.Builder chatClient, UserDetailsRepository userDetailsRepository) {
        this.chatClient = chatClient.build();
        this.userDetailsRepository = userDetailsRepository;
        this.userDAO = userDAO;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }
    @Transactional
    public AnswerDTO chat(AskDTO askDTO, MultipartFile file, String email) {
        if (askDTO == null) {
            throw new RuntimeException("Messege not found");
        }

        String userText = String.format("\"Câu hỏi / Chủ đề cần giải đáp: %s \" về môn học: %s\"",
                askDTO.getQuestion(), askDTO.getSubject());

        String systemMessage = """
        Bạn là "AI Study Mentor" - trợ lý học tập AI thông minh, thân thiện dành cho học sinh, sinh viên (từ cấp 2, cấp 3 đến đại học).
        
        Nhiệm vụ của bạn:
        1. Phân tích câu hỏi của học sinh để xác định môn học và độ khó.
        2. Cung cấp lời giải thích chính xác, rõ ràng, chi tiết từng bước (step-by-step) bằng ngôn ngữ tiếng Việt dễ hiểu, phù hợp với trình độ học sinh.
        3. Trình bày rõ ràng các bước logic, công thức và lý thuyết liên quan đối với các bài tập tính toán / giải đề.
        4. Chỉ ra các lỗi sai thường gặp, các phương pháp giải thay thế và tóm tắt kiến thức trọng tâm.
        5. Gợi ý thêm các câu hỏi luyện tập / bài tập tương tự để học sinh củng cố kiến thức.
        6. Kiến thức không liên quan thì cứ trả lời là không hỗ trợ và không đưa ra các thông tin không liên quan.
        Luôn giữ văn phong khuyến khích, tích cực, mang tính giáo dục cao. Tuyệt đối không trả lời các nội dung không liên quan đến học tập hoặc vi phạm chuẩn mực.
        LƯU Ý: Rất quan trọng! Chuỗi JSON trả về phải hợp lệ theo chuẩn RFC 8259.
        Nếu trong nội dung có chứa dấu gạch chéo ngược '\\\\', hãy escape nó thành '\\\\\\\\'.
        Không sử dụng các escape character không hợp lệ như \\\\c, \\\\p, \\\\s.
        """;

        // 1. Gọi AI để lấy câu trả lời (Xử lý riêng trường hợp có ảnh / không ảnh)
        AnswerDTO answerDTO;

        if (file != null && !file.isEmpty()) {
            try {
                // Xác định MimeType an toàn
                String contentType = file.getContentType();
                if (contentType == null || contentType.contains("*")) {
                    contentType = "image/jpeg";
                }

                // Fix lỗi chính: Dùng org.springframework.core.io.ByteArrayResource
                // và MimeTypeUtils.parseMimeType() từ org.springframework.util
                org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };

                Media media = new Media(org.springframework.util.MimeTypeUtils.parseMimeType(contentType), resource);

                answerDTO = chatClient.prompt()
                        .user(promptUserSpec -> promptUserSpec.text(userText).media(media))
                        .system(systemMessage)
                        .call()
                        .entity(AnswerDTO.class);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Không thể đọc file ảnh gửi lên: " + e.getMessage(), e);
            }
        } else {
            answerDTO = chatClient.prompt()
                    .user(promptUserSpec -> promptUserSpec.text(userText))
                    .system(systemMessage)
                    .call()
                    .entity(AnswerDTO.class);
        }

        // 2. Lưu vào Database nếu có email (User đăng nhập)
        if (email != null && !email.isBlank()) {
            User user = userDAO.findByEmail(email);
            Subject subject = subjectRepository.findBySubjectName(askDTO.getSubject());

            // Tránh NPE nếu không tìm thấy Subject
            if (subject == null) {
                System.out.println("Cảnh báo: Không tìm thấy môn học: " + askDTO.getSubject());
            }

            Question question = Question.builder()
                    .questionText(askDTO.getQuestion())
                    .askedAt(java.time.LocalDateTime.now())
                    .subject(subject)
                    .user(user)
                    .build();

            AIAnswer aiAnswer = AIAnswer.builder()
                    .primaryAnswer(answerDTO.getMainAnswer())
                    .simplifiedExplanation(answerDTO.getAdditionalInfo())
                    .question(question)
                    .build();

            question.setAiAnswer(aiAnswer);
            questionRepository.save(question); // Cascading sẽ tự save AIAnswer nếu cấu hình
        }

        return answerDTO;
    }
    public QuizDTO quizGen(String subject, String email) {
        if (subject == null) {
            throw new RuntimeException("Subject not found");
        }

        User user = userDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String eduLevel = user.getEducationLevel();

        // Sửa lại Prompt: Chuẩn hóa Schema ví dụ, bỏ ngoặc kép thừa
        String userText = String.format("""
        Bạn là "AI Study Mentor" - trợ lý học tập AI thông minh, thân thiện dành cho học sinh, sinh viên (từ cấp 2, cấp 3 đến đại học).
        Nhiệm vụ của bạn là tạo ra một đề kiểm tra trắc nghiệm gồm 10 câu hỏi về môn học %s ở cấp độ %s.
        
        YÊU CẦU ĐỊNH DẠNG VÀ HIỂN THỊ (CỰC KỲ QUAN TRỌNG):
        1. Trả về ký tự tiếng Việt và ký tự đặc biệt ở dạng văn bản UTF-8 chuẩn. Không dùng unicode escape sequence (như \\u00b0, \\u221a). Viết trực tiếp ký tự như: °, √, α, β...
        2. Tất cả công thức toán học, biểu thức phải được bọc trong cặp dấu $...$ theo cú pháp LaTeX chuẩn.
           - Ví dụ: "$y = 2x^2 + 1$", "$y = \\sqrt{3}x - 2$", "80^\\circ$"
        3. Cấu trúc JSON trả về BẮT BUỘC phải theo đúng format sau:
        {
            "questions": [
                {
                    "question": "Nội dung câu hỏi 1",
                    "options": ["A. Phương án 1", "B. Phương án 2", "C. Phương án 3", "D. Phương án 4"],
                    "answer": "A",
                    "explain": "Giải thích chi tiết tại sao chọn A"
                }
            ]
        }
        4. Output phải là một chuỗi JSON hợp lệ. Hãy double-escape các dấu gạch chéo ngược trong LaTeX (ví dụ: dùng \\\\frac thay vì \\frac).
        """, subject, eduLevel);

        String systemMessage = """
        Luôn giữ văn phong khuyến khích, tích cực, mang tính giáo dục cao.
        Tuyệt đối không trả lời các nội dung không liên quan đến học tập.
        Đảm bảo output JSON là hợp lệ, chỉ trả về chuỗi JSON thuần (không dùng markdown block ```json ... ```, không kèm văn bản giải thích).
        """;

        com._anhtai.aistudymentor.dto.reponse.QuizDTO quizDTO = chatClient.prompt()
                .system(systemMessage)
                .user(userText)
                .call()
                .entity(QuizDTO.class);
        System.out.println(quizDTO);
        return quizDTO;
    }
    public List<QuestionHistory> getQuestions(String email){
        List<Question> question = questionRepository.findAllByUserEmail(email);
        return question.stream().map(q -> {
            QuestionHistory questionHistory = new QuestionHistory();
            questionHistory.setQuestionText(q.getQuestionText());
            questionHistory.setAskedAt(q.getAskedAt());
            questionHistory.setSubject(q.getSubject().getSubjectName());
            AIAnswer aiAnswer = q.getAiAnswer();
            questionHistory.setPrimaryAnswer(aiAnswer.getPrimaryAnswer());
            questionHistory.setSimplifiedExplanation(aiAnswer.getSimplifiedExplanation());
            return questionHistory;
        }).toList();
    }
}
