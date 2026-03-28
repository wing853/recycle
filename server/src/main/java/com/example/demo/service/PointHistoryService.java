package com.example.demo.service;

import com.example.demo.dto.PointHistoryResponse;
import com.example.demo.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository historyRepository;

    /** 전체 내역 조회 (페이징 없이 전체 불러오기) */
    public List<PointHistoryResponse> getHistory(Long userId) {
        return historyRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    /** 페이징 지원 내역 조회 (선택적 사용) */
    public Page<PointHistoryResponse> getHistory(Long userId, Pageable pageable) {
        return historyRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .map(PointHistoryResponse::from);
    }
}
