package com.teamcocoon.QuizzyAPI.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ping")
public class Ping {

    @GetMapping
    public ResponseEntity<Object> ping() {
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
