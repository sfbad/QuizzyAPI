package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.dtos.AddNewQuestionDTO;
import com.teamcocoon.QuizzyAPI.dtos.AnswersDTO;
import com.teamcocoon.QuizzyAPI.dtos.QuizDto;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import com.teamcocoon.QuizzyAPI.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = QuizzyApiApplication.class
)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class QuizControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private Jwt jwt;
    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void createQuiz_returns201WithLocation() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "New quizz1");
        UserRequestDto user = new UserRequestDto("Test");
        mockMvc.perform(post("/api/users")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)));

        mockMvc.perform(post("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz)))
                .andExpect(status().isCreated());
    }

    @Test
    void createQuiz_returns201WithLocation2() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "New quizz2");
        UserUtils.createUserIfNotExists("testUser");

        mockMvc.perform(post("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz)))
                .andExpect(status().isCreated());

    }

    @Test
    void getListQuiz_returnsListOfQuizzes() throws Exception {
        // Arrange: Créer un quiz pour un utilisateur donné
        createQuiz_returns201WithLocation();
        createQuiz_returns201WithLocation2();

        // Act & Assert: Vérifier que l'utilisateur peut récupérer la liste des quiz
        mockMvc.perform(get("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isOk()) // Vérifie que la réponse est 200 OK
                .andExpect(jsonPath("$.data").isArray()) // Vérifie que la réponse contient un tableau JSON
                .andExpect(jsonPath("$.data[0].title").value("New quizz1")) // Vérifie que le quiz attendu est dans la réponse
                .andExpect(jsonPath("$.data[1].title").value("New quizz2"));
    }

}
