package com.wowraid.jobspoon.infrastructure.external.email;

public interface EmailService {
    void sendInterviewResultNotification(String to, Long interviewId);
    void sendErrorNotification(String to, Long userToken);
}