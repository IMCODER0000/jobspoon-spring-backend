package com.wowraid.jobspoon.studyroom_report.controller;

import com.wowraid.jobspoon.account.entity.Account;
import com.wowraid.jobspoon.account.entity.RoleType;
import com.wowraid.jobspoon.accountProfile.entity.AccountProfile;
import com.wowraid.jobspoon.accountProfile.repository.AccountProfileRepository;
import com.wowraid.jobspoon.redis_cache.RedisCacheService;
import com.wowraid.jobspoon.studyroom_report.service.StudyRoomReportService;
import com.wowraid.jobspoon.studyroom_report.service.request.UpdateStudyRoomReportStatusRequest;
import com.wowraid.jobspoon.studyroom_report.service.response.StudyRoomReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminReportController {

    private final StudyRoomReportService studyRoomReportService;
    private final RedisCacheService redisCacheService;
    private final AccountProfileRepository accountProfileRepository;

    @GetMapping("/study-rooms/reports")
    public ResponseEntity<List<StudyRoomReportResponse>> getStudyRoomReports(
            @CookieValue(name = "userToken", required = false) String userToken) {

        Long userId = redisCacheService.getValueByKey(userToken, Long.class);
        if(userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AccountProfile userProfile = accountProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Account account = userProfile.getAccount();
        if (account.getAccountRoleType().getRoleType() != RoleType.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StudyRoomReportResponse> reports = studyRoomReportService.findAllReports();
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/study-rooms/reports/{reportId}/status")
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody UpdateStudyRoomReportStatusRequest request,
            @CookieValue(name = "userToken", required = false) String userToken) {

        Long userId = redisCacheService.getValueByKey(userToken, Long.class);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AccountProfile userProfile = accountProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (userProfile.getAccount().getAccountRoleType().getRoleType() != RoleType.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        studyRoomReportService.updateReportStatus(reportId, request.getStatus());

        return ResponseEntity.ok().build();
    }
}
