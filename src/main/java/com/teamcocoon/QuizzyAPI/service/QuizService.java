package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.Response;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import com.teamcocoon.QuizzyAPI.repositories.ResponseRepository;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);
    @Autowired
    private final QuizRepository quizRepository;
    @Autowired
    private final QuestionService questionService;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private ResponseRepository responseRepository;
    @Autowired
    private UserService userService;

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

    public Quiz createQuiz(QuizDto quizDto, String uid) {
        System.out.println("UID reçu : " + uid);

            User user = userService.getUserByUID(uid);
            System.out.println("Utilisateur trouvé : " + user);

            Quiz quiz = new Quiz();
            quiz.setTitle(quizDto.title());
            quiz.setDescription(quizDto.description());
            quiz.setUser(user);

            return quizRepository.save(quiz);

    }

   /* public ResponseEntity<QuizResponseDto> getQuizByIdAndUserId(Long idQuiz, String uid) {
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
    }*/

    public ResponseEntity<ListQuestionsDto> getQuizByIdAndUserId(Long idQuiz, String uid) {
        log.info("Recuperation du quizz : " + idQuiz);
        Quiz quiz = quizRepository.findByIdWithQuestions(idQuiz)
                .filter(q -> q.getUser().getUserId().equals(uid))
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        //System.out.println("Quiz title: " + quiz.getTitle());
        List<Question> questions = quiz.getQuestions();
        List<QuestionAnswersDto> questionDtoss = new ArrayList<>();


        questions.forEach(question -> {
            List<Response> responses = questionService.getResponsesByQuestion(question.getQuestionId());
            List<AnswersDTO> answersDtos = new ArrayList<>();

            responses.forEach(response -> {
                AnswersDTO answersDTO = AnswersDTO.builder()
                        .title(response.getTitle())
                        .isCorrect(response.isCorrect())
                        .build();
                answersDtos.add(answersDTO);
            });
            QuestionAnswersDto questionAnswersDto = QuestionAnswersDto.builder()
                    .title(question.getTitle())
                    .answers(answersDtos)
                    .build();

            questionDtoss.add(questionAnswersDto);
        });

//        List<QuestionAnswersDto> questionDtos = quiz.getQuestions().stream()
//                .map(q -> QuestionAnswersDto.builder()
//                        .title(q.getTitle())
//                        .answers(q.getResponses().stream()
//                                .map(r -> AnswersDTO.builder()
//                                        .title(r.getTitle())
//                                        .isCorrect(r.isCorrect())
//                                        .build())
//                                .collect(Collectors.toList()))
//                        .build())
//                .collect(Collectors.toList());
        log.info("Envoie de la liste aves ces questions  : " + questionDtoss);

        return ResponseEntity.ok(ListQuestionsDto.builder()
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questions(questionDtoss)
                .build());
    }


    public Quiz getQuizById(Long idQuiz) {
        return quizRepository.findById(idQuiz)
                .orElseThrow(() -> new EntityNotFoundedException("Ce quizz n'existe pas !!"));
    }

    public Long addQuestionToQuiz(Long idQuiz, @NotNull AddNewQuestionDTO questionDTO) {
        Quiz quizz = getQuizById(idQuiz);
        Question question = questionService.saveQuestion(Question.builder()
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

    public void updateQuestion(Long quizId, Long questionId, String newTitle, List<AnswersDTO> updatedAnswersDTOs) {
        log.info("Updating question with : " +updatedAnswersDTOs);
        Quiz quiz = getQuizById(quizId);
        Question   question = questionService.getQuestionById(questionId);
        if (!question.getQuiz().equals(quiz)) {
            throw new EntityNotFoundedException("Question does not belong to the specified quiz");
        }
        questionService.updateQuestionTitle(question, newTitle);
        List<Response> responses = questionService.getResponsesByQuestion(questionId);
        assert  responses != null;
        responses.forEach(response -> {
            questionService.deleteAllAnswers(response);
        });

        updatedAnswersDTOs.forEach(answer -> {
            Response response = Response.builder()
                    .title(answer.title())
                    .isCorrect(answer.isCorrect())
                    .build();
            log.info("Response : " + response);
            responseRepository.save(response);
            questionService.addResponsesToQuestion(questionId,response);
        });
        questionService.saveQuestion(question);
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
