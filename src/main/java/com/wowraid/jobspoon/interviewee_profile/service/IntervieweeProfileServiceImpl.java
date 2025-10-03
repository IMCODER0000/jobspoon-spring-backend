package com.wowraid.jobspoon.interviewee_profile.service;


import com.wowraid.jobspoon.account.entity.Account;
import com.wowraid.jobspoon.interview.controller.request.IntervieweeProfileRequest;
import com.wowraid.jobspoon.interview.controller.request_form.InterviewCreateRequestForm;
import com.wowraid.jobspoon.interviewee_profile.entity.IntervieweeProfile;
import com.wowraid.jobspoon.interviewee_profile.repository.IntervieweeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntervieweeProfileServiceImpl implements IntervieweeProfileService {

    private final IntervieweeProfileRepository intervieweeProfileRepository;

    @Override
    @Transactional
    public IntervieweeProfile createIntervieweeProfile(IntervieweeProfileRequest intervieweeProfileRequest) {
         return intervieweeProfileRepository.save(
                new IntervieweeProfile(
                        intervieweeProfileRequest.getCompany(),
                        intervieweeProfileRequest.getMajor(),
                        intervieweeProfileRequest.getCareer(),
                        intervieweeProfileRequest.getProjectExp(),
                        intervieweeProfileRequest.getJob(),
                        intervieweeProfileRequest.getProjectDescription(),
                        intervieweeProfileRequest.getTechStacks()
                )
        );

    }

    @Override
    public Optional<IntervieweeProfile> findById(Long id) {
        return intervieweeProfileRepository.findById(id);
    }
}
