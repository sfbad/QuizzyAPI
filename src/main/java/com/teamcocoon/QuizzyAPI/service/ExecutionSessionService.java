package com.teamcocoon.QuizzyAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.StatusUpdateDTO;
import com.teamcocoon.QuizzyAPI.dtos.WebSocketResponseHandlerDTO;
import com.teamcocoon.QuizzyAPI.exceptions.EntityAlreadyExists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
@Getter
@Setter
@NoArgsConstructor
public class ExecutionSessionService {
    private  List<WebSocketSession> participants = new ArrayList<>();
    private  List<WebSocketSession> hosts = new ArrayList<>();

    public void addParticipant(WebSocketSession session) {
        if(participants.contains(session)) {
            throw  new EntityAlreadyExists("Participant already exists");
        }
        participants.add(session);
    }

    public void addHost(WebSocketSession session) {
        if(hosts.contains(session)) {
            throw  new EntityAlreadyExists("Host already exists");
        }
        hosts.add(session);
    }
    public void removeParticipant(WebSocketSession session) {
        participants.remove(session);
    }
    public void removeHost(WebSocketSession session) {
        hosts.remove(session);
    }
    public int getParticipantsCount() {
        return participants.size();
    }

    public void broadcastMessage(String message) {

    }
    public void sendMessageToSession(WebSocketSession session, String name, Object dto ) throws Exception {
        WebSocketResponseHandlerDTO webSocketResponseHandlerDTO = new WebSocketResponseHandlerDTO(name, dto);
        String message = (new ObjectMapper()).writeValueAsString(webSocketResponseHandlerDTO);
        sendMessage(session, message);
    }

    // Méthode pour envoyer un message texte à une session WebSocket
    private void sendMessage(WebSocketSession session, String message) throws Exception {
        log.info("Sending message to {}: {}", session.getId(), message);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    public void broadcastMessageToHost(List<WebSocketSession> hosts, String name, Object dto) throws Exception {
        for (WebSocketSession session : hosts) {
            sendMessageToSession(session, name, dto);
        }
    }
    public void broadcastMessageToParticipants(List<WebSocketSession> participants, String name, Object dto) throws Exception {
        for (WebSocketSession session : participants) {
            sendMessageToSession(session, name, dto);
        }
    }

}
