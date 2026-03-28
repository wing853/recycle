package com.example.greenlens.model;

import java.util.Date;

public class Coupon {
    private Long id;
    private String brandName;
    private String productName;
    private int points;
    private String category;
    private String expireDate;
    private int imageResId;
    private boolean isUsed;
    private String imageUrl;
    private int validityDays;

    public Coupon(String brandName, String productName, int points, String category,
                  String expireDate, int imageResId) {
        this.brandName = brandName;
        this.productName = productName;
        this.points = points;
        this.category = category;
        this.expireDate = expireDate;
        this.imageResId = imageResId;
        this.isUsed = false;
        this.validityDays = 30;
    }

    // Setter 메서드들
    public void setId(Long id) {
        this.id = id;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getter 메서드들
    public Long getId() { return id; }
    public String getBrandName() { return brandName; }
    public String getProductName() { return productName; }
    public int getPoints() { return points; }
    public String getCategory() { return category; }
    public String getExpireDate() { return expireDate; }
    public int getImageResId() { return imageResId; }
    public boolean isUsed() { return isUsed; }
    public String getImageUrl() { return imageUrl; }
    public int getValidityDays() { return validityDays; }

    // 쿠폰 관련 기능 메서드들
    public void use() {
        if (!isUsed && !isExpired()) {
            isUsed = true;
        }
    }

    public boolean isExpired() {
        // 만료 여부 체크 로직
        return false; // 임시 반환
    }

    public boolean isAvailable() {
        return !isUsed && !isExpired();
    }
}