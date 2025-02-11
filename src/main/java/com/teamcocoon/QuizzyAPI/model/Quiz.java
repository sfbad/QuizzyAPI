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
public class Quiz{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long QuizId;

    private String QuizCode;

    private String title;

    @ManyToOne
    private User user;
}
