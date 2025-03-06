package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.Response;
import com.teamcocoon.QuizzyAPI.repositories.QuizRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> room = new ConcurrentHashMap<>();
    private final QuizService quizService ;
    private final QuestionService questionService;
    private final Map<String, Queue<Question>> questionQueues = new ConcurrentHashMap<>();


    @Autowired
    public WebSocketHandlerImpl(QuizService quizService, QuestionService questionService) {
        this.quizService = quizService;
        this.questionService = questionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New connection received " + session.getId());
        super.afterConnectionEstablished(session);
    }

    // Méthode pour gérer les messages entrants
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        HostDetailsDTO hostDetailsDTO = parsePayload(payload, HostDetailsDTO.class);
        String name = hostDetailsDTO.name();
        log.info("handling message from " + name+" with payload " + hostDetailsDTO);

        if ("join".equals(name)) {
            handleJoin(session, hostDetailsDTO);
        } else if ("host".equals(name)) {
            handleHost(session, hostDetailsDTO);
        } else if ("nextQuestion".equals(name)) {
            handleNextQuestion(session, hostDetailsDTO);
        }
    }

    // Méthode pour envoyer un message à une session
    void sendMessageToSession(WebSocketSession session, Object dto, String type) throws Exception {
        WebSocketResponseHandlerDTO webSocketResponseHandlerDTO = new WebSocketResponseHandlerDTO(type, dto);
        String message = (new ObjectMapper()).writeValueAsString(webSocketResponseHandlerDTO);
        sendMessage(session, message);
    }

    // Méthode pour envoyer un message texte à une session WebSocket
    public void sendMessage(WebSocketSession session, String message) throws Exception {
        log.info("Sending message to {}: {}", session.getId(), message);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    // Diffusion d'un message à tous les participants d'une salle
    void broadcastStatusUpdate(String executionId, String statusMessage) throws Exception {
        log.info("Broadcasting message {} to room {}", statusMessage, executionId);
        for (WebSocketSession session : getSessions(executionId)) {
            sendMessage(session, statusMessage);
        }
    }


    void addParticipant(String executionId, WebSocketSession session) {
        if (session.isOpen()) {
            room.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    // Met à jour le statut des participants dans la salle
    private void updateStatus(String executionId) throws Exception {
        Thread.sleep(3000);

        int participants = getParticipantCount(executionId);
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("waiting", participants);
        WebSocketResponseHandlerDTO webSocketResponseHandlerDTO = new WebSocketResponseHandlerDTO("status", statusUpdateDTO);
        String message = (new ObjectMapper()).writeValueAsString(webSocketResponseHandlerDTO);
        broadcastStatusUpdate(executionId, message);
    }

    // Récupère le nombre de participants dans une salle
    private int getParticipantCount(String executionId) {
        Set<WebSocketSession> participants = room.get(executionId);
        return participants == null ? 0 : participants.size();
    }


    private void handleJoin(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);
        sendMessageToSession(session, quiz, "hostDetails");
        updateStatus(executionId);
        addParticipant(executionId, session);
    }

    private void handleHost(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        log.info("Handling message to host {}", hostDetailsDTO);
        String executionId = hostDetailsDTO.data().executionId();
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);
        sendMessageToSession(session, quiz, "hostDetails");
        updateStatus(executionId);
        addParticipant(executionId, session);
    }

    private void handleNextQuestion(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();

        String checkedExecutionId = getExecutionIdFromSession(session);

        if (checkedExecutionId != null && checkedExecutionId.equals(executionId)) {
            // Vérifier si une file existe
            Optional<List<Question>> questions = questionService.getQuestionsByQuizIdAndQuizCode(checkedExecutionId);
            if (!questions.isPresent()) {
                log.info("No question found for execution ID: {}", executionId);
                return;
            }
            questions.get().forEach(question -> {
                // Ajouter la question à la file
                questionQueues.computeIfAbsent(executionId, k -> new ConcurrentLinkedQueue<>()).add(question);
            });

            Queue<Question> queue = questionQueues.get(executionId);
            if (queue == null || queue.isEmpty()) {
                log.info("No more questions available for execution ID: {}", executionId);
                sendMessage(session,"No more questions available");
                return;
            }

            // Récupérer la prochaine question
            Question nextQuestion = queue.poll(); // Récupère et supprime la question en tête de file
            QuestionTitleDTO questionTitle = new QuestionTitleDTO(nextQuestion.getTitle());
            List<ResponseTitleDTO> answers = new ArrayList<ResponseTitleDTO>();
            List<Response> responses = questionService.getResponsesByQuestion(nextQuestion.getQuestionId());
            responses.forEach(response ->{
                ResponseTitleDTO responseTitle = new ResponseTitleDTO(response.getTitle());
                answers.add(responseTitle);
            });

            NextQuestionWSDTO nextQuestionWSDTO = new NextQuestionWSDTO(questionTitle,answers);

            StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("started",getParticipantCount(executionId));
            sendMessageToSession(session,statusUpdateDTO,"status");
            Thread.sleep(3000);
            sendMessageToSession(session, nextQuestionWSDTO, "nextQuestion");
        }else{
            log.info("Execution ID not found in session, rejecting the request.");
            sendMessageToSession(session, "Execution ID not found in session, rejecting the request.", "error");
        }
    }



    private String getExecutionIdFromSession(WebSocketSession session) {
        log.info("seeking for execution id for {}", session.getId());
        log.info(" room's size {}", room.size());
        for (Map.Entry<String, Set<WebSocketSession>> entry : room.entrySet()) {
            String executionId = entry.getKey();
            Set<WebSocketSession> sessions = entry.getValue();
            if (sessions.contains(session)) {
                return executionId;
            }
        }
        return null;
    }

    private <T> T parsePayload(String payload, Class<T> classDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(payload, classDTO);
    }

    private Set<WebSocketSession> getSessions(String executionId) {
        return room.getOrDefault(executionId, new HashSet<>());
    }

    public void cleanUpClosedSessions() {
        room.forEach((executionId, sessions) -> sessions.removeIf(session -> !session.isOpen()));
    }

}
