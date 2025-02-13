package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.Response;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuizService {

    @Autowired
    private final QuizRepository quizRepository;
    @Autowired
    private final QuestionService questionService;
    @Autowired
    private final UserRepository userRepository;

    public ResponseEntity<ListQuizResponseDto> getListQuizByUserId(String uid) {
        List<Quiz> listQuiz = quizRepository.findListQuizByUserId(uid);

        ListQuizResponseDto listQuizResponseDto = new ListQuizResponseDto(
                listQuiz.stream()
                       .map(quiz -> QuizDto.builder()
                               .id(quiz.getQuizId())
                               .title(quiz.getTitle())
                               .build())
                       .collect(Collectors.toList())
        );

        return ResponseEntity.ok(listQuizResponseDto);
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public ResponseEntity<QuizResponseDto> getQuizByIdAndUserId(Long idQuiz, String uid) {
        System.out.println("idQuiz: " + idQuiz);

        Quiz quiz = quizRepository.findByIdWithQuestions(idQuiz)
                .filter(q -> q.getUser().getUserId().equals(uid))
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        System.out.println("Quiz title: " + quiz.getTitle());

        // Transformer les questions en DTOs pour n'envoyer que le titre
        List<QuestionDto> questionDtos = quiz.getQuestions().stream()
                .map(q -> new QuestionDto(q.getTitle()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(QuizResponseDto.builder()
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questions(questionDtos)
                .build());
    }

    public Quiz getQuizById(Long idQuiz) {
        return quizRepository.findById(idQuiz)
                .orElseThrow(() -> new EntityNotFoundedException("Ce quizz n'existe pas !!"));
    }

    public Long addQuestionToQuiz(Long idQuiz, AddNewQuestionDTO questionDTO) {
        Quiz quizz = getQuizById(idQuiz);
        Question question = questionService.addQuestion(Question.builder()
                            .title(questionDTO.title())
                            .quiz(quizz)
                            .build());

        questionDTO.answers().forEach(answer -> {
            Response response = Response.builder()
                    .title(answer.title())
                    .isCorrect(answer.isCorrect())
                    .build();
            questionService.addResponsesToQuestion(question.getQuestionId(), response);
        });

        quizz.getQuestions().add(question);

        quizRepository.save(quizz);
        return question.getQuestionId();
    }

    public void updateQuizTitle(Long id, List<PatchQuizTitleRequestDTO> patchQuizTitleRequestDTOS, String uid) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        User user = userRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!quiz.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz does not belong to user");
        }

        for (PatchQuizTitleRequestDTO requestDTO : patchQuizTitleRequestDTOS) {
            if (requestDTO.op().equals("replace") && requestDTO.path().equals("/title")) {
                quiz.setTitle(requestDTO.value());
            }
        }

        quizRepository.save(quiz);
    }
}
