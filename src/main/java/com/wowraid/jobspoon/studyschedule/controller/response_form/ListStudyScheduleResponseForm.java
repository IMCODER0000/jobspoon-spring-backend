package com.wowraid.jobspoon.studyschedule.controller.response_form;

import com.wowraid.jobspoon.studyschedule.service.response.ListStudyScheduleResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ListStudyScheduleResponseForm {
    private final Long id;
    private final Long authorId;
    private final String authorNickname;
    private final String title;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public static ListStudyScheduleResponseForm from(ListStudyScheduleResponse response) {
        return new ListStudyScheduleResponseForm(
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