package com.wowraid.jobspoon.quiz.repository;

import com.wowraid.jobspoon.quiz.entity.QuizChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuizChoiceRepository extends JpaRepository<QuizChoice, Long> {
    List<QuizChoice> findByQuizQuestionIdIn(List<Long> questionIds);
    List<QuizChoice> findByQuizQuestionId(Long quizQuestionId);
    List<QuizChoice> findByQuizQuestionIdInOrderByIdAsc(Collection<Long> questionIds);
}