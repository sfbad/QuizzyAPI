package com.teamcocoon.QuizzyAPI.repositories;

import com.teamcocoon.QuizzyAPI.model.Question;
import com.teamcocoon.QuizzyAPI.model.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    @Modifying
    @Query("DELETE FROM Response r WHERE r.question.questionId = :questionId")
    void deleteAllByQuestion(Question question);

}
