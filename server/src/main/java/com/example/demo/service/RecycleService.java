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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.nio.charset.StandardCharsets;
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
    // private static final int DAILY_POINT_LIMIT = 50;
    // private static final int DAILY_ANALYSIS_LIMIT = 5;
    // private static final int ANALYSIS_REWARD = 10;


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

        // 포인트 지급 로직
        int pointsEarned = 100;  // 또는 일일 제한/분석 횟수 체크 후 결정

        // 기존 포인트 조회 or 새로 생성
        Point point = pointRepository.findByUserId(userId)
            .orElseGet(() -> {
                Point p = new Point();
                p.setUser(user);
                p.setPoints(0);
                return p;
            });

        // 포인트 누적 지급
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

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${script.python-exe}")
    private String pythonExe;

    @Value("${script.path}")
    private String scriptPath;

    @Value("${script.weight}")
    private String weightPath;

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Transactional
    public Map<String, Object> analyzeAndSave(MultipartFile image, Long userId) {
        Map<String, Object> resultInfo = new HashMap<>();
        try {
            Long analysisId = System.currentTimeMillis();  // millisecond 단위 ID

            // 1️⃣ 이미지 저장
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = StringUtils.getFilename(image.getOriginalFilename());
            String ascii = Normalizer.normalize(original, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = UUID.randomUUID() + "_" + ascii;
            Path target = uploadPath.resolve(fileName);
            image.transferTo(target);

            // 2️⃣ Python API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new FileSystemResource(target.toFile()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(aiApiUrl, requestEntity, Map.class);

            // 3️⃣ 결과 파싱
            Map<String, Object> resultMap = response.getBody();
            String category = (String) resultMap.getOrDefault("category", "unknown");
            double confidence = Double.parseDouble(resultMap.getOrDefault("confidence", 0.0).toString());
            String disposalMethod = (String) resultMap.getOrDefault("disposal_method", "일반 쓰레기통에 버려주세요.");

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

            resultInfo.put("remaining_reward_count", Math.max(0, DAILY_ANALYSIS_LIMIT - todayAiCount.intValue()));
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

        } catch (IOException e) {
            log.error("이미지 처리 중 IOException 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        }

        return resultInfo;
    }

    // private String runPythonScript(String imgPath) {
    //     ProcessBuilder pb = new ProcessBuilder(
    //             pythonExe,
    //             scriptPath,
    //             "--image", imgPath
    //     );
    //     pb.redirectErrorStream(true);

    //     try {
    //         Process proc = pb.start();
    //         String output;
    //         try (BufferedReader br = new BufferedReader(
    //                 new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
    //             output = br.lines().collect(Collectors.joining("\n"));
    //         }
    //         int exit = proc.waitFor();
    //         log.info("PYTHON exit={} cmd={}", exit, pb.command());

    //         if (exit != 0) {
    //             return "error: script failed with exit code " + exit + "\n" + output;
    //         }
    //         return output;
    //     } catch (IOException | InterruptedException e) {
    //         return "error:" + e.getMessage();
    //     }
    // }

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
