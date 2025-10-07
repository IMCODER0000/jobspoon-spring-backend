package com.wowraid.jobspoon.user_term.controller;

import com.wowraid.jobspoon.redis_cache.RedisCacheService;
import com.wowraid.jobspoon.user_term.controller.request_form.*;
import com.wowraid.jobspoon.user_term.controller.response_form.*;
import com.wowraid.jobspoon.user_term.repository.UserTermProgressRepository;
import com.wowraid.jobspoon.user_term.repository.UserWordbookFolderRepository;
import com.wowraid.jobspoon.user_term.service.*;
import com.wowraid.jobspoon.user_term.service.request.*;
import com.wowraid.jobspoon.user_term.service.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserTermController {

    private final FavoriteTermService favoriteTermService;
    private final UserWordbookFolderService userWordbookFolderService;
    private final MemorizationService memorizationService;
    private final UserWordbookFolderRepository userWordbookFolderRepository;
    private final UserRecentTermService userRecentTermService;
    private final RedisCacheService redisCacheService;
    private final UserTermProgressRepository userTermProgressRepository;
    private final UserWordbookFolderQueryService userWordbookFolderQueryService;

    /** 공통: 쿠키에서 토큰 추출 후 Redis에서 accountId 조회(없으면 null) — 쿠키 전용 */
    private Long resolveAccountId(String userToken) {
        if (userToken == null || userToken.isBlank()) return null;
        return redisCacheService.getValueByKey(userToken, Long.class); // TTL 만료/무효면 null
    }

    // 즐겨찾기 용어 등록
    @PostMapping("/me/favorite-terms")
    public CreateFavoriteTermResponseForm responseForm(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestBody @Valid CreateFavoriteTermRequestForm requestForm) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[favorite:create] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        CreateFavoriteTermRequest request = requestForm.toCreateFavoriteTermRequest(accountId);
        CreateFavoriteTermResponse response = favoriteTermService.registerFavoriteTerm(request);
        log.info("[favorite:create] done");
        log.debug("[favorite:create] accountId={} -> response={}", accountId, response);
        return CreateFavoriteTermResponseForm.from(response);
    }

    // 즐겨찾기 용어 삭제
    @DeleteMapping("/me/favorite-terms/{favoriteTermId}")
    public ResponseEntity<?> deleteFavoriteTerm(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long favoriteTermId) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[favorite:delete] 인증 실패");
            return ResponseEntity.status(UNAUTHORIZED).build();
        }
        log.debug("[favorite:delete] favoriteTermId={}", favoriteTermId);
        var response = favoriteTermService.deleteFavoriteTerm(favoriteTermId);
        log.info("[favorite:delete] done");
        return response;
    }

    // 즐겨찾기 용어 이동
    @PatchMapping("/me/wordbook/favorites:move")
    public MoveFavoritesResponseForm moveFavorites(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestBody @Valid MoveFavoritesRequestForm requestForm) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var request = requestForm.toRequest(accountId);
        var response = favoriteTermService.moveToFolder(request);
        log.info("[favorites:move] done");
        log.debug("[favorites:move] accountId={}, response={}", accountId, response);
        return MoveFavoritesResponseForm.from(response);
    }

    // 폴더 간 이동 지원
    @PatchMapping("/me/folders/{sourceFolderId}/terms:move")
    public MoveFolderTermsResponseForm moveFolderTerms(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long sourceFolderId,
            @RequestBody @Valid MoveFolderTermsRequestForm requestForm) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folders:move] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var response = userWordbookFolderService.moveTerms(accountId, sourceFolderId, requestForm.getTargetFolderId(), requestForm.getTermIds());
        log.info("[folders:move] done");
        log.debug("[folders:move] accountId={}, sourceFolderId={}, targetFolderId={}, count={}",
                accountId, sourceFolderId, requestForm.getTargetFolderId(), requestForm.getTermIds().size());
        return MoveFolderTermsResponseForm.from(response);
    }

    // 단어장 폴더 추가
    @PostMapping("/me/folders")
    public CreateUserWordbookFolderResponseForm createFolder(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestBody @Valid CreateUserWordbookFolderRequestForm requestForm) {

        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:create] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        CreateUserWordbookFolderRequest request = requestForm.toCreateFolderRequest(accountId);
        CreateUserWordbookFolderResponse response = userWordbookFolderService.registerWordbookFolder(request);
        log.info("[folder:create] done");
        log.debug("[folder:create] accountId={}, folderName={}", accountId, response.getFolderName());
        return CreateUserWordbookFolderResponseForm.from(response);
    }

    // 암기 상태 변경 : 로그인 사용자 기준, termId로 직접 상태 변경
    @PatchMapping("/me/terms/{termId}/memorization")
    public UpdateMemorizationResponseForm updateMemorizationByTermId(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long termId,
            @RequestBody @Valid UpdateMemorizationRequestForm requestForm
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[memo:update:byTerm] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        log.debug("[memo:update:byTerm:req] accountId={}, termId={}, status={}", accountId, termId, requestForm.getStatus());
        UpdateMemorizationRequest request = requestForm.toUpdateMemorizationRequest(accountId, termId);
        UpdateMemorizationResponse response = memorizationService.updateMemorization(request);
        log.info("[memo:update:byTerm] done");
        log.debug("[memo:update:byTerm] response={}", response);
        return UpdateMemorizationResponseForm.from(response);
    }

    // 여러 용어의 암기 상태 조회
    @GetMapping("/me/terms/memorization")
    public Map<String, String> getMemorizationStatuses(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestParam(name = "ids") String idsCsv
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 빈/공백 방어
        if (idsCsv == null || idsCsv.isBlank()) {
            return Map.of();
        }

        // CSV -> List<Long>
        final List<Long> termIds;
        try {
            termIds = Arrays.stream(idsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException nfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ids 파라미터 형식이 올바르지 않습니다.");
        }

        if (termIds.isEmpty()) return Map.of();

        var rows = userTermProgressRepository.findByIdAccountIdAndIdTermIdIn(accountId, termIds);

        // 기본값 LEARNING으로 채워 두고, 조회된 건 덮어쓰기
        Map<String, String> result = new LinkedHashMap<>();
        termIds.stream().distinct().forEach(id -> result.put(String.valueOf(id), "LEARNING"));
        rows.forEach(p -> result.put(String.valueOf(p.getId().getTermId()), p.getStatus().name()));

        log.info("[memo:list] done");
        log.debug("[memo:list] accountId={}, size={}", accountId, result.size());
        return result;
    }

    // 최근 학습/열람 이벤트 발생 시 ‘최근 본 용어’로 저장하기
    @PostMapping("/me/terms/{termId}/view")
    public ResponseEntity<RecordTermViewResponseForm> view(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long termId,
            @RequestBody @Valid RecordTermViewRequestForm requestForm) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[recent:view] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        log.info("[recent:view] accountId={}, termId={}, reqForm={}", accountId, termId, requestForm);
        RecordTermViewRequest request = requestForm.toRecordTermViewRequest(accountId, termId);
        RecordTermViewResponse response = userRecentTermService.recordTermView(request);
        log.info("[recent:view] done");
        return ResponseEntity.ok(RecordTermViewResponseForm.from(response));
    }

    // 인증된 사용자가 폴더별로 단어장 조회하기(페이지네이션 & 정렬)
    @GetMapping({"/me/folders/{folderId}/terms"})
    public ListUserWordbookTermResponseForm userTermList(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId,
            @ModelAttribute ListUserWordbookTermRequestForm requestForm
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:terms:list] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        var serviceReq = requestForm.toListUserTermRequest(accountId, folderId);
        var serviceRes = userWordbookFolderService.list(serviceReq);
        return ListUserWordbookTermResponseForm.from(
                serviceRes, serviceReq.getPage(), serviceReq.getPerPage(), requestForm.getSort()
        );
    }

    // 단어장 폴더 순서 변경하기
    @PatchMapping("/me/folders:reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderFolders(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestBody @Valid ReorderUserWordbookFoldersRequestForm requestForm
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:reorder] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        log.debug("[folder:reorder] accountId={}, req={}", accountId, requestForm);
        userWordbookFolderService.reorder(requestForm.toRequest(accountId));
        log.info("[folder:reorder] done");
    }

    // 단어장 폴더 리스트 조회하기
    @GetMapping({"/me/folders", "/user-terms/folders"})
    public List<Map<String, Object>> listFolders(
            @CookieValue(name = "userToken", required = false) String userToken
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:list] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var list = userWordbookFolderRepository.findAllByAccount_IdOrderBySortOrderAscIdAsc(accountId);
        var result = list.stream()
                .map(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", f.getId());
                    m.put("folderName", f.getFolderName());
                    m.put("sortOrder", f.getSortOrder());
                    return m;
                })
                .collect(Collectors.toList());
        log.info("[folder:list] done");
        log.debug("[folder:list] accountId={}, count={}", accountId, result.size());
        return result;
    }

    // 단어장 폴더에 용어 추가하기
    @PostMapping("/me/folders/{folderId}/terms")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserWordbookTermResponseForm addTermFolder(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId,
            @RequestBody @Valid AddTermToFolderRequestForm requestForm
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:attach] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        log.debug("[folder:attach] accountId={}, folderId={}, reqForm={}", accountId, folderId, requestForm);
        CreateUserWordbookTermRequest request = requestForm.toRequest(accountId, folderId);
        CreateUserWordbookTermResponse response = userWordbookFolderService.attachTerm(request);
        log.info("[folder:attach] done");
        log.debug("[folder:attach:res] {}", response);
        return CreateUserWordbookTermResponseForm.from(response);
    }

    // 단어장 폴더 이름 변경하기
    @PatchMapping("/me/folders/{folderId}")
    public RenameUserWordbookFolderResponseForm renameFolder(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId,
            @RequestBody @Valid RenameUserWordbookFolderRequestForm requestForm) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:attach] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        log.debug("[folder:rename] accountId={}, folderId={}", accountId, folderId);

        var request = requestForm.toRequest(accountId, folderId);
        var response = userWordbookFolderService.rename(request);
        log.debug("[folder:rename] response={}", response);
        return RenameUserWordbookFolderResponseForm.from(response);
    }

    // 단어장 폴더 삭제(단건)
    @DeleteMapping("/me/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId,
            @RequestParam(name = "mode", defaultValue = "purge") String mode,
            @RequestParam(name = "targetFolderId", required = false) Long targetFolderId
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:attach] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        userWordbookFolderService.deleteOne(
                accountId,
                UserWordbookFolderService.DeleteMode.of(mode),
                folderId,
                targetFolderId
        );
        return ResponseEntity.noContent().build();
    }

    // 단어장 폴더 삭제(다건)
    @DeleteMapping("/me/folders:bulk")
    public ResponseEntity<Void> deleteFoldersBulk(
            @CookieValue(name = "userToken", required = false) String userToken,
            @RequestParam(name = "mode", defaultValue = "purge") String mode,
            @RequestParam(name = "targetFolderId", required = false) Long targetFolderId,
            @RequestBody @Valid BulkDeleteFoldersRequestForm form
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:attach] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        var ids = form.getFolderIds();
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        userWordbookFolderService.deleteBulk(
                accountId,
                UserWordbookFolderService.DeleteMode.of(mode),
                ids.stream().distinct().toList(),
                targetFolderId
        );
        return ResponseEntity.noContent().build();
    }

    // PDF 생성을 위해 단어장 폴더의 termId 한 번에 조회하기
    @GetMapping("/me/folders/{folderId}/term-ids")
    public ResponseEntity<?> getAllTermIds(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("[folder:attach] 인증 실패");
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        var result = userWordbookFolderService.getAllTermIds(accountId, folderId);

        if (result.limitExceeded()) {
            return ResponseEntity.status(PAYLOAD_TOO_LARGE)
                    .header("Ebook-Error", "LIMIT_EXCEEDED")
                    .header("Ebook-Limit", String.valueOf(result.limit()))
                    .header("Ebook-Total", String.valueOf(result.total()))
                    .body("LIMIT_EXCEEDED");
        }
        if (result.termIds().isEmpty()) {
            return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                    .header("Ebook-Error", "EMPTY_FOLDER")
                    .body("EMPTY_FOLDER");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("folderId", result.folderId());
        body.put("count", result.termIds().size());
        body.put("termIds", result.termIds());
        return ResponseEntity.ok(body);
    }

    // 내 단어장 폴더 목록과 각 폴더의 즐겨찾기 용어 수 조회하기
    @GetMapping("/me/wordbook/folders")
    public ResponseEntity<?> getMyFolders(
            @CookieValue(name = "userToken", required = false) String userToken
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            log.warn("인증 실패: 계정 식별 불가");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(userWordbookFolderQueryService.getMyFolders(accountId));
        } catch (Exception e) {
            log.error("폴더 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 여러 용어를 한 단어장 폴더에 일괄 추가하기
    @PostMapping("/me/folders/{folderId}/terms:bulk")
    public ResponseEntity<AttachTermsBulkResponseForm> attachTermsBulk(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId,
            @RequestBody @Valid AttachTermsBulkRequestForm requestForm
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        AttachTermsBulkRequest request = requestForm.toRequest(accountId, folderId);
        AttachTermsBulkResponse response = userWordbookFolderService.attachTermsBulk(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AttachTermsBulkResponseForm.from(response));
    }

    // 단어장 폴더에 있는 총 단어 개수를 즉시 확인하기
    @GetMapping("/me/folders/{folderId}/terms/count")
    public Map<String, Object> countFolderTerms(
            @CookieValue(name = "userToken", required = false) String userToken,
            @PathVariable Long folderId
    ) {
        Long accountId = resolveAccountId(userToken);
        if (accountId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        long count = userWordbookFolderQueryService.countTermsInFolderOrThrow(accountId, folderId);
        return Map.of("folderId",  folderId, "count", count);
    }
}
