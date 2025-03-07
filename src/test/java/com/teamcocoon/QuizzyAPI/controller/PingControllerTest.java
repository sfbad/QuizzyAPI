package com.teamcocoon.QuizzyAPI.controller;  // Remplacez par votre package réel

import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = QuizzyApiApplication.class  // Remplacez par votre classe principale
)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

class PingControllerTest {

    private static final String BASE_URL = "/api/ping";

    @Autowired
    private ApplicationContext applicationContext;


    @Test
    void ping_whenApplicationIsHealthy_returns200WithOkStatus() throws Exception {
        log.info("Exécution du test de ping en état normal");

        TestUtils.Response<Ping.PingResponse> response = TestUtils.performGetRequest(BASE_URL, Ping.PingResponse.class);

        assertEquals(200, response.status(), "Le statut HTTP doit être 200 OK");

        assertNotNull(response.body(), "Le corps de la réponse ne doit pas être null");
        assertEquals("OK", response.body().getStatus(), "Le statut dans la réponse doit être 'OK'");

        assertNotNull(response.body().getDetails(), "Les détails ne doivent pas être null");
        assertEquals("OK", response.body().getDetails().getDatabase(), "Le statut de la base de données doit être 'OK'");

        log.info("Test de ping en état normal réussi");
    }

    @Test
    void ping_whenApplicationIsUnhealthy_returns500WithKoStatus() throws Exception {
        log.info("Exécution du test de ping en état d'erreur");

        Ping pingController = applicationContext.getBean(Ping.class);

        pingController.setForceError(true);

        try {
            TestUtils.Response<Ping.PingResponse> response = TestUtils.performGetRequest(BASE_URL, Ping.PingResponse.class);

            assertEquals(500, response.status(), "Le statut HTTP doit être 500 Internal Server Error");

            assertNotNull(response.body(), "Le corps de la réponse ne doit pas être null");
            assertEquals("KO", response.body().getStatus(), "Le statut dans la réponse doit être 'KO'");

            assertNotNull(response.body().getDetails(), "Les détails ne doivent pas être null");
            assertEquals("KO", response.body().getDetails().getDatabase(), "Le statut de la base de données doit être 'KO'");

            log.info("Test de ping en état d'erreur réussi");
        } finally {
            pingController.setForceError(false);
            log.debug("Mode d'erreur forcée désactivé");
        }
    }

}