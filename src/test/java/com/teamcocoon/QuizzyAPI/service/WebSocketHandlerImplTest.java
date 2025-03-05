package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.ExecutionIDDTO;
import com.teamcocoon.QuizzyAPI.dtos.HostDetailsDTO;
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
    private WebSocketResponseHandlerDTO response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(WebSocketSession.class);
        executionIDDTO = new ExecutionIDDTO("ABCEDEF");
        requestHost = new HostDetailsDTO("host",executionIDDTO);
        requestHost = new HostDetailsDTO("Join",executionIDDTO);

    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        webSocketHandler.afterConnectionEstablished(mockSession);
        verify(mockSession, times(1)).getId();
    }

    @Test
    void testHandleJoinMessage() throws Exception {

        Quiz mockQuiz = new Quiz();
        mockQuiz.setTitle("Sample Quiz");
        webSocketHandler.sendMessage(mockSession,requestJoin.toString());

        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleHostMessage() throws Exception {
        // Mock the HostDetailsDTO for host request

        // Mock the quiz service to return a dummy quiz
        Quiz mockQuiz = new Quiz();
        mockQuiz.setTitle("Sample Quiz");
        when(quizService.getQuizByQuizCode("executionId456")).thenReturn(mockQuiz);

        // Send a host message to the handler
        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Handle the message
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Verify that the sendMessage method is called
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastStatusUpdate() throws Exception {
        WebSocketSession participantSession = mock(WebSocketSession.class);
        when(participantSession.isOpen()).thenReturn(true);

        webSocketHandler.addParticipant(executionIDDTO.executionId(), participantSession);


        webSocketHandler.broadcastStatusUpdate(executionIDDTO.executionId(), "Status update message");

        // Verify that the sendMessage method is called
        verify(participantSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSendMessageToSession() throws Exception {
        // Create a WebSocketResponseHandlerDTO
        WebSocketResponseHandlerDTO responseHandlerDTO = new WebSocketResponseHandlerDTO("type", "message");

        // Call the method to send a message
        webSocketHandler.sendMessageToSession(mockSession, responseHandlerDTO, "type");

        // Verify that sendMessage was called
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }
    @Test
    void testBroadcastStatusUpdate_whenParticipantIsClosed() throws Exception {
        WebSocketSession participantSession = mock(WebSocketSession.class);
        when(participantSession.isOpen()).thenReturn(false);  // Participant fermé

        webSocketHandler.addParticipant(executionIDDTO.executionId(), participantSession);

        webSocketHandler.broadcastStatusUpdate(executionIDDTO.executionId(), "Status update message");

        // Vérifier que sendMessage n'a pas été appelé, car la session est fermée
        verify(participantSession, never()).sendMessage(any(TextMessage.class));
    }
    @Test
    void testHandleJoinMessage_whenServiceFails() throws Exception {
        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenThrow(new RuntimeException("Quiz not found"));

        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Tester si une exception est correctement gérée sans crash
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Vérification que la méthode sendMessage n'a pas été appelée (erreur dans le service)
        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }
    @Test
    void testBroadcastStatusUpdate_multipleParticipants() throws Exception {
        WebSocketSession participantSession1 = mock(WebSocketSession.class);
        WebSocketSession participantSession2 = mock(WebSocketSession.class);
        when(participantSession1.isOpen()).thenReturn(true);
        when(participantSession2.isOpen()).thenReturn(true);

        // Ajout de participants
        webSocketHandler.addParticipant(executionIDDTO.executionId(), participantSession1);
        webSocketHandler.addParticipant(executionIDDTO.executionId(), participantSession2);

        webSocketHandler.broadcastStatusUpdate(executionIDDTO.executionId(), "Status update message");

        // Vérifier que chaque participant a bien reçu le message
        verify(participantSession1, times(1)).sendMessage(any(TextMessage.class));
        verify(participantSession2, times(1)).sendMessage(any(TextMessage.class));
    }
    @Test
    void testHandleJoinMessage_structureOfSentMessage() throws Exception {
        Quiz mockQuiz = new Quiz();
        mockQuiz.setTitle("Sample Quiz");
        when(quizService.getQuizByQuizCode(executionIDDTO.executionId())).thenReturn(mockQuiz);

        String jsonPayload = objectMapper.writeValueAsString(requestHost);
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Appel du handler
        webSocketHandler.handleTextMessage(mockSession, textMessage);

        // Capture du message envoyé
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, times(1)).sendMessage(captor.capture());

        // Vérification du contenu du message envoyé
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
