package com.wowraid.jobspoon.user_term.controller.response_form;

import com.wowraid.jobspoon.user_term.service.response.MoveFavoritesResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveFavoritesResponseForm {
    private Long targetFolderId;
    private int movedCount;
    private int skippedCount;
    private List<Item> skipped; // 중복/권한 등 스킵 사유

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private Long termId;
        private String reason; // DUPLICATE_IN_TARGET | NOT_FOUND_FAVORITE | FORBIDDEN | UNKNOWN
    }

    public static MoveFavoritesResponseForm from(MoveFavoritesResponse response) {
        var list = response.getSkipped().stream()
                .map(s -> new Item(s.getTermId(), s.getReason().name()))
                .toList();
        return new MoveFavoritesResponseForm(response.getTargetFolderId(), response.getMovedCount(), list.size(), list);
    }
}
