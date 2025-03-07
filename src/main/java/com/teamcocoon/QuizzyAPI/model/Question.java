package com.teamcocoon.QuizzyAPI.model;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    private String title;

    @ManyToOne
    private Quiz quiz;

    @OneToMany
    private List<Response> responses;

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", title='" + title + '\'' +
                ", quiz=" + quiz +
                ", responses=" + responses +
                '}';
    }
}
