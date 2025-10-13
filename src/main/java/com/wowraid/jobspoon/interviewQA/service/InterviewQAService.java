package com.wowraid.jobspoon.interviewQA.service;

import com.wowraid.jobspoon.interview.controller.request.InterviewQARequest;
import com.wowraid.jobspoon.interview.controller.response_form.InterviewCreateResponseForm;
import com.wowraid.jobspoon.interview.entity.Interview;
import com.wowraid.jobspoon.interviewQA.entity.InterviewQA;

import java.util.List;
import java.util.Optional;

public interface InterviewQAService {


    void saveInterviewQAByInterview(Interview interview, InterviewQA interviewQA);
    InterviewQA createInterviewQA(InterviewQARequest interviewQARequest);
    InterviewQA createInterviewQuestion(Interview interview, String interviewQuestion);
    InterviewQA createInterviewQaByInterview(Interview interview);
    InterviewQA saveInterviewAnswer(Long interviewQAId, String interviewAnswer);
    Optional<InterviewQA> findById(Long interviewQAId);
    List<InterviewQA> findAllByInterviewId(Long interviewId);


}
