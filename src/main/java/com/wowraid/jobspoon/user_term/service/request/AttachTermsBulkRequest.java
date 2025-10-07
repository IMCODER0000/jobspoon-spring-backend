package com.wowraid.jobspoon.user_term.service.request;

import java.util.List;

public record AttachTermsBulkRequest(
        Long accountId,
        Long folderId,
        List<Long> termIds,
        DedupeMode dedupeMode
) {
    /** 중복 정책. 현재 구현은 SKIP 고정으로 동작하지만, 추후 확장 대비 필드 유지 */
    public enum DedupeMode {
        SKIP,   // 중복은 건너뛰고 진행
        FAIL,   // 중복 발견 시 전체 실패(409) - 필요 시 구현
        UPSERT; // 중복이면 무시 또는 갱신 - 필요 시 구현

        /** 문자열 → 열거형 매핑. null/빈값/매핑 실패는 기본 SKIP */
        public static DedupeMode of(String v) {
            if (v == null || v.isBlank()) return SKIP;
            try {
                return DedupeMode.valueOf(v.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return SKIP;
            }
        }
    }
}
