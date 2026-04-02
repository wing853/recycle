package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.AppSettings;
import com.example.demo.entity.Point;
import com.example.demo.entity.User;
import com.example.demo.repository.AppSettingsRepository;
import com.example.demo.repository.PointRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PointRepository pointRepository;
    private final AppSettingsRepository appSettingsRepository;

    @Transactional
    public UserSignupResponse signup(UserSignupRequest request) {
        if (request.getEmail() == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("요청 데이터가 잘못되었습니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // ✅ 앱 설정 초기값 저장
        AppSettings defaultSettings = new AppSettings();
        defaultSettings.setUserId(savedUser.getId());
        defaultSettings.setTheme("light");
        defaultSettings.setNotifications(true);
        defaultSettings.setLanguage("ko");
        appSettingsRepository.save(defaultSettings);

        // ✅ 포인트 초기값 저장
        Point point = new Point();
        point.setUser(savedUser);
        point.setPoints(0);
        pointRepository.save(point);

        return UserSignupResponse.builder()
                .message("회원가입 성공!")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(ZonedDateTime.now().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        List<User> users = userRepository.findByEmail(request.getEmail());

        if (users.isEmpty() || !passwordEncoder.matches(request.getPassword(), users.get(0).getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        User user = users.get(0);
        String token = jwtService.generateToken(user.getEmail(), "USER", user.getId(), user.getUsername());

        return UserLoginResponse.builder()
                .message("로그인 성공!")
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getId())
                .token(token)
                .expiresIn(3600)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        userRepository.save(user);
        return new UserUpdateResponse("사용자 정보 수정 성공!");
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> getProfile(String email) {
        List<User> users = userRepository.findByEmail(email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileResponse(String email) {
        User user = userRepository.findByEmail(email)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int point = pointRepository.findByUserId(user.getId())
                .map(Point::getPoints)
                .orElse(0);

        int recycleCount = user.getRecycleLogs() != null
                ? user.getRecycleLogs().size()
                : 0;

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .points(point)
                .recycleCount(recycleCount)
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .authorities(roles)
                .build();
    }

    public PointUsageResponse usePoints(Long userId, PointUsageRequest request) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 없습니다."));

        if (request.getPointsToUse() > point.getPoints()) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        point.setPoints(point.getPoints() - request.getPointsToUse());
        pointRepository.save(point);

        return PointUsageResponse.builder()
                .message("포인트 사용 성공!")
                .remainingPoints(point.getPoints())
                .updatedAt(point.getUpdatedAt().toString())
                .build();
    }

    public Long getUserIdByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다.");
        }
        return users.get(0).getId();
    }

    @Transactional
    public AppSettingsResponse getAppSettings(Long userId) {
        AppSettings settings = appSettingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                AppSettings newSettings = new AppSettings();
                newSettings.setUserId(userId);
                newSettings.setTheme("light");
                newSettings.setNotifications(true);
                newSettings.setLanguage("ko");
                return appSettingsRepository.save(newSettings);
            });

        return AppSettingsResponse.builder()
                .theme(settings.getTheme())
                .notifications(settings.getNotifications())
                .language(settings.getLanguage())
                .build();
    }

    @Transactional
    public AppSettingsUpdateResponse updateAppSettings(Long userId, AppSettingsUpdateRequest request) {
        AppSettings settings = appSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    AppSettings newSettings = new AppSettings();
                    newSettings.setUserId(userId);
                    newSettings.setTheme("light");
                    newSettings.setNotifications(true);
                    newSettings.setLanguage("ko");
                    return newSettings;
                });

        if (request.getTheme() != null) settings.setTheme(request.getTheme());
        if (request.getNotifications() != null) settings.setNotifications(request.getNotifications());
        if (request.getLanguage() != null) settings.setLanguage(request.getLanguage());

        appSettingsRepository.save(settings);

        return new AppSettingsUpdateResponse(
                "설정이 변경되었습니다.",
                ZonedDateTime.now().toString()
        );
    }
}

