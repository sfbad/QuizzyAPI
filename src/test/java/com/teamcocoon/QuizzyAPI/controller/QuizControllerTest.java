package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.AddNewQuestionDTO;
import com.teamcocoon.QuizzyAPI.dtos.AnswersDTO;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(QuizController.class)
@ExtendWith(MockitoExtension.class)
class QuizControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private UserRepository userRepository;

    private Jwt jwt;
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

   private  List<AnswersDTO>  answers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);
        objectMapper = new ObjectMapper();
        AnswersDTO answersDTO1 = new AnswersDTO("ok",true);
        AnswersDTO answersDTO2 = new AnswersDTO("Italie",false);
        answers.add(answersDTO1);
        answers.add(answersDTO2);
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


        verify(quizService, times(1)).saveQuiz(any(Quiz.class));
    }

    @Test
    void addNewQuestion_WithMockMvc() throws Exception {
        Long quizId = 1L;
        AddNewQuestionDTO question = new AddNewQuestionDTO("Test", answers);
        Long expectedQuestionId = 10L;

        when(quizService.addQuestionToQuiz(eq(quizId), any(AddNewQuestionDTO.class)))
                .thenReturn(expectedQuestionId);


        mockMvc.perform(post("/api/quiz/{quizId}/questions", quizId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                        .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/quiz/1/questions/10"));

        verify(quizService, times(1)).addQuestionToQuiz(eq(quizId), any(AddNewQuestionDTO.class));
    }



}
