package com.wowraid.jobspoon.interview_result.service;


import com.wowraid.jobspoon.interview.controller.request_form.InterviewResultRequestForm;
import com.wowraid.jobspoon.interview_result.entity.InterviewResultDetail;
import com.wowraid.jobspoon.interview_result.repository.InterviewResultDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewResultDetailServiceImpl implements InterviewResultDetailService {

    private final InterviewResultDetailRepository interviewResultDetailRepository;


    @Override
    public List<InterviewResultDetail> createInterviewResultDetail(InterviewResultRequestForm interviewResultRequestForm,  Long interviewResultId) {

        List<InterviewResultRequestForm.QaScore> qaScores = interviewResultRequestForm.getResult().getQa_scores();
        List<InterviewResultDetail> interviewResultDetails = new ArrayList<>();
        for (InterviewResultRequestForm.QaScore qaScore : qaScores) {
            InterviewResultDetail interviewResultDetail = new InterviewResultDetail(
                    interviewResultId,qaScore.getQuestion(),qaScore.getAnswer(),qaScore.getFeedback(),qaScore.getCorrection()
            );
            InterviewResultDetail savedInterviewResultDetail = interviewResultDetailRepository.save(interviewResultDetail);
            interviewResultDetails.add(savedInterviewResultDetail);
        }

        return interviewResultDetails;
    }

    @Override
    public List<InterviewResultDetail> findAllByInterviewResultId(Long interviewResultId) {
        return interviewResultDetailRepository.findAllByInterviewResultId(interviewResultId);
    }
}
