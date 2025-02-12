package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn("user123");
    }

    @Test
    void testCreateQuiz() {
        // Arrange
        Quiz quiz = new Quiz();
        quiz.setTitle("My title");
        quiz.setDescription("My description");

        Quiz savedQuiz = new Quiz();
        savedQuiz.setQuizId(1L);
        savedQuiz.setTitle("My title");
        savedQuiz.setDescription("My description");
        savedQuiz.setUser(User.builder()
                        .username("username")
                        .email("email@gmail.com")
                .build());

        when(quizService.saveQuiz(any(Quiz.class))).thenReturn(savedQuiz);

        // Act
        ResponseEntity<Void> response = quizController.createQuiz(quiz, jwt);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/quiz/1"));

        verify(quizService, times(1)).saveQuiz(any(Quiz.class));
    }
}
