package com._anhtai.aistudymentor.service;

import com._anhtai.aistudymentor.dto.reponse.UserDTO;
import com._anhtai.aistudymentor.dto.request.EduLevelDTO;
import com._anhtai.aistudymentor.entity.User;
import com._anhtai.aistudymentor.exception.EmailIsPresentException;
import com._anhtai.aistudymentor.repositoy.UserDetailsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsRepository userDetailsRepository;
    public UserService(PasswordEncoder passwordEncoder,UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public void save(UserDTO user) {
        if(userDetailsRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailIsPresentException("User already exists");
        }
        User newUser = User.builder()
                .email(user.getEmail())
                .educationLevel(user.getEducationLevel())
                .passwordHash(passwordEncoder.encode(user.getPassword()))
                .totalXp(0)
                .createdAt(LocalDateTime.now())
                .role("STUDENT")
                .build();
        userDetailsRepository.save(newUser);

    }
    public UserDTO getUserByEmail(String email) {
        User user = userDetailsRepository.findByEmail(email).orElse(null);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setEducationLevel(user.getEducationLevel());
        userDTO.setPassword(null);
        return userDTO;
    }
    public UserDTO updateUserByEmail(String email, EduLevelDTO eduLevelDTO) {
        User user = userDetailsRepository.findByEmail(email).orElse(null);
        if(user == null) {
            throw new RuntimeException("User not found");
        }
        user.setEducationLevel(eduLevelDTO.getEduLevel());
        userDetailsRepository.save(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setEducationLevel(user.getEducationLevel());
        userDTO.setPassword(null);
        return userDTO;
    }
}
