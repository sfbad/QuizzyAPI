package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.HostDetailsDTO;
import com.teamcocoon.QuizzyAPI.dtos.StatusUpdateDTO;
import com.teamcocoon.QuizzyAPI.dtos.WebSocketResponseHandlerDTO;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Quiz;
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

@Component
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> room = new ConcurrentHashMap<>();
    private final QuizService quizService ;

    @Autowired
    public WebSocketHandlerImpl(QuizService quizService) {
        this.quizService = quizService;
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
        Quiz quiz = quizService.getQuizByQuizCode(executionId);
        sendMessageToSession(session, quiz, "hostDetails");
        updateStatus(executionId);
        addParticipant(executionId, session);
    }

    private void handleHost(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        Quiz quiz = quizService.getQuizByQuizCode(executionId);
        sendMessageToSession(session, quiz, "hostDetails");
        updateStatus(executionId);
        addParticipant(executionId, session);
    }

    private void handleNextQuestion(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();

        // Vérifier si une file existe
        Queue<Question> queue = questionQueues.get(executionId);
        if (queue == null || queue.isEmpty()) {
            log.info("No more questions available for execution ID: {}", executionId);
            broadcastStatusUpdate(executionId, "{\"name\": \"status\", \"data\": {\"status\": \"finished\"}}");
            return;
        }

        // Récupérer la prochaine question
        Question nextQuestion = queue.poll(); // Récupère et supprime la question en tête de file

        // Construire l'événement 'newQuestion' avec questionId
        Map<String, Object> newQuestionData = Map.of(
                "questionId", nextQuestion.getQuestionId(),
                "question", nextQuestion.getTitle(),
                "answers", nextQuestion.getResponses().stream().map(Response::getText).toList()
        );

        // Envoyer l'événement 'status' et la nouvelle question
        broadcastStatusUpdate(executionId, new ObjectMapper().writeValueAsString(
                new WebSocketResponseHandlerDTO("status", new StatusUpdateDTO("started", getParticipantCount(executionId)))
        ));

        broadcastStatusUpdate(executionId, new ObjectMapper().writeValueAsString(
                new WebSocketResponseHandlerDTO("newQuestion", newQuestionData)
        ));

        log.info("Next question (ID: {}) sent for executionId {}: {}", nextQuestion.getQuestionId(), executionId, nextQuestion.getTitle());
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
