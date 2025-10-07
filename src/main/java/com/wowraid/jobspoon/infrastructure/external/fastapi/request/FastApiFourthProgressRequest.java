package com.wowraid.jobspoon.infrastructure.external.fastapi.request;

import com.wowraid.jobspoon.interviewee_profile.entity.TechStack;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FastApiFourthProgressRequest {

    private Long interviewId;
    private List<TechStack> techStack;
    private Long questionId;
    private String answerText;
    private String userToken;


}
