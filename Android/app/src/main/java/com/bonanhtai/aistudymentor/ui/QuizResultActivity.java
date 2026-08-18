package com.bonanhtai.aistudymentor.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bonanhtai.aistudymentor.R;
import com.bonanhtai.aistudymentor.model.QuestionDTO;
import com.bonanhtai.aistudymentor.model.QuizDTO;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.List;

import com.bonanhtai.aistudymentor.ui.view.MathView;

public class QuizResultActivity extends AppCompatActivity {

    private TextView tvScore;
    private LinearLayout layoutResultList;
    private MaterialButton btnHome, btnRetry;

    private QuizDTO quiz;
    private String[] userAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);
        Toast.makeText(this, "QuizResultActivity onCreate started", Toast.LENGTH_SHORT).show();

        String quizJson = getIntent().getStringExtra("QUIZ_DATA");
        userAnswers = getIntent().getStringArrayExtra("USER_ANSWERS");
        Toast.makeText(this, "quizJson=" + (quizJson != null ? "not null" : "null") + ", userAnswers=" + (userAnswers != null ? userAnswers.length : "null"), Toast.LENGTH_SHORT).show();

        if (quizJson != null) {
            quiz = new Gson().fromJson(quizJson, QuizDTO.class);
        }

        initViews();
        displayResults();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        layoutResultList = findViewById(R.id.layoutResultList);
        btnHome = findViewById(R.id.btnHome);
        btnRetry = findViewById(R.id.btnRetry);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnRetry.setOnClickListener(v -> {
            startActivity(new Intent(this, PracticeActivity.class));
            finish();
        });
    }

    private void displayResults() {
        if (quiz == null || userAnswers == null) return;

        List<QuestionDTO> questions = quiz.getQuestions();
        int score = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < questions.size(); i++) {
            QuestionDTO q = questions.get(i);
            String uAns = userAnswers[i];
            
            View itemView = inflater.inflate(R.layout.item_quiz_result, layoutResultList, false);
            MathView mathTitle = itemView.findViewById(R.id.mathQuestionTitle);
            TextView tvUAns = itemView.findViewById(R.id.tvUserAnswer);
            TextView tvCAns = itemView.findViewById(R.id.tvCorrectAnswer);
            MathView mathExpl = itemView.findViewById(R.id.mathExplanation);

            mathTitle.setText((i + 1) + ". " + q.getQuestion());

            String userAnswerText = "Not answered";
            String userAnswerLetter = "";
            if (uAns != null && !uAns.isEmpty()) {
                // Extract the letter (first character) - works for both "B" and "B. Động cơ hơi nước"
                userAnswerLetter = String.valueOf(uAns.charAt(0)).trim().toUpperCase();
                
                // Try to build full display text
                int optionIndex = userAnswerLetter.charAt(0) - 'A';
                if (optionIndex >= 0 && optionIndex < q.getOptions().size()) {
                    userAnswerText = userAnswerLetter + ". " + q.getOptions().get(optionIndex);
                } else {
                    // Fallback: use original uAns
                    userAnswerText = uAns;
                }
            }
            tvUAns.setText("Your Answer: " + userAnswerText);

            tvCAns.setText("Correct Answer: " + q.getAnswer());
            mathExpl.setText(q.getExplain());

            boolean isCorrect = false;
            if (!userAnswerLetter.isEmpty()) {
                String correctAnswer = q.getAnswer().trim();
                if (i == 0) { // Only show toast for first question to avoid too many toasts
                    Toast.makeText(this, "Q1: userAnswerLetter='" + userAnswerLetter + "', correctAnswer='" + correctAnswer + "', uAns='" + uAns + "'", Toast.LENGTH_LONG).show();
                }
                isCorrect = userAnswerLetter.equals(correctAnswer);
            }

            if (isCorrect) {
                score++;
                tvUAns.setTextColor(Color.parseColor("#10B981")); // Green
            } else {
                tvUAns.setTextColor(Color.parseColor("#EF4444")); // Red
            }

            layoutResultList.addView(itemView);
        }

        tvScore.setText("Score: " + score + "/" + questions.size());
    }
}
