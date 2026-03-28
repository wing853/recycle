package com.example.demo.repository;

import com.example.demo.entity.RecycleAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecycleAnalysisResultRepository extends JpaRepository<RecycleAnalysisResult, Long> {
    List<RecycleAnalysisResult> findByAnalysisId(Long analysisId);
}
