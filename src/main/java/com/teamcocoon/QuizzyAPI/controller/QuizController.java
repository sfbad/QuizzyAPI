package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.ListQuizResponseDto;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping()
    public ResponseEntity<ListQuizResponseDto> getListQuiz(@AuthenticationPrincipal Jwt jwt){
        System.out.println("uid: " + jwt.getClaim("sub"));
        String uid = jwt.getClaim("sub");
        return quizService.getListQuizByUserId(uid);
    }
}
