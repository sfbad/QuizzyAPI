package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.teamcocoon.QuizzyAPI.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<AnswersDTO> answers = new ArrayList<>();
    private AnswersDTO answersDTO1 = new AnswersDTO("Paris", true);
    private AnswersDTO answersDTO2 = new AnswersDTO("Italie", false);
    private final String BASE_URL = "/api/quiz";


    @Test
    void createQuiz_returns201WithLocation() throws Exception {
        QuizDto quiz = new QuizDto(1L, "New quizz1");
        TestUtils.createUserIfNotExists("testUser");

        // Créer un quiz avec le méthode utilitaire
        Response<Void> response = TestUtils.performPostRequest(BASE_URL,quiz, Void.class);

        assertEquals(201, response.status(), "Le statut doit être 201");
        String location = response.headers().get(HttpHeaders.LOCATION);
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");
    }

    @Test
    void getListQuiz_returnsListOfQuizzes() throws Exception {
        createQuiz_returns201WithLocation();
        createQuiz_returns201WithLocation();

        Response<ListQuizResponseDto> response = TestUtils.performGetRequest(BASE_URL, ListQuizResponseDto.class);
        ListQuizResponseDto listQuiz = response.body();

        assertNotNull(listQuiz, "La réponse ne doit pas être nulle");
        assertNotNull(listQuiz.data(), "La liste des quizzes ne doit pas être nulle");
        assertFalse(listQuiz.data().isEmpty(), "La liste des quizzes ne doit pas être vide");
        assertEquals("New quizz1", listQuiz.data().get(0).title(), "Le titre du premier quiz doit être 'New quizz1'");
        assertEquals("New quizz1", listQuiz.data().get(1).title(), "Le titre du 2eme quiz doit etre 'New quizz1'");

    }


    @Test
    void addNewQuestion_ShouldReturn_LocationUrl_For_The_CreatedQuestion() throws Exception {
        createQuiz_returns201WithLocation();  // Créer un quiz d'abord si nécessaire
        answers.add(answersDTO1);
        answers.add(answersDTO2);

        AddNewQuestionDTO question = new AddNewQuestionDTO("Quelle est la capitale de la France?", answers);

        Response<AddNewQuestionDTO> response = performPostRequest(
                BASE_URL + "/1/questions", question, AddNewQuestionDTO.class);

        assertEquals(201, response.status(), "Le statut doit être 201");
        String location = response.headers().get("Location");
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");
        assertTrue(location.matches("http://localhost/api/quiz/1/questions/\\d+"),
                "L'URL Location a un format invalide.");
    }


    @Test
    void getQuizById() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Sample Quiz");
        TestUtils.createUserIfNotExists("testUser");

        // Créer un quiz via la méthode performRequest
        Response<QuizDto> createResponse = performPostRequest(
                BASE_URL, quiz, QuizDto.class);

        // Vérifier la réponse de la création du quiz
        assertEquals(201, createResponse.status(), "Le statut de la création doit être 201");
        String location = createResponse.headers().get("Location");
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");

        // Extraire l'ID du quiz depuis l'URL "Location"
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // Effectuer la requête GET pour récupérer le quiz par ID via performRequest
        Response<QuizResponseDto> getResponse = performGetRequest(
                BASE_URL + "/" + quizId, QuizResponseDto.class);

        // Vérifier les valeurs retournées par la requête GET
        QuizResponseDto retrievedQuiz = getResponse.body();
        assertEquals("Sample Quiz", retrievedQuiz.title(), "Le titre du quiz doit être 'Sample Quiz'.");
        assertNull(retrievedQuiz.description(), "La description du quiz ne doit pas exister.");
        assertNotNull(retrievedQuiz.questions(), "La liste des questions du quiz doit être présente.");
    }

    @Test
    void updateQuizTitle() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Old title");
        TestUtils.createUserIfNotExists("testUser");

        // Créer un quiz via la méthode performRequest
        Response<QuizDto> createResponse = performPostRequest(
                BASE_URL, quiz, QuizDto.class);

        // Vérifier la réponse de la création du quiz
        assertEquals(201, createResponse.status(), "Le statut de la création doit être 201");
        String location = createResponse.headers().get("Location");
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");

        // Extraire l'ID du quiz depuis l'URL "Location"
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // Créer une demande de mise à jour de titre
        PatchQuizTitleRequestDTO patchRequest = PatchQuizTitleRequestDTO.builder()
                .op("replace")
                .path("/title")
                .value("New Title")
                .build();

        // Effectuer le PATCH via performRequest
        Response<Void> patchResponse = performPatchRequest(
                BASE_URL + "/" + quizId, patchRequest,Void.class);

        // Vérifier que le statut de la réponse est "No Content" (204)
        assertEquals(204, patchResponse.status(), "Le statut du PATCH doit être 204");

        // Récupérer le quiz après la mise à jour
        Response<QuizResponseDto> getResponse = performGetRequest(
                BASE_URL + "/" + quizId, QuizResponseDto.class);

        // Vérifier la mise à jour du titre
        QuizResponseDto updatedQuiz = getResponse.body();
        assertEquals("New Title", updatedQuiz.title(), "Le titre du quiz doit être 'New Title'.");
    }
}


