package com.wowraid.jobspoon.studyschedule.controller.response_form;

import com.wowraid.jobspoon.studyschedule.service.response.ReadStudyScheduleResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReadStudyScheduleResponseForm {
    private final Long id;
    private final Long authorId;
    private final String authorNickname;
    private final String title;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public static ReadStudyScheduleResponseForm from(ReadStudyScheduleResponse response) {
        return new ReadStudyScheduleResponseForm(
                response.getId(),
                response.getAuthorId(),
                response.getAuthorNickname(),
                response.getTitle(),
                response.getDescription(),
                response.getStartTime(),
                response.getEndTime()
        );
    }
}