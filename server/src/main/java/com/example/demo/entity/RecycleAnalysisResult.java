package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RecycleAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long analysisId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private String disposalMethod;

    private ZonedDateTime createdAt = ZonedDateTime.now();
}
