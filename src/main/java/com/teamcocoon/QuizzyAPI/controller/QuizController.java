package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.ListQuizResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.PatchQuizTitleRequestDTO;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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

    @PostMapping()
    public ResponseEntity<Void> createQuiz(@RequestBody Quiz quiz, @AuthenticationPrincipal Jwt jwt){
        String uid = jwt.getClaim("sub");

        User user = new User();
        user.setUserId(uid);

        quiz.setUser(user);

        Quiz savedQuiz = quizService.saveQuiz(quiz);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedQuiz.getQuizId())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuizTitle(
            @PathVariable Long id,
            @RequestBody List<PatchQuizTitleRequestDTO> patchRequests,
            @AuthenticationPrincipal Jwt jwtl) {

        String username = jwtl.getClaim("sub");

        quizService.updateQuizTitle(id, patchRequests, username);
        return ResponseEntity.noContent().build();
    }

}
