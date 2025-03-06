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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

    /**
     * Issue12
     * Récupère les quiz d'un utilisateur et ajoute un lien HATEOAS pour la création de quiz.
     * ajout d'un lien de création de quiz dans la réponse.
     */
    public ResponseEntity<ListQuizResponseDto> getListQuizByUserId(String uid) {
        List<Quiz> listQuiz = quizRepository.findListQuizByUserId(uid);

//        listQuiz.forEach(quiz -> {
//            System.out.println("zezezezezezez" + quiz.getTitle());
//            System.out.println("taille des questions +++++ " +quiz.getQuestions().size());
//            System.out.println("taille des responses ------ ");
//            quiz.getQuestions().forEach(r-> {
//
//
//                List<Response> responses = questionService.getResponsesByQuestion(r.getQuestionId());
//                System.out.println("--- " + responses.size() + " ------");
//
//            });
//        });
        List<QuizDto> quizDtoList = listQuiz.stream()
                .map(quiz -> {
                    Map<String, String> links = new HashMap<>();
                    if (isQuizStartable(quiz)) {
                        links.put("start", "http://127.0.0.1:3000/api/quiz/" + quiz.getQuizId() + "/start");
                    }
                    return new QuizDto(quiz.getQuizId(), quiz.getTitle(), quiz.getDescription(), links.isEmpty() ? null : links);
                })
                .collect(Collectors.toList());

        ListQuizResponseDto listQuizResponseDto = new ListQuizResponseDto(quizDtoList);

        return ResponseEntity.ok(listQuizResponseDto);
    }

    private boolean isQuizStartable(Quiz quiz) {
        return checkForQuestionValidity(quiz.getQuestions()) &&
              chekForTitleValidity(quiz) &&
              chekForQuizzNotEmptyQuestionListValidity(quiz);
    }
    private boolean chekForTitleValidity(Quiz quiz) {
        return !quiz.getTitle().isEmpty();
    }
    private boolean chekForQuizzNotEmptyQuestionListValidity(Quiz quiz) {
        return quiz.getQuestions() != null && !quiz.getQuestions().isEmpty();
    }
    private boolean checkForQuestionValidity(List<Question> questions){

        for (Question question : questions) {
            if (!isQuestionValid(question)) {
                return false;
            }
        }
        return true;
    }
    private boolean isQuestionValid(Question question) {
        List<Response> responses = questionService.getResponsesByQuestion(question.getQuestionId());
        if (question.getTitle() == null || question.getTitle().isEmpty() ) {
            return false;
        }
        // Vérifiez qu'il y a au moins deux réponses.
        if (responses == null || responses.size() < 2) {
            return false;
        }
        long correctAnswersCount = correctAnswerCountFor(responses);
        System.out.println(" nombre de reponses valides " +correctAnswersCount);
        return correctAnswersCount >= 1;
    }

    private long correctAnswerCountFor(List<Response> responses){
        return responses.stream().filter(Response::isCorrect).count();
    }


    /**
     * Sauvegarde simple d'un quiz en base de données.
     * return Le quiz persisté
     */
    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    /**
     * Issue6
     * Crée un nouveau quiz pour un utilisateur à partir des informations minimales.
     * Implémente l'issue6' de création de quiz avec génération automatique de l'identifiant.
     *
     * Étapes clés :
     * - Vérifie l'authentification de l'utilisateur
     * - Associe le quiz au bon utilisateur
     * - Génère une URI de localisation pour accéder au quiz créé
     */
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

    /**
     * Issue7
     * Récupère un quiz avec ses questions, vérifiant l'appartenance à l'utilisateur.
     * Transforme les entités en DTO pour la réponse API.
     * param idQuiz Identifiant du quiz
     * param uid Identifiant de l'utilisateur
     * return DTO contenant les détails du quiz et ses questions
     * throws RuntimeException si le quiz n'existe pas ou n'appartient pas à l'utilisateur
     */
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

    /**
     * Ajoute une nouvelle question à un quiz existant.
     *
     * Cette méthode :
     * 1. Récupère le quiz par son ID
     * 2. Crée et sauvegarde une nouvelle question associée au quiz
     * 3. Ajoute les réponses à la question
     * 4. Met à jour le quiz avec la nouvelle question
     *
     * param idQuiz L'identifiant du quiz auquel ajouter la question
     * param questionDTO Les détails de la nouvelle question (titre et réponses)
     * return L'identifiant de la question nouvellement créée
     */
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

    /**
     * Issue11
     * Met à jour une question dans un quiz spécifique.
     * Supprime les anciennes réponses et ajoute les nouvelles.
     * param quizId Identifiant du quiz
     * param questionId Identifiant de la question à modifier
     * param newTitle Nouveau titre de la question
     * param updatedAnswersDTOs Nouvelles réponses à associer
     * throws EntityNotFoundedException si la question n'appartient pas au quiz
     */
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
            question.getResponses().remove(response);
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

    /**
     * Mise à jour du titre d'un quiz avec validation de propriété.
     * Gère la modification partielle d'un quiz selon les spécifications JSON Patch.
     * Vérifie que l'utilisateur est bien propriétaire du quiz avant modification.
     */
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

    /**
     * Génère un identifiant aléatoire de 6 caractères alphanumériques.
     * @return Code d'exécution du quiz
     */
    public String generateExecutionId() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    /**
     * Retrouve un quiz spécifique appartenant à un utilisateur.
     * return Le quiz si trouvé, null sinon
     */
    public Quiz getQuizByUserId(String userId, Long quizId) {

        List<Quiz> quizzes = quizRepository.findListQuizByUserId(userId);
        log.info("Quiz size "+quizzes.size()+"");

        //boucle sur quizzes
        for (Quiz quiz : quizzes) {
            // Si on trouve le quiz avec le même id
            //log.info(quiz.getTitle());
            if (quiz.getQuizId().equals(quizId)) {
                log.info(quiz.getTitle());
                return quiz;
            }
        }
        // Si on ne trouve pas de quiz avec le même id
        return null;
    }

    /**
     * Vérifie si un quiz est prêt à être démarré.
     * Conditions : quiz non vide et chaque question a au moins une réponse.
     * return true si le quiz est complet, false sinon
     */
    public boolean isQuizReady(Quiz quiz) {
        // Vérifier que le quiz a des questions
        System.out.println("question : " + quiz.getQuestions().size());
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            return false;
        }

        // Vérifier que chaque question a au moins une réponse
        for (Question question : quiz.getQuestions()) {
            System.out.println("response size " +  question.getResponses().size());
            if (question.getResponses() == null || question.getResponses().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
