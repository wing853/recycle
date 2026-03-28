package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ 이메일로 사용자 리스트 조회 (대소문자 무시, 중복 대응)
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    List<User> findByEmail(@Param("email") String email);

    // ✅ 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // ✅ 사용자 ID로 상세 정보 조회 (추후 fetch join으로 확장 가능)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    java.util.Optional<User> findByIdWithDetails(@Param("userId") Long userId);
}

