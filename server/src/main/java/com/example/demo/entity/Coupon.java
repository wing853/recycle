package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer points; // ✅ 필드명 기준 getter는 getPoints()

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer expireDays;

    @Column(nullable = false)
    private String description;

    // ✅ 명확한 의미 전달을 위한 getter alias (선택 사항)
    public int getAmount() {
        return this.points;
    }
}
