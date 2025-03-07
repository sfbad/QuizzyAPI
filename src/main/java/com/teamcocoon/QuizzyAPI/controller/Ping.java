package com.teamcocoon.QuizzyAPI.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ping")
@Tag(name = "Ping", description = "Ping l'API")
public class Ping {

    // Attribut pour simuler une erreur (pour les tests)
    private boolean forceError = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Point de terminaison de vérification de l'état de santé de l'application.
     * Ce contrôleur fournit un mécanisme de ping simple pour :
     * - Vérifier que l'application est en cours d'exécution
     * - Vérifier l'état de base du système
     * - Renvoyer une réponse standardisée indiquant la santé du système
     */
    @Operation(summary = "Ping l'API")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "API est en ligne"),
                    @ApiResponse(responseCode = "500", description = "API est hors ligne")
            }
    )

    // Méthode pour définir l'état de forceError (pour les tests)
    public void setForceError(boolean forceError) {
        this.forceError = forceError;
    }

    @GetMapping
    public ResponseEntity<Object> ping() {
        try {
            // Si forceError est true, simuler une erreur
            if (forceError) {
                throw new RuntimeException("Erreur simulée pour les tests");
            }
            return ResponseEntity.ok().body(new PingResponse("OK", new PingDetails("OK")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PingResponse("KO", new PingDetails("KO")));
        }
    }

    @NoArgsConstructor
    public static class PingResponse {
        private String status;
        private PingDetails details;

        public PingResponse(String status, PingDetails details) {
            this.status = status;
            this.details = details;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public PingDetails getDetails() {
            return details;
        }

        public void setDetails(PingDetails details) {
            this.details = details;
        }
    }

    @NoArgsConstructor
    public static class PingDetails {
        private String database;

        public PingDetails(String database) {
            this.database = database;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }
    }
}
