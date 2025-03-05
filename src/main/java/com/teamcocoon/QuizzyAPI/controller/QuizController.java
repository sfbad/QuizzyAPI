package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import jakarta.validation.Valid;
import com.teamcocoon.QuizzyAPI.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    @GetMapping()
    public ResponseEntity<ListQuizResponseDto> getListQuiz(@AuthenticationPrincipal Jwt jwt){
        String uid = jwt.getClaim("sub");
        return quizService.getListQuizByUserId(uid);
    }

    @PostMapping()
    public ResponseEntity<Void> createQuiz(@RequestBody Quiz quiz, @AuthenticationPrincipal Jwt jwt){
        String uid = jwt.getClaim("sub");

        User user = userService.getUserByUID(uid);

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

    /*@GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getQuizById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        String uid = jwt.getClaim("sub");
        System.out.println("getQuizById : " + id);
        return quizService.getQuizByIdAndUserId(id, uid);
    }*/

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuizTitle(
            @PathVariable Long id,
            @RequestBody List<PatchQuizTitleRequestDTO> patchRequests,
            @AuthenticationPrincipal Jwt jwt) {

        String uid = jwt.getClaim("sub");

        quizService.updateQuizTitle(id, patchRequests, uid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<?> addNewQuestion( @AuthenticationPrincipal Jwt jwt, @PathVariable Long id,@Valid @RequestBody AddNewQuestionDTO question){
        System.out.println("addNewQuestion : " + question);
        Long questionId = quizService.addQuestionToQuiz(id, question);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{questionId}")
                .buildAndExpand(questionId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        log.info("location : " + location);
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListQuestionsDto> getQuizById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        String uid = jwt.getClaim("sub");
        System.out.println("getQuizById : " + id);
        return quizService.getQuizByIdAndUserId(id, uid);
    }
    @PutMapping("/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuestion(@PathVariable Long quizId, @PathVariable Long questionId,
                               @RequestBody @Valid AddNewQuestionDTO updateQuestionDTO) {

        String newTitle = updateQuestionDTO.title();
        quizService.updateQuestion(quizId,questionId,newTitle,updateQuestionDTO.answers());
    }

}
