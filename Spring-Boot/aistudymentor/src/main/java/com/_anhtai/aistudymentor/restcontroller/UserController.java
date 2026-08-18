package com._anhtai.aistudymentor.restcontroller;

import com._anhtai.aistudymentor.dto.reponse.UserDTO;
import com._anhtai.aistudymentor.dto.request.EduLevelDTO;
import com._anhtai.aistudymentor.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/profile")
    public UserDTO getUser(Principal principal) {
        return userService.getUserByEmail(principal.getName());
    }
    @PostMapping("/profile/edit")
    public UserDTO updateUser(Principal principal, EduLevelDTO eduLevelDTO) {
        return userService.updateUserByEmail(principal.getName(), eduLevelDTO);
    }

}
