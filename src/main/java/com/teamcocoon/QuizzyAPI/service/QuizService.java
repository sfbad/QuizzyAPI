package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.ListQuizResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.PatchQuizTitleRequestDTO;
import com.teamcocoon.QuizzyAPI.dtos.QuizDto;
import com.teamcocoon.QuizzyAPI.model.Quiz;
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

    public void updateQuizTitle(Long id, List<PatchQuizTitleRequestDTO> patchQuizTitleRequestDTOS, String username) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

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
