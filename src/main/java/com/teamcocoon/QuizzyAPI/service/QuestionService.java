package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.Response;
import com.teamcocoon.QuizzyAPI.repositories.QuestionRepository;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import com.teamcocoon.QuizzyAPI.repositories.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;
    private final QuizRepository quizRepository;

    public Question saveQuestion(Question question) {
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
        log.info("Adding responses to question {}", questionId);
        Question question = getQuestionById(questionId);
        response.setQuestion(question);
        question.getResponses().add(response);
        questionRepository.save(question);
        log.info("Adding responses to question {} fin", questionId);

    }

    public void deleteAllByQuestion(Question question) {
        responseRepository.deleteAllByQuestion(question.getQuestionId());
    }
    public void deleteAllAnswers(Response response) {
        responseRepository.delete(response);
    }

    public List<Response> getResponsesByQuestion(Long questionId) {

        return responseRepository.findResponseByQuestion_QuestionId(questionId);

    }

    public void updateQuestionTitle(Question question, String title)
    {
        Question updatedQuestion = getQuestionById(question.getQuestionId());
        updatedQuestion.setTitle(title);
        questionRepository.save(updatedQuestion);
    }

    public Optional<List<Question>> getQuestionsByQuizIdAndQuizCode(String quizCode) {
        return questionRepository.findByQuiz_QuizCode(quizCode);
    }
}
