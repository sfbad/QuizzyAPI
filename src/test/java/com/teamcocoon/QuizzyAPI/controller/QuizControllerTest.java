package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static com.teamcocoon.QuizzyAPI.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = QuizzyApiApplication.class
)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@TestPropertySource("classpath:application-test.properties")
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<AnswersDTO> answers = new ArrayList<>();
    private AnswersDTO answersDTO1 = new AnswersDTO("Paris", true);
    private AnswersDTO answersDTO2 = new AnswersDTO("Italie", false);
    private final String BASE_URL = "/api/quiz";


    @BeforeEach
    void setUp() throws Exception {
        // Initialisation des données de test
        TestUtils.createUserIfNotExists("testUser");

        // Création d'un quiz basic
        QuizDto quiz = new QuizDto(1L, "New quizz1", "description1", Map.of());

        // Réinitialisation des réponses pour les questions
        answers = new ArrayList<>();
        answersDTO1 = new AnswersDTO("Paris", true);
        answersDTO2 = new AnswersDTO("Lyon", false);
    }

    @Test
    void createQuiz_returns201WithLocation() throws Exception {
        QuizDto quiz = new QuizDto(1L, "New quizz1", "description1", Map.of());
        // Créer un utilisateur si non existant
        TestUtils.createUserIfNotExists("testUser");

        // Créer un quiz avec la méthode utilitaire
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
        createQuiz_returns201WithLocation();
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
    void addNewQuestion_ShouldReturn_Exeption_Like_ThisQuiz_Doesnt_Exist() throws Exception {
        createQuiz_returns201WithLocation();
        answers.add(answersDTO1);
        answers.add(answersDTO2);

        AddNewQuestionDTO question = new AddNewQuestionDTO("Quelle est la capitale de la France?", answers);

        Response<ExceptionsResponseDTO> response = performPostRequest(
                BASE_URL + "/2/questions", question, ExceptionsResponseDTO.class);

        assertEquals(404, response.status(), "Le statut doit être 404");
        String location = response.headers().get("Location");
        assertNull(location, "L'URL Location  doit  être nulle.");
        assertEquals("Ce quizz n'existe pas !!", response.body().message(), "Le message d'erreur doit être 'Ce quizz n'existe pas !!'");

    }


    @Test
    void getQuizById() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Sample Quiz", "description2", Map.of());
        TestUtils.createUserIfNotExists("testUser");

        Response<QuizDto> createResponse = performPostRequest(
                BASE_URL, quiz, QuizDto.class);

        assertEquals(201, createResponse.status(), "Le statut de la création doit être 201");
        String location = createResponse.headers().get("Location");
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");

        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        Response<QuizResponseDto> getResponse = performGetRequest(
                BASE_URL + "/" + quizId, QuizResponseDto.class);

        QuizResponseDto retrievedQuiz = getResponse.body();
        assertEquals("Sample Quiz", retrievedQuiz.title(), "Le titre du quiz doit être 'Sample Quiz'.");
        assertNull(retrievedQuiz.description(), "La description du quiz ne doit pas exister.");
        assertNotNull(retrievedQuiz.questions(), "La liste des questions du quiz doit être présente.");
    }

    @Test
    void updateQuizTitle() throws Exception {
        QuizDto quiz = new QuizDto(-1L, "Old title", "description3", Map.of());
        TestUtils.createUserIfNotExists("testUser");

        Response<QuizDto> createResponse = performPostRequest(
                BASE_URL, quiz, QuizDto.class);

        assertEquals(201, createResponse.status(), "Le statut de la création doit être 201");
        String location = createResponse.headers().get("Location");
        assertNotNull(location, "L'URL Location ne doit pas être nulle.");

        // Extraire l'ID du quiz depuis l'URL "Location"
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        PatchQuizTitleRequestDTO patchRequest = PatchQuizTitleRequestDTO.builder()
                .op("replace")
                .path("/title")
                .value("New Title")
                .build();

        Response<Void> patchResponse = performPatchRequest(
                BASE_URL + "/" + quizId, patchRequest,Void.class);

        assertEquals(204, patchResponse.status(), "Le statut du PATCH doit être 204");

        Response<QuizResponseDto> getResponse = performGetRequest(
                BASE_URL + "/" + quizId, QuizResponseDto.class);

        QuizResponseDto updatedQuiz = getResponse.body();
        assertEquals("New Title", updatedQuiz.title(), "Le titre du quiz doit être 'New Title'.");
    }

    @Test
    void getListQuiz_returnsEmptyListWhenNoQuizzesExist() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Perform GET request
        Response<ListQuizResponseLinkDto> response = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);
        log.info(response.toString());
        // Verify response
        assertEquals(200, response.status(), "Status should be 200 OK");
        assertNotNull(response.body(), "Response body should not be null");
        assertTrue(response.body().data().isEmpty(), "Quiz list should be empty");
        assertNotNull(response.body()._links(), "Links map should not be null");
        assertTrue(response.body()._links().containsKey("create"), "Should contain create link");
    }

    @Test
    void getListQuiz_returnsCorrectQuizzesList() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Create multiple quizzes
        QuizDto quiz1 = new QuizDto(null, "Quiz 1", "Description 1", Map.of());
        QuizDto quiz2 = new QuizDto(null, "Quiz 2", "Description 2", Map.of());

        // Create first quiz
        Response<QuizDto> createResponse1 = performPostRequest(
                BASE_URL, quiz1, QuizDto.class);
        assertEquals(201, createResponse1.status(), "First quiz creation should return 201");

        // Create second quiz
        Response<QuizDto> createResponse2 = performPostRequest(
                BASE_URL, quiz2, QuizDto.class);
        assertEquals(201, createResponse2.status(), "Second quiz creation should return 201");

        // Perform GET request
        Response<ListQuizResponseLinkDto> response = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        // Verify response
        assertEquals(200, response.status(), "Status should be 200 OK");
        assertNotNull(response.body(), "Response body should not be null");
        assertEquals(2, response.body().data().size(), "Should return 2 quizzes");

        // Verify quiz details
        assertTrue(response.body().data().stream()
                        .anyMatch(quiz -> "Quiz 1".equals(quiz.title())),
                "First quiz title should match");
        assertTrue(response.body().data().stream()
                        .anyMatch(quiz -> "Quiz 2".equals(quiz.title())),
                "Second quiz title should match");

        // Verify links
        assertNotNull(response.body()._links(), "Links map should not be null");
        assertTrue(response.body()._links().containsKey("create"), "Should contain create link");
        assertEquals("/api/quiz", response.body()._links().get("create"), "Create link should be correct");
    }

    @Test
    void getListQuiz_returnsListWithLinks() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Create a quiz
        QuizDto quiz = new QuizDto(null, "Test Quiz", "Test Description", Map.of());
        performPostRequest(BASE_URL, quiz, QuizDto.class);

        // Perform GET request
        Response<ListQuizResponseLinkDto> response = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        // Verify response
        assertEquals(200, response.status(), "Status should be 200 OK");
        assertNotNull(response.body(), "Response body should not be null");
        assertNotNull(response.body()._links(), "Links map should not be null");

        // Verify specific links
        assertTrue(response.body()._links().containsKey("create"), "Should contain create link");
        assertEquals("/api/quiz", response.body()._links().get("create"), "Create link should match expected URL");
    }

    @Test
    void getQuizById_ShouldReturn404_WhenQuizDoesNotExist() throws Exception {
        // Appel de l'API pour récupérer un quiz qui n'existe pas
        Response<ExceptionsResponseDTO> response = TestUtils.performGetRequest(
                BASE_URL + "/999", ExceptionsResponseDTO.class);

        // Vérification du statut et du message d'erreur
        assertEquals(404, response.status(), "Le statut doit être 404");
        assertEquals("Quiz not found", response.body().message(), "Le message d'erreur doit être 'Quiz not found'");
    }

