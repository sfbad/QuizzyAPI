package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.ExecutionIDDTO;
import com.teamcocoon.QuizzyAPI.dtos.HostDetailsDTO;
import com.teamcocoon.QuizzyAPI.dtos.QuizDToRelease;
import com.teamcocoon.QuizzyAPI.dtos.WebSocketResponseHandlerDTO;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketHandlerImplTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private WebSocketHandlerImpl webSocketHandler;

    private WebSocketSession mockSession;
    private ObjectMapper objectMapper;
    private HostDetailsDTO requestHost;
    private HostDetailsDTO requestJoin;
    private ExecutionIDDTO executionIDDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(WebSocketSession.class);
        executionIDDTO = new ExecutionIDDTO("ABCEDEF");
        requestHost = new HostDetailsDTO("host", executionIDDTO);
        requestJoin = new HostDetailsDTO("Join", executionIDDTO);
    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        webSocketHandler.afterConnectionEstablished(mockSession);
        verify(mockSession, times(1)).getId();
    }

    @Test
    void testHandleJoinMessage() throws Exception {
        QuizDToRelease mockQuiz = new QuizDToRelease(1L,"ABCDEF","Sample Quizz","Description");
        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenReturn(mockQuiz);

        // Appel de la méthode handleTextMessage avec un message "join"
        String jsonPayload = objectMapper.writeValueAsString(requestJoin);
        TextMessage textMessage = new TextMessage(jsonPayload);
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Vérification que sendMessage est appelé pour envoyer le message avec les informations du quiz
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleHostMessage() throws Exception {
        QuizDToRelease mockQuiz = new QuizDToRelease(1L,"ABCDEF","Sample Quizz","Description");
        when(quizService.getQuizByQuizCode("executionId456")).thenReturn(mockQuiz);

        // Envoi du message "host"
        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Traitement du message
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Vérification que sendMessage est appelé
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastStatusUpdate() throws Exception {
        WebSocketSession participantSession = mock(WebSocketSession.class);
        when(participantSession.isOpen()).thenReturn(true);

        webSocketHandler.addToRoom(executionIDDTO.executionId(), participantSession,false);

        // Envoi de l'update du statut
       // webSocketHandler.broadcastStatusUpdate(executionIDDTO.executionId(), "Status update message");

        // Vérification que le participant a bien reçu le message
        verify(participantSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSendMessageToSession() throws Exception {
        WebSocketResponseHandlerDTO responseHandlerDTO = new WebSocketResponseHandlerDTO("type", "message");

        // Envoi d'un message à la session
        //webSocketHandler.sendMessageToSession(mockSession, responseHandlerDTO, "type");

        // Vérification que la méthode sendMessage a bien été appelée
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastStatusUpdate_whenParticipantIsClosed() throws Exception {
        WebSocketSession participantSession = mock(WebSocketSession.class);
        when(participantSession.isOpen()).thenReturn(false); // Le participant est fermé

        webSocketHandler.addToRoom(executionIDDTO.executionId(), participantSession,false);

        // Envoi de l'update du statut
       // webSocketHandler.broadcastStatusUpdate(executionIDDTO.executionId(), "Status update message");

        // Vérification que sendMessage n'a pas été appelé
        verify(participantSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleJoinMessage_whenServiceFails() throws Exception {
        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenThrow(new RuntimeException("Quiz not found"));

        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Tester la gestion des erreurs sans que la session ne soit déconnectée
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Vérification que la méthode sendMessage n'a pas été appelée en cas d'erreur
        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastStatusUpdate_multipleParticipants() throws Exception {
        WebSocketSession participantSession1 = mock(WebSocketSession.class);
        WebSocketSession participantSession2 = mock(WebSocketSession.class);
        when(participantSession1.isOpen()).thenReturn(true);
        when(participantSession2.isOpen()).thenReturn(true);

        // Ajout de participants
        webSocketHandler.addToRoom(executionIDDTO.executionId(), participantSession1,false);
        webSocketHandler.addToRoom(executionIDDTO.executionId(), participantSession2,false);

        // Envoi de l'update du statut
       // webSocketHandler.(executionIDDTO.executionId(), "Status update message");

        // Vérification que chaque participant a bien reçu le message
        verify(participantSession1, times(1)).sendMessage(any(TextMessage.class));
        verify(participantSession2, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleJoinMessage_structureOfSentMessage() throws Exception {
        QuizDToRelease mockQuiz = new QuizDToRelease(1L,"ABCDEF","Sample Quizz","Description");

        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenReturn(mockQuiz);

        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Appel du handler
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Capture du message envoyé
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, times(1)).sendMessage(captor.capture());

        // Vérification que le message contient le titre du quiz
        String sentMessage = captor.getValue().getPayload();
        assertTrue(sentMessage.contains("Sample Quiz"), "Le titre du quiz devrait être 'Sample Quiz'");
    }

    @Test
    void testHandleJoinMessage_withInvalidQuiz() throws Exception {
        // Cas où le quiz n'est pas trouvé
        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenReturn(null);

        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Quand le quiz n'est pas trouvé, vérifier qu'aucun message n'est envoyé
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }
}
