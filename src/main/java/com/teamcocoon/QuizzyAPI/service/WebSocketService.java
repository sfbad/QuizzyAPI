package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Slf4j
@Service
public class WebSocketService {

    private final Map<String, Set<WebSocketSession>> room = new ConcurrentHashMap<>();

    // Méthode pour envoyer un message à une session WebSocket spécifique
    public void sendMessage(WebSocketSession session, String message) throws Exception {
        log.info("Sending message to {} and message {}", session.getId(), message);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    // Méthode pour diffuser un message de mise à jour de statut à tous les participants d'une salle spécifique
    public void broadcastStatusUpdate(String executionId, String statusMessage) throws Exception {
        log.info("BroadCasting message {} to  {}", statusMessage, executionId);
        for (WebSocketSession session : getSessions(executionId)) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(statusMessage));
            }
        }
    }

    // Méthode pour ajouter un participant à une salle donnée
    public void addParticipant(String executionId, WebSocketSession session) {
        if (session.isOpen()) {
            room.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    // Méthode pour obtenir le nombre de participants dans une salle spécifique
    public int getParticipantCount(String executionId) {
        Set<WebSocketSession> participants = room.get(executionId);
        if (participants == null) {
            return 0;
        }
        return participants.size();
    }

    // Méthode pour retirer un participant d'une salle spécifique
    public void removeParticipant(String executionId, WebSocketSession session) {
        Set<WebSocketSession> participants = room.get(executionId);
        if (participants != null) {
            participants.removeIf(session1 -> !session1.isOpen());
            participants.remove(session);
        }
    }

    // Méthode pour obtenir les sessions (participants) d'une salle spécifique
    public Set<WebSocketSession> getSessions(String executionId) {
        return room.getOrDefault(executionId, new HashSet<>());
    }

    // Méthode pour nettoyer les sessions fermées
    public void cleanUpClosedSessions() {
        room.forEach((executionId, sessions) ->
                sessions.removeIf(session -> !session.isOpen())
        );
    }
}
