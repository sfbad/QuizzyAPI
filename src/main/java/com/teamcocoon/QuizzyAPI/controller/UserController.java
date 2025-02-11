package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<Void> registerUser(
            @AuthenticationPrincipal Jwt jwt,
           @Valid @RequestBody UserRequestDto userRequestDto
            ) {
        String uid = jwt.getClaim("sub");
        String username = userRequestDto.username();
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        userService.registerUser(uid, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUserData(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserData(jwt));
    }
}
