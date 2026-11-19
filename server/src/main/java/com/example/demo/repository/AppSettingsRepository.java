package com.example.demo.repository;

import com.example.demo.entity.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
    Optional<AppSettings> findByUserId(Long userId);

    // ✅ 회원 탈퇴 시 사용자 데이터 일괄 삭제
    void deleteByUserId(Long userId);
}
