package com.wowraid.jobspoon.quiz.entity;

import com.wowraid.jobspoon.account.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wrong_note",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_wrong_user_question",
        columnNames = {"account_id", "quiz_question_id"}))
public class UserWrongNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 오답노트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // 사용자 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion; // 틀린 문제

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_choice_id", nullable = false)
    private QuizChoice quizChoice; // 사용자가 선택한 오답 보기와 해설

    private LocalDateTime submittedAt; // 오답 저장 시각
    private String explanation; // 해설

    public static UserWrongNote create(Account account,
                                       QuizQuestion question,
                                       QuizChoice choice,
                                       String explanation) {
        UserWrongNote n = new UserWrongNote();
        n.account = account;
        n.quizQuestion = question;
        n.quizChoice = choice;
        n.submittedAt = LocalDateTime.now();
        n.explanation = explanation;
        return n;
    }

    public void update(QuizChoice choice, String explanation) {
        this.quizChoice = choice;
        this.explanation = explanation;
        this.submittedAt = LocalDateTime.now();
    }

    public void setQuizChoice(QuizChoice c){ this.quizChoice = c; }
    public void setExplanation(String e){ this.explanation = e; }
    public void setSubmittedAt(LocalDateTime t){ this.submittedAt = t; }
}
