package com.example.demo.controller;

import com.example.demo.dto.RecycleLogRequest;
import com.example.demo.dto.RecycleLogResponse;
import com.example.demo.entity.RecycleAnalysisResult;
import com.example.demo.entity.RecycleLog;
import com.example.demo.entity.User;
import com.example.demo.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

@RestController
@RequestMapping("/recycle")
@RequiredArgsConstructor
public class RecycleController {

    private final AtomicLong analysisIdGenerator = new AtomicLong(1);
    private final RecycleService recycleService;

    /**
     * ✅ 이미지 분석 요청 API (포인트 자동 지급 포함)
     */
    @PostMapping("/analyze")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal User user
    ) {
        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "이미지 파일이 비어 있습니다."));
        }

        Long userId = userDetails.getUser().getId();
        Map<String, Object> result = recycleService.analyzeAndSave(image, userId);

        result.putIfAbsent("status", "processing");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    /**
     * ✅ 분리수거 활동 기록 API (인증 사용자 기반 userId 사용)
     * 수기로 기록, 주로 테스트 용용
     */
    @PostMapping("/log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> recordRecycleLog(
            @RequestBody RecycleLogRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            Long userId = userDetails.getUser().getId();
            RecycleLogResponse response = recycleService.saveLog(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * ✅ 분리수거 활동 조회 (한 유저의 모든 기록)
     */
    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyRecycleLogs(@AuthenticationPrincipal User user) {
        Long userId = userDetails.getUser().getId();
        List<RecycleLogResponse> logs = recycleService.getLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }

    /**
     * ✅ 분리수거 로그 단건 조회 API (본인 로그만 조회 가능)
     */
    @GetMapping("/log/{logId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecycleLogById(
            @PathVariable("logId") Long logId,
            @AuthenticationPrincipal User user
    ) {
        try {
            Long userId = userDetails.getUser().getId();
            RecycleLog log = recycleService.getLogById(logId, userId);
            return ResponseEntity.ok(log);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * ✅ 분리수거 로그 삭제 API (본인 로그만 삭제 가능)
     */
    @DeleteMapping("/log/{logId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRecycleLogById(
            @PathVariable("logId") Long logId,
            @AuthenticationPrincipal User user
    ) {
        try {
            Long userId = user.getId();
            recycleService.deleteLogById(logId, userId);
            return ResponseEntity.ok(Map.of("message", "로그가 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * ✅ 분석 결과 조회 API
     */
    @GetMapping("/result/{analysis_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnalysisResult(@PathVariable("analysis_id") Long analysisId) {
        try {
            RecycleAnalysisResult result = recycleService.getResultByAnalysisId(analysisId);
            Map<String, Object> response = new HashMap<>();
            response.put("analysis_id", result.getAnalysisId());
            response.put("category", result.getCategory());
            response.put("confidence", result.getConfidence());
            response.put("disposal_method", result.getDisposalMethod());
            response.put("created_at", result.getCreatedAt().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * ✅ 테스트용: 분석 결과 더미 데이터 삽입 API
     */
    @PostMapping("/insert-dummy")
    public ResponseEntity<?> insertDummy() {
        RecycleAnalysisResult result = new RecycleAnalysisResult();
        result.setAnalysisId(456L);
        result.setCategory("플라스틱");
        result.setConfidence(0.95);
        result.setDisposalMethod("플라스틱 전용 수거함에 버려주세요.");
        recycleService.saveAnalysisResult(result);
        return ResponseEntity.ok(Map.of("message", "테스트용 분석 결과가 저장되었습니다."));
    }
}
