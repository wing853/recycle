package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Point;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://recycle-9bar.onrender.com")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RecycleService recycleService;
    private final PointHistoryService pointHistoryService;
    private final UserCouponService userCouponService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest request) {
        try {
            UserSignupResponse response = userService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace(); // ★ 이거 추가
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"잘못된 토큰입니다.\"}");
        }
        String jwt = token.substring(7);
        if (!jwtService.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"유효하지 않은 토큰입니다.\"}");
        }
        jwtService.invalidateToken(jwt);
        return ResponseEntity.ok().body("{\"message\": \"로그아웃 성공!\"}");
    }

    @GetMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserById(@PathVariable Long user_id) {
        Optional<User> user = userService.getUserById(user_id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"사용자를 찾을 수 없습니다.\"}");
        }
        return ResponseEntity.ok(user.get());
    }

    @PutMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUser(@PathVariable Long user_id, @RequestBody UserUpdateRequest request) {
        try {
            UserUpdateResponse response = userService.updateUser(user_id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteUser(@PathVariable("user_id") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().body("{\"message\": \"계정 삭제 성공!\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        User user = userDetails.getUser();
        Long userId = user.getId();

        int points = recycleService.getUserPointInfo(userId).getPoints();
        long recycleCount = recycleService.getRecycleCountByUser(userId);

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .points(points)
                .recycleCount(recycleCount)
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .authorities(user.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{user_id}/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserPoints(@PathVariable("user_id") Long userId) {
        try {
            Point point = recycleService.getUserPointInfo(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("user_id", point.getUser().getId());
            response.put("points", point.getPoints());
            response.put("last_updated", point.getUpdatedAt().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/{user_id}/points/use")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> usePoints(@PathVariable("user_id") Long userId,
                                       @RequestBody PointUsageRequest request) {
        try {
            PointUsageResponse response = userService.usePoints(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAppSettings(@AuthenticationPrincipal User user) {
        Long userId = userDetails.getUser().getId();
        try {
            AppSettingsResponse response = userService.getAppSettings(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /** ✅ 앱 설정 변경 */
    @PutMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAppSettings(
            @AuthenticationPrincipal User user,
            @RequestBody AppSettingsUpdateRequest request) {
        Long userId = userDetails.getUser().getId();
        try {
            AppSettingsUpdateResponse response = userService.updateAppSettings(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/points/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal User user) {
        if (!userDetails.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<PointHistoryResponse> history = pointHistoryService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{userId}/coupons")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserCouponBoxResponse> getUserCoupons(
            @PathVariable Long userId,
            @AuthenticationPrincipal User user) {
        if (!userDetails.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserCouponBoxResponse response = userCouponService.getUserCoupons(userId);
        return ResponseEntity.ok(response);
    }
}
