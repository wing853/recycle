package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.dto.RecycleLogRequest;
import com.example.demo.dto.RecycleLogResponse;
import com.example.demo.entity.RecycleLog;
import com.example.demo.entity.RecycleAnalysisResult;
import com.example.demo.entity.Point;
import com.example.demo.entity.PointHistory;
import com.example.demo.repository.RecycleLogRepository;
import com.example.demo.repository.RecycleAnalysisResultRepository;
import com.example.demo.repository.PointRepository;
import com.example.demo.repository.PointHistoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.nio.file.Files;

@RequiredArgsConstructor
@Service
public class RecycleService {

    private static final Logger log = LoggerFactory.getLogger(RecycleService.class);
    private final RecycleLogRepository recycleLogRepository;
    private final RecycleAnalysisResultRepository recycleAnalysisResultRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;


    // ✅ Render AI 서버 URL
    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // ✅ 이미지 임시 저장 경로
    @Value("${app.upload-dir}")
    private String uploadDir;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public RecycleLogResponse saveLog(RecycleLogRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecycleLog log = new RecycleLog();
        log.setUser(user);
        log.setAnalysisId(request.getAnalysisId());
        log.setDisposalCategory(request.getDisposalCategory());
        log.setDisposalMethod(request.getDisposalMethod());

        RecycleLog saved = recycleLogRepository.save(log);

        int pointsEarned = 100;

        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Point p = new Point();
                    p.setUser(user);
                    p.setPoints(0);
                    return p;
                });

        point.setPoints(point.getPoints() + pointsEarned);
        point.setUpdatedAt(ZonedDateTime.now());
        pointRepository.save(point);

        PointHistory history = new PointHistory();
        history.setUser(user);
        history.setDate(ZonedDateTime.now());
        history.setType("적립");
        history.setReason(request.getDisposalCategory() + " 분리수거");
        history.setWasteTypeKorean(request.getDisposalCategory());
        history.setPoints(pointsEarned);
        history.setBalance(point.getPoints());
        pointHistoryRepository.save(history);

        long recycleCount = recycleLogRepository.countByUserId(userId);

        return RecycleLogResponse.builder()
                .logId(saved.getId())
                .totalPoints(point.getPoints())
                .recycleCount(recycleCount)
                .message(request.getDisposalCategory() + " 분리수거가 기록되었습니다.")
                .build();
    }

    public int getUserRank(Long userId) {
        List<Object[]> allRanks = recycleLogRepository.countRecycleLogsByUser();
        allRanks.sort((a, b) -> Long.compare((Long) b[1], (Long) a[1]));

        for (int i = 0; i < allRanks.size(); i++) {
            Long uid = (Long) allRanks.get(i)[0];
            if (uid.equals(userId)) return i + 1;
        }
        return -1;
    }

    @Transactional(readOnly = true)
    public RecycleLog getLogById(Long logId, Long userId) {
        Optional<RecycleLog> optionalLog = recycleLogRepository.findById(logId);
        if (optionalLog.isEmpty()) {
            throw new IllegalArgumentException("해당 로그를 찾을 수 없습니다.");
        }

        RecycleLog log = optionalLog.get();
        if (!log.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 로그에 접근할 권한이 없습니다.");
        }

        return log;
    }

    @Transactional
    public void deleteLogById(Long logId, Long userId) {
        RecycleLog log = getLogById(logId, userId);
        recycleLogRepository.delete(log);
    }

    @Transactional(readOnly = true)
    public RecycleAnalysisResult getResultByAnalysisId(Long analysisId) {
        List<RecycleAnalysisResult> results = recycleAnalysisResultRepository.findByAnalysisId(analysisId);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("해당 분석 결과가 없습니다.");
        }
        return results.get(0);
    }

    @Transactional
    public void saveAnalysisResult(RecycleAnalysisResult result) {
        recycleAnalysisResultRepository.save(result);
    }

    @Transactional
    public Map<String, Object> analyzeAndSave(MultipartFile image, Long userId) {
        Map<String, Object> resultInfo = new HashMap<>();
        try {
            Long analysisId = System.currentTimeMillis();

            // 1️⃣ 이미지 저장 (AI 서버로 보내기 전 임시 저장)
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = StringUtils.getFilename(image.getOriginalFilename());
            String ascii = Normalizer.normalize(original, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = UUID.randomUUID() + "_" + ascii;
            Path target = uploadPath.resolve(fileName);
            image.transferTo(target);

            // 2️⃣ Render AI API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new FileSystemResource(target.toFile()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("AI 서버로 분석 요청 송신: {}", aiApiUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(aiApiUrl, requestEntity, Map.class);

            // 3️⃣ 결과 파싱
            Map<String, Object> resultMap = response.getBody();
            if (resultMap == null) throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다.");

            String category = (String) resultMap.getOrDefault("category", "unknown");
            double confidence = Double.parseDouble(resultMap.getOrDefault("confidence", 0.0).toString());
            String disposalMethod = (String) resultMap.getOrDefault("disposal_method", "일반 쓰레기통에 버려주세요.");

            // ✅ Gemini Vision fallback (confidence 0.85 미만일 때)
            if (confidence < 0.85) {
                log.info("YOLO 신뢰도 낮음 ({}) → Gemini Vision으로 재판별", confidence);
                try {
                    byte[] imageBytes = Files.readAllBytes(target);
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

                    String prompt = """
                        이 이미지는 분리수거 대상 물체입니다.
                        다음 항목을 JSON 형식으로만 답변해주세요. 다른 말은 하지 마세요.
                        반드시 아래 category 값 중 하나만 사용하세요: 페트병, 플라스틱, 종이, 유리, 캔, 비닐, 스티로폼
                        {
                          "category": "위 목록 중 하나",
                          "disposal_method": "구체적인 분리수거 방법"
                        }
                        """;

                    HttpHeaders geminiHeaders = new HttpHeaders();
                    geminiHeaders.setContentType(MediaType.APPLICATION_JSON);

                    Map<String, Object> geminiBody = new HashMap<>();
                    geminiBody.put("contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of(
                                            "inline_data", Map.of(
                                                    "mime_type", "image/jpeg",
                                                    "data", base64Image
                                            )
                                    ),
                                    Map.of("text", prompt)
                            ))
                    ));

                    HttpEntity<Map<String, Object>> geminiRequest = new HttpEntity<>(geminiBody, geminiHeaders);

                    String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
                    ResponseEntity<Map> geminiResponse = restTemplate.postForEntity(geminiUrl, geminiRequest, Map.class);

                    // 응답 파싱
                    Map<String, Object> geminiResult = geminiResponse.getBody();
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) geminiResult.get("candidates");
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    String geminiText = (String) parts.get(0).get("text");

                    // JSON 파싱 (```json 블록 제거)
                    geminiText = geminiText.replaceAll("```json", "").replaceAll("```", "").trim();

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, String> parsedResult = mapper.readValue(geminiText, Map.class);

                    category = parsedResult.getOrDefault("category", category);
                    disposalMethod = parsedResult.getOrDefault("disposal_method", disposalMethod);
                    log.info("Gemini Vision 재판별 완료 → category: {}", category);

                } catch (Exception e) {
                    log.warn("Gemini Vision 호출 실패, YOLO 결과 유지: {}", e.getMessage());
                }
            }

            // 카테고리 정규화 (YOLO/Gemini 반환값을 표준 한국어로 통일)
            category = normalizeCategory(category);

            // 4️⃣ 분석 결과 저장
            RecycleAnalysisResult result = new RecycleAnalysisResult();
            result.setAnalysisId(analysisId);
            result.setCategory(category);
            result.setConfidence(confidence);
            result.setDisposalMethod(disposalMethod);
            result.setCreatedAt(ZonedDateTime.now());
            recycleAnalysisResultRepository.save(result);

            // 5️⃣ 유저 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 6️⃣ 포인트 제한 체크
            final int DAILY_POINT_LIMIT = 300;
            final int DAILY_ANALYSIS_LIMIT = 3;
            final int ANALYSIS_REWARD = 100;

            Long todayTotalPoints = pointHistoryRepository.getTodayTotalEarnedPoints(userId);
            if (todayTotalPoints == null) todayTotalPoints = 0L;

            Long todayAiCount = pointHistoryRepository.getTodayAiRewardCount(userId);
            if (todayAiCount == null) todayAiCount = 0L;

            boolean canReward = todayTotalPoints + ANALYSIS_REWARD <= DAILY_POINT_LIMIT
                    && todayAiCount < DAILY_ANALYSIS_LIMIT;

            // 7️⃣ 포인트 지급
            if (canReward) {
                Point point = pointRepository.findByUserId(userId)
                        .orElseGet(() -> {
                            Point p = new Point();
                            p.setUser(user);
                            p.setPoints(0);
                            return p;
                        });

                point.setPoints(point.getPoints() + ANALYSIS_REWARD);
                point.setUpdatedAt(ZonedDateTime.now());
                pointRepository.save(point);

                PointHistory history = new PointHistory();
                history.setUser(user);
                history.setDate(ZonedDateTime.now());
                history.setType("적립");
                history.setReason("AI 분석 리워드");
                history.setWasteTypeKorean(category);
                history.setPoints(ANALYSIS_REWARD);
                history.setBalance(point.getPoints());
                pointHistoryRepository.save(history);

                resultInfo.put("points_rewarded", ANALYSIS_REWARD);
                resultInfo.put("message", "포인트가 지급되었습니다.");
            } else {
                resultInfo.put("points_rewarded", 0);
                resultInfo.put("message", "하루 보상 한도를 초과하여 포인트가 지급되지 않았습니다.");
            }

            resultInfo.put("category", category);
            resultInfo.put("confidence", confidence);
            resultInfo.put("disposal_method", disposalMethod);
            long usedCount = canReward ? todayAiCount + 1 : todayAiCount;
            resultInfo.put("remaining_reward_count", Math.max(0, DAILY_ANALYSIS_LIMIT - (int) usedCount));
            resultInfo.put("analysis_id", analysisId);
            resultInfo.put("created_at", ZonedDateTime.now().toString());

            // 8️⃣ 분석 로그 저장
            RecycleLog recycleLog = new RecycleLog();
            recycleLog.setUser(user);
            recycleLog.setCategory(category);
            recycleLog.setDisposalCategory(category);
            recycleLog.setDisposalMethod(disposalMethod);
            recycleLog.setAnalysisId(analysisId);
            recycleLog.setCreatedAt(ZonedDateTime.now());
            recycleLogRepository.save(recycleLog);

            // 임시 파일 삭제
            Files.deleteIfExists(target);

        } catch (IOException e) {
            log.error("이미지 처리 중 에러: {}", e.getMessage());
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        } catch (Exception e) {
            log.error("AI 분석 중 에러: {}", e.getMessage());
            throw new RuntimeException("AI 분석 서비스 호출 중 오류 발생", e);
        }

        return resultInfo;
    }

    private String normalizeCategory(String raw) {
        if (raw == null || raw.isBlank()) return "플라스틱";
        switch (raw.toLowerCase().trim()) {
            case "pet": case "페트병": case "pet_bottle": case "pet bottle":
            case "bottle": case "plastic_bottle": case "plastic bottle":
            case "음료병": case "음료수병": case "투명페트":
                return "페트병";
            case "plastic": case "플라스틱": case "플라스틱용기": case "플라스틱 용기":
            case "plastic_container": case "요구르트":
                return "플라스틱";
            case "paper": case "종이": case "cardboard": case "박스":
            case "종이박스": case "신문지": case "종이류":
                return "종이";
            case "glass": case "유리": case "유리병": case "glass_bottle":
            case "glass bottle": case "유리류":
                return "유리";
            case "metal": case "캔": case "금속": case "can": case "aluminum":
            case "알루미늄": case "알루미늄캔": case "철캔": case "금속캔":
            case "tin": case "steel": case "캔류":
                return "캔";
            case "vinyl": case "비닐": case "vinyl_bag": case "plastic_bag":
            case "비닐봉지": case "비닐류": case "봉지":
                return "비닐";
            case "styrofoam": case "스티로폼": case "eps": case "foam":
            case "발포스티렌": case "스티로폼류":
                return "스티로폼";
            default:
                return "플라스틱";
        }
    }

    public List<RecycleLogResponse> getLogsByUser(Long userId) {
        List<RecycleLog> logs = recycleLogRepository.findByUserId(userId);

        long recycleCount = recycleLogRepository.countByUserId(userId);
        long totalPoints = pointRepository.findByUserId(userId)
                .map(Point::getPoints)
                .orElse(0);

        return logs.stream()
                .map(log -> RecycleLogResponse.builder()
                        .logId(log.getId())
                        .analysisId(log.getAnalysisId())
                        .category(log.getCategory())
                        .disposalCategory(log.getDisposalCategory())
                        .disposalMethod(log.getDisposalMethod())
                        .createdAt(log.getCreatedAt())
                        .recycleCount(recycleCount)
                        .totalPoints(totalPoints)
                        .message(log.getDisposalCategory() + " 분리수거가 기록되었습니다.")
                        .build()
                )
                .collect(Collectors.toList());
    }

    public Point getUserPointInfo(Long userId) {
        return pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 없습니다."));
    }

    public long getRecycleCountByUser(Long userId) {
        return recycleLogRepository.countByUserId(userId);
    }
}