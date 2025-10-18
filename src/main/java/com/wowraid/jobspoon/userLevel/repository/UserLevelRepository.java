package com.wowraid.jobspoon.userLevel.repository;

import com.wowraid.jobspoon.userLevel.entity.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    boolean existsByAccountId(Long accountId);

    // 계정별 유저 레벨 가져오기
    Optional<UserLevel> findByAccountId(Long accountId);

    void deleteAllByAccountId(Long accountId);
}
