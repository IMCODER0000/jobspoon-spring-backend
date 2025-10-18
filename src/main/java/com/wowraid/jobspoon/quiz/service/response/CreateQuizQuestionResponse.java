package com.wowraid.jobspoon.quiz.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wowraid.jobspoon.quiz.entity.enums.QuestionType;
import com.wowraid.jobspoon.quiz.entity.QuizQuestion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateQuizQuestionResponse {

    private final String message;
    private final Long questionId;
    private final QuestionType questionType;
    private final String questionText;

    // 선택형(OX/CHOICE)
    private final Integer answerIndex;

    // 텍스트형(INITIALS/주관식 등)
    private final String answerText;

    public static CreateQuizQuestionResponse from(QuizQuestion q) {
        String msg = "문제가 성공적으로 등록되었습니다.";
        return new CreateQuizQuestionResponse(
                msg,
                q.getId(),
                q.getQuestionType(),
                q.getQuestionText(),
                q.getAnswerIndex(),   // OX/CHOICE면 값, INITIALS면 null
                q.getAnswerText()     // INITIALS/주관식이면 값, OX/CHOICE면 null
        );
    }
}
