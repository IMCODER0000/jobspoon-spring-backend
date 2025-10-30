package com.wowraid.jobspoon.interview.repository;

import com.wowraid.jobspoon.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i WHERE i.account.id = :accountId AND i.deletedAt IS NULL")
    List<Interview> getInterviewResultListByAccountId(@Param("accountId") Long accountId);

    // 최신 완료된 인터뷰 1건 조회 (가장 최근 완료 인터뷰)
    Optional<Interview> findTopByAccountIdAndIsFinishedTrueOrderByCreatedAtDesc(Long accountId);

    // 이번 달 완료된 인터뷰 수 (삭제되지 않은 인터뷰만 포함)
    @Query("""
    SELECT COUNT(i)
    FROM Interview i
    WHERE i.account.id = :accountId
      AND i.isFinished = true
      AND i.deletedAt IS NULL
      AND i.createdAt BETWEEN :start AND :end
          """)
    int countFinishedInterviewsThisMonth(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
