package com.wowraid.jobspoon.user_level.controller;

import com.wowraid.jobspoon.user_level.controller.request.AddExpRequest;
import com.wowraid.jobspoon.user_dashboard.service.TokenAccountService;
import com.wowraid.jobspoon.user_level.controller.response.UserLevelHistoryResponse;
import com.wowraid.jobspoon.user_level.controller.response.UserLevelResponse;
import com.wowraid.jobspoon.user_level.service.UserLevelHistoryService;
import com.wowraid.jobspoon.user_level.service.UserLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-level")
@RequiredArgsConstructor
public class UserLevelController {

    private final TokenAccountService tokenAccountService;
    private final UserLevelService userLevelService;
    private final UserLevelHistoryService userLevelHistoryService;

    /**
     * 사용자 레벨 조회
     */
    @GetMapping
    public ResponseEntity<UserLevelResponse> getUserLevel(
            @CookieValue(name = "userToken", required = false) String userToken
    ) {
        Long accountId = tokenAccountService.resolveAccountId(userToken);
        return ResponseEntity.ok(userLevelService.getUserLevel(accountId));
    }

    /**
     * 유저 레벨 업 이력 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<UserLevelHistoryResponse>> getUserLevelHistory(
            @CookieValue(name = "userToken", required = false) String userToken
    ){
        Long accountId = tokenAccountService.resolveAccountId(userToken);
        return ResponseEntity.ok(userLevelHistoryService.getHistory(accountId));
    }

    /**
     * 경험치 추가 (레벨업 조건은 UserLevelService에서 처리)
     */
    @PostMapping("/experience")
    public ResponseEntity<UserLevelResponse> addExp(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestBody AddExpRequest request
    ) {
        Long accountId = tokenAccountService.resolveAccountId(userToken);
        return ResponseEntity.ok(userLevelService.addExp(accountId, request.getAmount()));
    }
}