/*    @Test
    void getListQuiz_returnsStartLinkForStartableQuizzes() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Create a quiz that should be startable
        QuizDto quiz = new QuizDto(null, "Startable Quiz", "Test Description");
        Response<QuizDto> createResponse = performPostRequest(BASE_URL, quiz, QuizDto.class);
        assertEquals(201, createResponse.status(), "Quiz creation should return 201");

        // Extract the quiz ID from the Location header
        String location = createResponse.headers().get("Location");
        assertNotNull(location, "Location URL should not be null");
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // Add valid questions with correct answers to make the quiz startable
        answers.clear();
        answers.add(new AnswersDTO("Paris", true));
        answers.add(new AnswersDTO("Lyon", false));

        AddNewQuestionDTO question = new AddNewQuestionDTO("Quelle est la capitale de la France?", answers);
        Response<AddNewQuestionDTO> questionResponse = performPostRequest(
                BASE_URL + "/" + quizId + "/questions", question, AddNewQuestionDTO.class);
        assertEquals(201, questionResponse.status(), "Question creation should return 201");

        // Perform GET request to retrieve the list of quizzes
        Response<ListQuizResponseLinkDto> response = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        // Verify response
        assertEquals(200, response.status(), "Status should be 200 OK");
        assertNotNull(response.body(), "Response body should not be null");
        assertFalse(response.body().data().isEmpty(), "Quiz list should not be empty");

        // Find the created quiz in the response
        Optional<QuizLinkDto> startableQuiz = response.body().data().stream()
                .filter(q -> q.title().equals("Startable Quiz"))
                .findFirst();

        assertTrue(startableQuiz.isPresent(), "Startable quiz should be in the response");
        assertNotNull(startableQuiz.get()._links(), "Quiz links should not be null");
        assertTrue(startableQuiz.get()._links().containsKey("start"),
                "Startable quiz should have a 'start' link");
        assertEquals("/api/quiz/" + quizId + "/start", startableQuiz.get()._links().get("start"),
                "Start link should match expected URL");
    }
*/
/*    @Test
    void getListQuiz_doesNotReturnStartLinkForNonStartableQuizzes() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Create a quiz with no questions (non-startable)
        QuizDto emptyQuiz = new QuizDto(null, "Empty Quiz", "No Questions");
        Response<QuizDto> createResponse = performPostRequest(BASE_URL, emptyQuiz, QuizDto.class);
        assertEquals(201, createResponse.status(), "Quiz creation should return 201");

        // Create a quiz with invalid questions (no correct answer)
        QuizDto invalidQuiz = new QuizDto(null, "Invalid Quiz", "Invalid Questions");
        Response<QuizDto> createInvalidResponse = performPostRequest(BASE_URL, invalidQuiz, QuizDto.class);
        assertEquals(201, createInvalidResponse.status(), "Quiz creation should return 201");

        // Extract the quiz ID from the Location header
        String location = createInvalidResponse.headers().get("Location");
        assertNotNull(location, "Location URL should not be null");
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // Add invalid question with no correct answer
        List<AnswersDTO> invalidAnswers = new ArrayList<>();
        invalidAnswers.add(new AnswersDTO("Wrong Answer 1", false));
        invalidAnswers.add(new AnswersDTO("Wrong Answer 2", false));

        AddNewQuestionDTO invalidQuestion = new AddNewQuestionDTO("Invalid Question", invalidAnswers);
        Response<AddNewQuestionDTO> questionResponse = performPostRequest(
                BASE_URL + "/" + quizId + "/questions", invalidQuestion, AddNewQuestionDTO.class);
        assertEquals(201, questionResponse.status(), "Question creation should return 201");

        // Perform GET request to retrieve the list of quizzes
        Response<ListQuizResponseLinkDto> response = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        // Verify response
        assertEquals(200, response.status(), "Status should be 200 OK");

        // Check the empty quiz doesn't have a start link
        Optional<QuizLinkDto> emptyQuizResult = response.body().data().stream()
                .filter(q -> q.title().equals("Empty Quiz"))
                .findFirst();

        assertTrue(emptyQuizResult.isPresent(), "Empty quiz should be in the response");
        assertTrue(!emptyQuizResult.get()._links().containsKey("start") ||
                        emptyQuizResult.get()._links().get("start") == null,
                "Non-startable quiz should not have a 'start' link");

        // Check the invalid quiz doesn't have a start link
        Optional<QuizLinkDto> invalidQuizResult = response.body().data().stream()
                .filter(q -> q.title().equals("Invalid Quiz"))
                .findFirst();

        assertTrue(invalidQuizResult.isPresent(), "Invalid quiz should be in the response");
        assertTrue(!invalidQuizResult.get()._links().containsKey("start") ||
                        invalidQuizResult.get()._links().get("start") == null,
                "Non-startable quiz should not have a 'start' link");
    }
*/
/*    @Test
    void getListQuiz_checksAllConditionsForStartableQuiz() throws Exception {
        // Ensure user exists
        TestUtils.createUserIfNotExists("testUser");

        // Create a quiz
        QuizDto quiz = new QuizDto(null, "Condition Test Quiz", "Testing all conditions");
        Response<QuizDto> createResponse = performPostRequest(BASE_URL, quiz, QuizDto.class);
        String location = createResponse.headers().get("Location");
        long quizId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // 1. Test with empty title
        PatchQuizTitleRequestDTO emptyTitlePatch = PatchQuizTitleRequestDTO.builder()
                .op("replace")
                .path("/title")
                .value("")
                .build();

        performPatchRequest(BASE_URL + "/" + quizId, emptyTitlePatch, Void.class);

        // Add valid question
        answers.clear();
        answers.add(new AnswersDTO("Correct Answer", true));
        answers.add(new AnswersDTO("Wrong Answer", false));

        AddNewQuestionDTO question = new AddNewQuestionDTO("Valid Question", answers);
        performPostRequest(BASE_URL + "/" + quizId + "/questions", question, AddNewQuestionDTO.class);

        // Check that quiz with empty title is not startable
        Response<ListQuizResponseLinkDto> response1 = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        Optional<QuizLinkDto> emptyTitleQuiz = response1.body().data().stream()
                .filter(q -> q.id() == quizId)
                .findFirst();

        assertTrue(emptyTitleQuiz.isPresent(), "Quiz should be in response");
        assertTrue(!emptyTitleQuiz.get()._links().containsKey("start") ||
                        emptyTitleQuiz.get()._links().get("start") == null,
                "Quiz with empty title should not have start link");

        // 2. Restore title and test with multiple correct answers
        PatchQuizTitleRequestDTO validTitlePatch = PatchQuizTitleRequestDTO.builder()
                .op("replace")
                .path("/title")
                .value("Valid Title Quiz")
                .build();

        performPatchRequest(BASE_URL + "/" + quizId, validTitlePatch, Void.class);

        // Add question with multiple correct answers
        List<AnswersDTO> multipleCorrectAnswers = new ArrayList<>();
        multipleCorrectAnswers.add(new AnswersDTO("Correct Answer 1", true));
        multipleCorrectAnswers.add(new AnswersDTO("Correct Answer 2", true));

        AddNewQuestionDTO invalidQuestion = new AddNewQuestionDTO("Multiple Correct Answers", multipleCorrectAnswers);
        performPostRequest(BASE_URL + "/" + quizId + "/questions", invalidQuestion, AddNewQuestionDTO.class);

        // Check that quiz with multiple correct answers is not startable
        Response<ListQuizResponseLinkDto> response2 = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        Optional<QuizLinkDto> multipleCorrectQuiz = response2.body().data().stream()
                .filter(q -> q.id() == quizId)
                .findFirst();

        assertTrue(multipleCorrectQuiz.isPresent(), "Quiz should be in response");
        assertTrue(!multipleCorrectQuiz.get()._links().containsKey("start") ||
                        multipleCorrectQuiz.get()._links().get("start") == null,
                "Quiz with multiple correct answers should not have start link");

        // 3. Create a fully valid quiz and check it has a start link
        QuizDto validQuiz = new QuizDto(null, "Valid Quiz", "This quiz meets all conditions");
        Response<QuizDto> validCreateResponse = performPostRequest(BASE_URL, validQuiz, QuizDto.class);
        String validLocation = validCreateResponse.headers().get("Location");
        long validQuizId = Long.parseLong(validLocation.substring(validLocation.lastIndexOf("/") + 1));

        // Add valid question
        List<AnswersDTO> validAnswers = new ArrayList<>();
        validAnswers.add(new AnswersDTO("Correct Answer", true));
        validAnswers.add(new AnswersDTO("Wrong Answer 1", false));
        validAnswers.add(new AnswersDTO("Wrong Answer 2", false));

        AddNewQuestionDTO validQuestionDto = new AddNewQuestionDTO("Valid Question", validAnswers);
        performPostRequest(BASE_URL + "/" + validQuizId + "/questions", validQuestionDto, AddNewQuestionDTO.class);

        // Check that valid quiz is startable
        Response<ListQuizResponseLinkDto> response3 = performGetRequest(
                BASE_URL, ListQuizResponseLinkDto.class);

        Optional<QuizLinkDto> validQuizResult = response3.body().data().stream()
                .filter(q -> q.title().equals("Valid Quiz"))
                .findFirst();

        assertTrue(validQuizResult.isPresent(), "Valid quiz should be in response");
        assertTrue(validQuizResult.get()._links().containsKey("start"),
                "Valid quiz should have a start link");
        assertEquals("/api/quiz/" + validQuizId + "/start", validQuizResult.get()._links().get("start"),
                "Start link should match expected URL");
    }
*/
}


