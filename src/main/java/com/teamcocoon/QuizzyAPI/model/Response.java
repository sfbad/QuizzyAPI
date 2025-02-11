package com.teamcocoon.QuizzyAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    private String title;

    private boolean isCorrect;

    @ManyToOne
    private Question question;
}
