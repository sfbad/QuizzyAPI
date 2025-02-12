package com.teamcocoon.QuizzyAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Quiz{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    private String quizCode;

    private String title;

    private String description;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "quiz",fetch = FetchType.EAGER)
    private List<Question> questions;
}
