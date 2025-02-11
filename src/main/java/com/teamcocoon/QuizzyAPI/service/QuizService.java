package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.ListQuizResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.QuizDto;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

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
}
