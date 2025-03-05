package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import jakarta.validation.Valid;
import com.teamcocoon.QuizzyAPI.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;


    @GetMapping()
    public ResponseEntity<?> getListQuiz(@AuthenticationPrincipal Jwt jwt) {
        String uid = jwt.getClaim("sub");
        System.out.println("UID: " + uid);
        ResponseEntity<ListQuizResponseDto> response;
        response = quizService.getListQuizByUserId(uid);

        Map<String, String> links = new HashMap<>();
        links.put("create", "/api/quiz");
        ListQuizResponseLinkDto responseWithLinks = new ListQuizResponseLinkDto(response.getBody().data(), links);
        log.info("issue 12  "+ responseWithLinks);
        return ResponseEntity.ok(responseWithLinks);
    }

    @PostMapping()
    public ResponseEntity<Void> createQuiz(@RequestBody QuizDto quizDto, @AuthenticationPrincipal Jwt jwt){
        if (jwt == null) {
            throw new IllegalStateException("Jwt is null");
        }

        String uid = jwt.getClaim("sub");
        System.out.println("JWT received, user UID: " + uid);

        Quiz savedQuiz = quizService.createQuiz(quizDto, uid);

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
            @Valid @PathVariable Long id,
            @Valid @RequestBody List<PatchQuizTitleRequestDTO> patchRequests,
            @AuthenticationPrincipal Jwt jwt) {

        String uid = jwt.getClaim("sub");

        quizService.updateQuizTitle(id, patchRequests, uid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<?> addNewQuestion( @AuthenticationPrincipal Jwt jwt, @Valid @PathVariable Long id, @Valid @RequestBody AddNewQuestionDTO question){
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
    public ResponseEntity<ListQuestionsDto> getQuizById(@AuthenticationPrincipal Jwt jwt, @Valid @PathVariable Long id){
        String uid = jwt.getClaim("sub");
        System.out.println("getQuizById : " + id);
        return quizService.getQuizByIdAndUserId(id, uid);
    }
    @PutMapping("/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuestion(@Valid @PathVariable Long quizId, @Valid @PathVariable Long questionId,
                               @RequestBody @Valid AddNewQuestionDTO updateQuestionDTO) {

        String newTitle = updateQuestionDTO.title();
        quizService.updateQuestion(quizId,questionId,newTitle,updateQuestionDTO.answers());
    }

}
