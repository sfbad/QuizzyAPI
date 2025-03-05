package com.teamcocoon.QuizzyAPI.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ParticipantService {

    private final Map<String, Set<WebSocketSession>> room = new ConcurrentHashMap<>();


    public void addParticipant(String executionId, WebSocketSession session) {
        if (session.isOpen()) {
            room.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    public int getParticipantCount(String executionId) {
        Set<WebSocketSession> participants = room.get(executionId);
        if (participants == null) {
            return 0;
        }
        return participants.size();
    }

    public void removeParticipant(String executionId, WebSocketSession session) {
        Set<WebSocketSession> participants = room.get(executionId);
        if (participants != null) {
            participants.removeIf(session1 -> !session1.isOpen());
            participants.remove(session);
        }
    }

    public Set<WebSocketSession> getSessions(String executionId) {
        return room.getOrDefault(executionId, new HashSet<>());
    }

    public void cleanUpClosedSessions() {
        room.forEach((executionId, sessions) ->
                sessions.removeIf(session -> !session.isOpen())
        );
    }
}
