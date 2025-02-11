package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }
}
