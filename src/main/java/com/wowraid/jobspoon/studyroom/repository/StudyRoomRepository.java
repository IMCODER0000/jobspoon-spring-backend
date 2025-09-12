package com.wowraid.jobspoon.studyroom.repository;

import com.wowraid.jobspoon.studyroom.entity.StudyLocation;
import com.wowraid.jobspoon.studyroom.entity.StudyRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    @Query("SELECT sr FROM StudyRoom sr JOIN FETCH sr.host WHERE sr.id = :id")
    Optional<StudyRoom> findByIdWithHost(@Param("id") Long id);

    @Query("SELECT sr.id FROM StudyRoom sr WHERE sr.id < :lastStudyId ORDER BY sr.id DESC")
    Slice<Long> findIdsByIdLessThan(@Param("lastStudyId") Long lastStudyId, Pageable pageable);

    @Query("SELECT sr.id FROM StudyRoom sr ORDER BY sr.id DESC")
    Slice<Long> findIds(Pageable pageable);

    // 2단계: ID 목록을 기반으로 모든 데이터 Fetch
    @Query("SELECT DISTINCT sr FROM StudyRoom sr " +
            "LEFT JOIN FETCH sr.skillStack " +
            "LEFT JOIN FETCH sr.recruitingRoles " +
            "WHERE sr.id IN :ids " +
            "ORDER BY sr.id DESC")
    List<StudyRoom> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    List<StudyRoom> findByLocation(StudyLocation location);

    // =========================
    // 👇 대시보드용 메소드 추가
    // =========================
    /** 특정 accountId가 Host인 StudyRoom 개수 카운트 */
    long countByHost_Account_Id(Long accountId);
}