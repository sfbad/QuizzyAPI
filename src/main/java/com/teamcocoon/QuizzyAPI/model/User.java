package com.teamcocoon.QuizzyAPI.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    private String userId;

    private String username;

    private String email;

}
