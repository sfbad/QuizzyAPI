package com.teamcocoon.QuizzyAPI.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ping")
@Tag(name = "Ping", description = "Ping l'API")
public class Ping {


    /**
     * Point de terminaison de vérification de l'état de santé de l'application.
     *
     * Ce contrôleur fournit un mécanisme de ping simple pour :
     * - Vérifier que l'application est en cours d'exécution
     * - Vérifier l'état de base du système
     * - Renvoyer une réponse standardisée indiquant la santé du système
     *
     * Point de terminaison : GET /api/ping
     * - Renvoie toujours un HTTP 200 avec un statut "OK" ou "KO"
     * - Inclut des détails de base du système (actuellement l'état de la base de données)
     * - Aide à la surveillance et aux diagnostics rapides de l'application
     */
    @Operation(summary = "Ping l'API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API est en ligne",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PingResponse.class),
                            examples = @ExampleObject(name = "Exemple de succès",
                                    value = "{ \"status\": \"OK\", \"details\": { \"message\": \"OK\" } }"))),
            @ApiResponse(responseCode = "500", description = "API est hors ligne",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PingResponse.class),
                            examples = @ExampleObject(name = "Exemple d'erreur",
                                    value = "{ \"status\": \"KO\", \"details\": { \"message\": \"KO\" } }")))
    })
    @GetMapping
    public ResponseEntity<PingResponse> ping() {
        try {
            return ResponseEntity.ok().body(new PingResponse("OK", new PingDetails("OK")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PingResponse("KO", new PingDetails("KO")));
        }
    }

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
