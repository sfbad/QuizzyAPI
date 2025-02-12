package com.teamcocoon.QuizzyAPI.repositories;

import com.teamcocoon.QuizzyAPI.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

}
