package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.HostDetailsDTO;
import com.teamcocoon.QuizzyAPI.service.ParticipantService;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final QuizService quizService;
    private final ParticipantService participantService;

    @Autowired
    public WebSocketController(QuizService quizService, ParticipantService participantService) {
        this.quizService = quizService;
        this.participantService = participantService;
    }
//
//    @MessageMapping("/host")  // Utilisation de STOMP pour écouter sur "/app/host"
//    @SendTo("/topic/status")  // Envoie un message à "/topic/status"
//    public String handleHostRequest(HostDetailsDTO hostDetailsDTO) {
//        // Traite la logique liée au host ici
//        return "host request processed";
//    }
//
//    @MessageMapping("/join")  // Utilisation de STOMP pour écouter sur "/app/join"
//    @SendTo("/topic/status")  // Envoie un message à "/topic/status"
//    public String handleJoinRequest(HostDetailsDTO hostDetailsDTO) {
//        // Traite la logique liée à la demande de rejoindre ici
//        return "join request processed";
//    }
}
