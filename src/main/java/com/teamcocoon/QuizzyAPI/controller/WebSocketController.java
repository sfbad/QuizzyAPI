package com.teamcocoon.QuizzyAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Map to hold rooms with executionId as the key, and participants as the values
    private Map<String, Set<String>> rooms = new HashMap<>();

    // Handle the 'host' event
    @MessageMapping("/host")
    public void handleHostEvent(HostEventData event, WebSocketSession session) {
        String executionId = event.getExecutionId();

        // Store the host in the room (executionId)
        rooms.computeIfAbsent(executionId, k -> new HashSet<>()).add(session.getId());

        // Fetch quiz title based on executionId
        String quizTitle = getQuizTitleByExecutionId(executionId);

        // Send the quiz title to the host
        messagingTemplate.convertAndSendToUser(session.getId(), "/queue/hostDetails", new HostDetails(quizTitle));

        // Broadcast status to all participants in the room (executionId)
        sendStatusUpdate(executionId);
    }

    // Handle the 'join' event from a participant
    @MessageMapping("/join")
    public void handleJoinEvent(String executionId, WebSocketSession session) {
        // Add the participant to the corresponding room
        rooms.computeIfAbsent(executionId, k -> new HashSet<>()).add(session.getId());

        // Send the quiz title to the joining client
        String quizTitle = getQuizTitleByExecutionId(executionId);
        messagingTemplate.convertAndSendToUser(session.getId(), "/queue/joinDetails", new JoinDetails(quizTitle));

        // Broadcast the updated status to all participants in the room (executionId)
        sendStatusUpdate(executionId);
    }

    // Handle disconnect event
    @MessageMapping("/disconnect")
    public void handleDisconnectEvent(WebSocketSession session) {
        // Remove the client from all rooms they are part of
        rooms.values().forEach(room -> room.remove(session.getId()));

        // Broadcast updated participant count to all participants
        sendStatusUpdateForAllRooms();
    }

    // This method sends status updates to everyone in the room (executionId)
    private void sendStatusUpdate(String executionId) {
        String statusMessage = "waiting";  // Placeholder for actual status logic
        String participantsCount = String.valueOf(rooms.get(executionId).size());

        // Broadcast the status update to all clients in the room
        messagingTemplate.convertAndSend("/topic/" + executionId + "/status", new StatusUpdate(statusMessage, participantsCount));
    }

    // This method sends status updates to all rooms
    private void sendStatusUpdateForAllRooms() {
        rooms.forEach((executionId, clients) -> {
            String statusMessage = "waiting";
            String participantsCount = String.valueOf(clients.size());
            messagingTemplate.convertAndSend("/topic/" + executionId + "/status", new StatusUpdate(statusMessage, participantsCount));
        });
    }

    // Fetch quiz title based on executionId
    private String getQuizTitleByExecutionId(String executionId) {
        // You need to implement this method to fetch quiz title from the executionId
        return "Sample Quiz Title";  // Placeholder
    }

    // DTO for host details
    public static class HostDetails {
        private String quiz;

        public HostDetails(String quiz) {
            this.quiz = quiz;
        }

        public String getQuiz() {
            return quiz;
        }
    }

    // DTO for join details (for participants)
    public static class JoinDetails {
        private String quiz;

        public JoinDetails(String quiz) {
            this.quiz = quiz;
        }

        public String getQuiz() {
            return quiz;
        }
    }

    // DTO for status update
    public static class StatusUpdate {
        private String status;
        private String participants;

        public StatusUpdate(String status, String participants) {
            this.status = status;
            this.participants = participants;
        }

        public String getStatus() {
            return status;
        }

        public String getParticipants() {
            return participants;
        }
    }

    // DTO for host event data
    public static class HostEventData {
        private String executionId;

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }
    }
}
