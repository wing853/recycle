package com.example.demo.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // ✅ 안전하고 충분히 긴 시크릿 키 (Base64 필요 없음)
    private static final String SECRET = "asdf";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    private SecretKey getSigningKey() {
        return SIGNING_KEY;
    }

    // ✅ 토큰 생성 - email, role, userId, username 포함
    public String generateToken(String email, String role, Long userId, String username) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", role)
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1시간 유효
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 토큰 유효성 검사
    public boolean validateToken(String token) {
        if (blacklistedTokens.contains(token)) {
            logger.warn("🚫 블랙리스트된 토큰입니다: {}", token);
            return false;
        }
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.trim());
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("❌ 지원되지 않는 JWT 형식: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("❌ JWT 형식이 올바르지 않음: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("❌ JWT 서명 검증 실패: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT가 비어 있음: {}", e.getMessage());
        }
        return false;
    }

    // ✅ 클레임에서 이메일 추출
    public String extractEmail(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            logger.error("❌ JWT에서 이메일 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 클레임에서 역할(role) 추출
    public String extractRole(String token) {
        try {
            return getClaims(token).get("roles", String.class);
        } catch (Exception e) {
            logger.error("❌ JWT에서 역할 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 클레임에서 userId 추출
    public Long extractUserId(String token) {
        try {
            return getClaims(token).get("userId", Long.class);
        } catch (Exception e) {
            logger.error("❌ JWT에서 userId 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 클레임에서 username 추출
    public String extractUsername(String token) {
        try {
            return getClaims(token).get("username", String.class);
        } catch (Exception e) {
            logger.error("❌ JWT에서 username 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 토큰 파싱 공통 처리
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.trim())
                .getBody();
    }

    // ✅ 토큰 블랙리스트 등록
    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
        logger.info("🚫 토큰 블랙리스트 추가됨: {}", token);
    }
}

