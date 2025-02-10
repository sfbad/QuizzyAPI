package com.teamcocoon.QuizzyAPI.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/ping")
public class Ping {
    @GetMapping()
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Ok");
    }
}
