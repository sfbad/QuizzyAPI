package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private  List<AnswersDTO>  answers = new ArrayList<>();
    AnswersDTO answersDTO1 = new AnswersDTO("Paris",true);
    AnswersDTO answersDTO2 = new AnswersDTO("Italie",false);

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
        //créer un quiz pour un utilisateur
        createQuiz_returns201WithLocation();
        createQuiz_returns201WithLocation2();

        //vérifier que l'utilisateur peut récupérer la liste des quiz
        mockMvc.perform(get("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("New quizz1"))
                .andExpect(jsonPath("$.data[1].title").value("New quizz2"));
    }

    @Test
    void addNewQuestion_ShouldReturn_LocationUrl_For_The_CreatedQuestion() throws Exception {
        createQuiz_returns201WithLocation();
        answers.add(answersDTO1);
        answers.add(answersDTO2);
        AddNewQuestionDTO question = new AddNewQuestionDTO("Quelle est la capitale de la France?", answers);

        mockMvc.perform(post("/api/quiz/{quizId}/questions", 1L)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/quiz/1/questions/1"));
    }

    @Test
    void getQuiById() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Sample Quiz");
        UserUtils.createUserIfNotExists("testUser");

        MvcResult result = mockMvc.perform(post("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        Long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(get("/api/quiz/{id}", quizId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Quiz"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.questions").isArray());
    }

    @Test
    void updateQuizTitle() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Old title");
        UserUtils.createUserIfNotExists("testUser");

        MvcResult result = mockMvc.perform(post("/api/quiz")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quiz)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        Long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        PatchQuizTitleRequestDTO patchRequest = PatchQuizTitleRequestDTO.builder()
                .op("replace")
                .path("/title")
                .value("New Title")
                .build();

        // effectuer le PATCH
        mockMvc.perform(patch("/api/quiz/{id}", quizId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(patchRequest))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/quiz/{id}", quizId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

}
