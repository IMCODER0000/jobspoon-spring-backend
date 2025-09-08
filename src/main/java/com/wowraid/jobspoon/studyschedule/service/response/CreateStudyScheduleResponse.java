package com.wowraid.jobspoon.studyschedule.service.response;

import com.wowraid.jobspoon.studyschedule.entity.StudySchedule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CreateStudyScheduleResponse {
    private final Long id;
    private final Long authorId;
    private final String authorNickname;
    private final String title;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public static CreateStudyScheduleResponse from(StudySchedule schedule) {
        return new  CreateStudyScheduleResponse(
                schedule.getId(),
                schedule.getAuthor().getId(),
                schedule.getAuthor().getNickname(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}