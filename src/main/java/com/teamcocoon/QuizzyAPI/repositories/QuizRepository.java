package com.teamcocoon.QuizzyAPI.repositories;

import com.teamcocoon.QuizzyAPI.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q FROM Quiz q WHERE q.user.userId = :uid")
    List<Quiz> findListQuizByUserId(@Param("uid") String uid);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.quizId = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);

    Optional<Quiz> findByQuizCode(String quizCode);

}
