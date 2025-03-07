package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
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

    /**
     * Called when a new WebSocket connection is established.
     * Logs the new connection and allows any further setup for the session.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New connection received: " + session.getId());
        super.afterConnectionEstablished(session);
    }

    /**
     * Handles incoming text messages from clients.
     * Based on the message content, it decides whether to process a "join", "host", or "nextQuestion" request.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        HostDetailsDTO hostDetailsDTO = null;

        try {
            hostDetailsDTO = parsePayload(payload, HostDetailsDTO.class);
            String name = hostDetailsDTO.name();
            log.info("Handling message from " + name + " with payload: " + hostDetailsDTO);

            if ("join".equals(name)) {
                handleJoin(session, hostDetailsDTO);
            } else if ("host".equals(name)) {
                handleHost(session, hostDetailsDTO);
            } else if ("nextQuestion".equals(name)) {
                handleNextQuestion(session, hostDetailsDTO);
            }
        } catch (Exception e) {
            log.error("Error handling message from session {}: {}", session.getId(), e.getMessage());
            if (hostDetailsDTO != null) {
                ExecutionSessionService executionSessionService = room.get(hostDetailsDTO.data().executionId());
                if (executionSessionService != null) {
                    executionSessionService.sendMessageToSession(session, "error", "An error occurred while processing your request.");
                }
            }
        } finally {
            // Actions to always execute (e.g., cleanup, log)
            log.info("Finished handling message for session {}", session.getId());
        }
    }

    /**
     * Adds the session to the appropriate room (either as a host or participant) for the given execution ID.
     * Ensures no duplicates of sessions in the same room.
     */
    void addToRoom(String executionId, WebSocketSession session, boolean isHost) {
        ExecutionSessionService executionSessionService = room.computeIfAbsent(executionId, k -> new ExecutionSessionService());

        try {
            if (isHost) {
                // Vérification si l'hôte n'est pas déjà présent
                if (!executionSessionService.getHosts().contains(session)) {
                    executionSessionService.addHost(session);
                } else {
                    log.info("Host with session ID {} already exists in room {}", session.getId(), executionId);
                }
            } else {
                // Vérification si le participant n'est pas déjà présent
                if (!executionSessionService.getParticipants().contains(session)) {
                    executionSessionService.addParticipant(session);
                } else {
                    log.info("Participant with session ID {} already exists in room {}", session.getId(), executionId);
                }
            }
        } catch (Exception e) {
            log.error("Error adding session to room {}: {}", executionId, e.getMessage());
            ExecutionSessionService executionSessionServiceA = room.get(executionId);
            if (executionSessionServiceA != null) {
                try {
                    executionSessionService.sendMessageToSession(session, "error", "An error occurred while adding you to the room.");
                } catch (Exception sendMessageException) {
                    log.error("Error sending message to session {}: {}", session.getId(), sendMessageException.getMessage());
                }
            }
        } finally {
            log.info("Attempted to add session {} to room {}", session.getId(), executionId);
        }
    }

    /**
     * Updates the status of the quiz room, broadcasting the status (waiting or in progress) to all participants and hosts.
     */
    private void updateStatus(String executionId,String status) throws Exception {
        try {
            int participants = getParticipantCount(executionId);
            StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO(status, participants);

            ExecutionSessionService executionSessionService = room.get(executionId);
            if (executionSessionService != null) {
                executionSessionService.broadcastMessageToEveryBody("status", statusUpdateDTO);

            }
        } catch (Exception e) {
            log.error("Error updating status for execution ID {}: {}", executionId, e.getMessage());
        } finally {
            log.info("Status update attempted for execution ID {}", executionId);
        }
    }

    /**
     * Gets the number of participants in the quiz room identified by the given execution ID.
     */
    private int getParticipantCount(String executionId) {
        ExecutionSessionService executionSessionService = room.get(executionId);
        return executionSessionService != null ? executionSessionService.getParticipantsCount() : 0;
    }

    /**
     * Handles a "join" message from a participant.
     * Adds the participant to the room and sends back the quiz details.
     */
    private void handleJoin(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);

        ExecutionSessionService executionSessionService = room.get(executionId);
        if (executionSessionService != null) {
            try {

                //executionSessionService.broadcastMessageToHost(executionSessionService.getHosts(), "joinDetails", new QuizTitleWSDTO(quiz.title()));
                executionSessionService.broadcastMessageToEveryBody("joinDetails", new QuizTitleWSDTO(quiz.title()));

            } catch (Exception e) {
                log.error("Error sending join details to session {}: {}", session.getId(), e.getMessage());
            }
        }

        addToRoom(executionId, session, false);
        updateStatus(executionId,"waiting");
    }

    /**
     * Handles a "host" message.
     * Adds the host to the room and sends back the quiz details.
     */
    private void handleHost(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        addToRoom(executionId, session, true);
        QuizDToRelease quiz = quizService.getQuizByQuizCode(executionId);

        ExecutionSessionService executionSessionService = room.get(executionId);
        if (executionSessionService != null) {
            try {
                executionSessionService.broadcastMessageToHost(executionSessionService.getHosts(), "hostDetails", new Quizzy(quiz));
                StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("status", getParticipantCount(executionId));
                executionSessionService.broadcastMessageToHost(executionSessionService.getHosts(), "status", statusUpdateDTO);

            } catch (Exception e) {
                log.error("Error sending host details to session {}: {}", session.getId(), e.getMessage());
            }
        }
        updateStatus(executionId,"waiting");
    }

    /**
     * Handles the "nextQuestion" message, fetching and sending the next question to the client.
     */
    private void handleNextQuestion(WebSocketSession session, HostDetailsDTO hostDetailsDTO) throws Exception {
        String executionId = hostDetailsDTO.data().executionId();
        String checkedExecutionId = getExecutionIdFromSession(session);

        try {
            if (checkedExecutionId != null && checkedExecutionId.equals(executionId)) {
                Optional<List<Question>> questions = questionService.getQuestionsByQuizIdAndQuizCode(checkedExecutionId);
                if (questions.isEmpty()) {
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
                List<String> answers = new ArrayList<>();
                List<Response> responses = questionService.getResponsesByQuestion(nextQuestion.getQuestionId());
                responses.forEach(response -> {
                    answers.add(response.getTitle());
                });

                NextQuestionWSDTO nextQuestionWSDTO = new NextQuestionWSDTO(nextQuestion.getTitle(), answers);
                StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO("started", getParticipantCount(executionId));

                ExecutionSessionService executionSessionService = room.get(executionId);
                if (executionSessionService != null) {
                    executionSessionService.broadcastMessageToHost(executionSessionService.getHosts(), "status", statusUpdateDTO);
                    executionSessionService.broadcastMessageToEveryBody("status", statusUpdateDTO);
                    executionSessionService.broadcastMessageToParticipants(executionSessionService.getParticipants(), "newQuestion", nextQuestionWSDTO);
                }
            } else {
                log.info("Execution ID not found in session, rejecting the request.");
                ExecutionSessionService executionSessionService = room.get(executionId);
                if (executionSessionService != null) {
                    executionSessionService.sendMessageToSession(session, "error", "Execution ID not found in session, rejecting the request.");
                }
            }
        } catch (Exception e) {
            log.error("Error handling next question for execution ID {}: {}", executionId, e.getMessage());
            ExecutionSessionService executionSessionService = room.get(executionId);
            if (executionSessionService != null) {
                executionSessionService.sendMessageToSession(session, "error", "An error occurred while processing the next question.");
            }
        } finally {
            log.info("Finished handling next question for session {}", session.getId());
        }
    }

    /**
     * Retrieves the execution ID associated with the provided session.
     * Used to determine which quiz room the session belongs to.
     */
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

    /**
     * Parses the payload string into a specified DTO object.
     * Uses Jackson ObjectMapper to perform the conversion.
     */
    private <T> T parsePayload(String payload, Class<T> classDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(payload, classDTO);
    }

    /**
     * Called when a WebSocket connection is closed.
     * Handles the cleanup process, including updating participant and host statuses and closing open connections.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        log.info("Connection closed: " + session.getId());

        // Trouver l'executionId associée à cette session
        String executionId = getExecutionIdFromSession(session);

        if (executionId != null) {
            ExecutionSessionService executionSessionService = room.get(executionId);

            if (executionSessionService != null) {
                boolean isHost = executionSessionService.getHosts().contains(session);

                if (isHost) {
                    executionSessionService.removeHost(session);
                    log.info("Host removed: " + session.getId());

                    // Si aucun autre hôte n'existe, envoyer un message à tous les participants et fermer la session
                    if (executionSessionService.getHosts().isEmpty()) {
                        log.warn("No hosts left for executionId: " + executionId);

                        // Créer un message pour informer tous les participants qu'il n'y a plus d'hôte
                        String message = "Il n'y a plus d'hôte pour cette session. La session va être fermée.";
                        for (WebSocketSession participantSession : executionSessionService.getParticipants()) {
                            if (participantSession.isOpen()) {
                                participantSession.sendMessage(new TextMessage(message));
                            }
                        }
                        for (WebSocketSession participantSession : executionSessionService.getParticipants()) {
                            if (participantSession.isOpen()) {
                                participantSession.close(CloseStatus.NORMAL);
                            }
                        }
                        if (session.isOpen()) {
                            session.close(CloseStatus.NORMAL);
                        }
                        room.remove(executionId);
                        log.info("Session closed and removed: " + executionId);
                    }
                } else {
                    executionSessionService.removeParticipant(session);
                    log.info("Participant removed: " + session.getId());
                }

                //updateStatus(executionId,"");
            }
        }
        super.afterConnectionClosed(session, status);
    }
}
