package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    private final Map<String, ExecutionSessionService> room = new ConcurrentHashMap<>();
    private final QuizService quizService;
    private final QuestionService questionService;
    private final Map<String, Queue<Question>> questionQueues = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketHandlerImpl(QuizService quizService, QuestionService questionService) {
        this.quizService = quizService;
        this.questionService = questionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New connection received: " + session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        HostDetailsDTO hostDetailsDTO = parsePayload(payload, HostDetailsDTO.class);
        String name = hostDetailsDTO.name();
        log.info("Handling message from " + name + " with payload: " + hostDetailsDTO);

        if ("join".equals(name)) {
            handleJoin(session, hostDetailsDTO);
        } else if ("host".equals(name)) {
            handleHost(session, hostDetailsDTO);
        } else if ("nextQuestion".equals(name)) {
            handleNextQuestion(session, hostDetailsDTO);
        }
    }

    private void addToRoom(String executionId, WebSocketSession session, boolean isHost) {
        ExecutionSessionService executionSessionService = room.computeIfAbsent(executionId, k -> new ExecutionSessionService());

        if (isHost) {
            executionSessionService.addHost(session);
        } else {
            executionSessionService.addParticipant(session);
        }
    }

    private void updateStatus(String executionId) throws Exception {
        int participants = getParticipantCount(executionId);
        StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("waiting", participants);

        // Diffusion du message à tous les participants et hôtes
        ExecutionSessionService executionSessionService = room.get(executionId);
        if (executionSessionService != null) {
            executionSessionService.broadcastMessageToParticipants(executionSessionService.getParticipants(), "status", statusUpdateDTO);
        }
    }

    private int getParticipantCount(String executionId) {
        ExecutionSessionService executionSessionService = room.get(executionId);
        return executionSessionService != null ? executionSessionService.getParticipantsCount() : 0;
    }

    private void handleJoin(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);

        ExecutionSessionService executionSessionService = room.get(executionId);
        if (executionSessionService != null) {
            executionSessionService.sendMessageToSession(session, "joinDetails", quiz);
        }

        addToRoom(executionId, session, false); // Ajouter en tant que participant
        updateStatus(executionId);
    }

    private void handleHost(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        addToRoom(executionId, session, true); // Ajouter en tant qu'hôte
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);

        ExecutionSessionService executionSessionService = room.get(executionId);
        if (executionSessionService != null) {
            executionSessionService.sendMessageToSession(session, "hostDetails", quiz);
        }

        updateStatus(executionId); // Mettre à jour le statut de la salle
    }

    private void handleNextQuestion(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        String checkedExecutionId = getExecutionIdFromSession(session);

        if (checkedExecutionId != null && checkedExecutionId.equals(executionId)) {
            Optional<List<Question>> questions = questionService.getQuestionsByQuizIdAndQuizCode(checkedExecutionId);
            if (!questions.isPresent()) {
                log.info("No question found for execution ID: {}", executionId);
                return;
            }

            questions.get().forEach(question -> {
                questionQueues.computeIfAbsent(executionId, k -> new ConcurrentLinkedQueue<>()).add(question);
            });

            Queue<Question> queue = questionQueues.get(executionId);
            if (queue == null || queue.isEmpty()) {
                log.info("No more questions available for execution ID: {}", executionId);
                ExecutionSessionService executionSessionService = room.get(executionId);
                if (executionSessionService != null) {
                    executionSessionService.sendMessageToSession(session, "error", "No more questions available");
                }
                return;
            }

            Question nextQuestion = queue.poll();
            QuestionTitleDTO questionTitle = new QuestionTitleDTO(nextQuestion.getTitle());
            List<ResponseTitleDTO> answers = new ArrayList<>();
            List<Response> responses = questionService.getResponsesByQuestion(nextQuestion.getQuestionId());
            responses.forEach(response -> {
                ResponseTitleDTO responseTitle = new ResponseTitleDTO(response.getTitle());
                answers.add(responseTitle);
            });

            NextQuestionWSDTO nextQuestionWSDTO = new NextQuestionWSDTO(questionTitle, answers);
            StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("started", getParticipantCount(executionId));

            ExecutionSessionService executionSessionService = room.get(executionId);
            if (executionSessionService != null) {
                executionSessionService.sendMessageToSession(session, "status", statusUpdateDTO);
                executionSessionService.sendMessageToSession(session, "nextQuestion", nextQuestionWSDTO);
            }
        } else {
            log.info("Execution ID not found in session, rejecting the request.");
            ExecutionSessionService executionSessionService = room.get(executionId);
            if (executionSessionService != null) {
                executionSessionService.sendMessageToSession(session, "error", "Execution ID not found in session, rejecting the request.");
            }
        }
    }

    private String getExecutionIdFromSession(WebSocketSession session) {
        log.info("Seeking execution ID for session {}", session.getId());
        for (Map.Entry<String, ExecutionSessionService> entry : room.entrySet()) {
            String executionId = entry.getKey();
            ExecutionSessionService executionSessionService = entry.getValue();
            if (executionSessionService.getParticipants().contains(session) || executionSessionService.getHosts().contains(session)) {
                return executionId;
            }
        }
        return null;
    }

    private <T> T parsePayload(String payload, Class<T> classDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(payload, classDTO);
    }

}
