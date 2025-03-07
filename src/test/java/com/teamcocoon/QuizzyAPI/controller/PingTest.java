package com.teamcocoon.QuizzyAPI.controller;  // Remplacez par votre package réel

import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.controller.Ping;
import com.teamcocoon.QuizzyAPI.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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
class PingControllerTest {

    private static final String BASE_URL = "/api/ping";

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void ping_whenApplicationIsHealthy_returns200WithOkStatus() throws Exception {
        // Loggez des informations utiles pour le débogage
        log.info("Exécution du test de ping en état normal");

        // Exécuter la requête GET vers /api/ping
        TestUtils.Response<Ping.PingResponse> response = TestUtils.performGetRequest(BASE_URL, Ping.PingResponse.class);

        // Vérifier le statut HTTP
        assertEquals(200, response.status(), "Le statut HTTP doit être 200 OK");

        // Vérifier le corps de la réponse
        assertNotNull(response.body(), "Le corps de la réponse ne doit pas être null");
        assertEquals("OK", response.body().getStatus(), "Le statut dans la réponse doit être 'OK'");

        // Vérifier les détails
        assertNotNull(response.body().getDetails(), "Les détails ne doivent pas être null");
        assertEquals("OK", response.body().getDetails().getDatabase(), "Le statut de la base de données doit être 'OK'");

        log.info("Test de ping en état normal réussi");
    }

    @Test
    void ping_whenApplicationIsUnhealthy_returns500WithKoStatus() throws Exception {
        // Loggez des informations utiles pour le débogage
        log.info("Exécution du test de ping en état d'erreur");

        // Obtenir une référence au contrôleur Ping
        Ping pingController = applicationContext.getBean(Ping.class);

        // Forcer une erreur
        pingController.setForceError(true);
        log.debug("Mode d'erreur forcée activé");

        try {
            // Exécuter la requête GET vers /api/ping
            TestUtils.Response<Ping.PingResponse> response = TestUtils.performGetRequest(BASE_URL, Ping.PingResponse.class);

            // Vérifier le statut HTTP
            assertEquals(500, response.status(), "Le statut HTTP doit être 500 Internal Server Error");

            // Vérifier le corps de la réponse
            assertNotNull(response.body(), "Le corps de la réponse ne doit pas être null");
            assertEquals("KO", response.body().getStatus(), "Le statut dans la réponse doit être 'KO'");

            // Vérifier les détails
            assertNotNull(response.body().getDetails(), "Les détails ne doivent pas être null");
            assertEquals("KO", response.body().getDetails().getDatabase(), "Le statut de la base de données doit être 'KO'");

            log.info("Test de ping en état d'erreur réussi");
        } finally {
            // Réinitialiser l'état pour ne pas affecter d'autres tests
            pingController.setForceError(false);
            log.debug("Mode d'erreur forcée désactivé");
        }
    }

}