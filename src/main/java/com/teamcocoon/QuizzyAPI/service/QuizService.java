package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.ListQuizResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.QuestionDto;
import com.teamcocoon.QuizzyAPI.dtos.QuizDto;
import com.teamcocoon.QuizzyAPI.dtos.QuizResponseDto;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
public class QuizService {

    @Autowired
    private final QuizRepository quizRepository;

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

}
