package com.wowraid.jobspoon.user_term.service.response;

import com.wowraid.jobspoon.user_term.entity.UserRecentTerm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordTermViewResponse {
    private final Long userRecentTermId;
    private final Long termId;
    private final LocalDateTime firstSeenAt;
    private final LocalDateTime lastSeenAt;
    private final Long viewCount;

    public static RecordTermViewResponse from(UserRecentTerm userRecentTerm) {
        return new RecordTermViewResponse(
                userRecentTerm.getId(),
                userRecentTerm.getTerm().getId(),
                userRecentTerm.getFirstSeenAt(),
                userRecentTerm.getLastSeenAt(),
                userRecentTerm.getViewCount()
        );
    }
}
