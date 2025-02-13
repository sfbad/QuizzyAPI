package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Response;
import com.teamcocoon.QuizzyAPI.repositories.QuestionRepository;
import com.teamcocoon.QuizzyAPI.repositories.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;

    public Question addQuestion(Question question) {
       return questionRepository.save(question);
    }

    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }
    public  Question getQuestionById(long id) {
        return questionRepository.findById(id).
                orElseThrow(()-> new EntityNotFoundedException("Question not found"));
    }

    public void addResponsesToQuestion(Long  questionId, Response response) {
        Question question = getQuestionById(questionId);
        response.setQuestion(question);
        responseRepository.save(response);
    }
}
