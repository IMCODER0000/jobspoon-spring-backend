package com.wowraid.jobspoon.user_dashboard.service;

import com.wowraid.jobspoon.quiz.repository.UserQuizSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class QuizSummaryServiceImpl implements QuizSummaryService {

    private final UserQuizSessionRepository userQuizSessionRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 총 문제풀이 횟수 */
    @Override
    @Transactional(readOnly = true)
    public long getTotalCount(Long accountId) {
        return userQuizSessionRepository.countByAccountId(accountId);
    }

    /** 이번 달 문제풀이 횟수 */
    @Override
    @Transactional(readOnly = true)
    public long getMonthlyCount(Long accountId) {

        YearMonth yearMonth = YearMonth.now(KST);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay(KST).toLocalDateTime();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(KST).toLocalDateTime();

        return userQuizSessionRepository.countMonthlyByAccountId(accountId, start, end);
    }
}
