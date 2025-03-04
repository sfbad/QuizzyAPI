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

    @Query("SELECT q FROM Quiz q WHERE q.quizId = :quizId AND q.user.userId = :userId")
    Optional<Quiz> findByQuizIdAndUserId(@Param("quizId") Long quizId, @Param("userId") String userId);

    Optional<Quiz> findByUser_UserIdAndQuizId(String userId,Long quizId);

    Quiz findByQuizId(Long quizId);
}
