package com.example.greenlens.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.greenlens.R;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.Coupon;
import com.example.greenlens.model.User;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;

import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopDetailActivity extends AppCompatActivity {

    private ImageView imageProduct;
    private TextView textBrand;
    private TextView textName;
    private TextView textPoint;
    private TextView textValidity;
    private TextView textGuide;
    private Button btnPurchase;
    private UserManager userManager;
    private Coupon coupon;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        // 유저 매니저 및 API 서비스 초기화
        userManager = UserManager.getInstance(this);
        apiService = ApiClient.getInstance().getApiService();

        // View 초기화
        imageProduct = findViewById(R.id.image_product);
        textBrand = findViewById(R.id.text_brand);
        textName = findViewById(R.id.text_name);
        textPoint = findViewById(R.id.text_point);
        textValidity = findViewById(R.id.text_validity);
        textGuide = findViewById(R.id.text_guide);
        btnPurchase = findViewById(R.id.btn_purchase);

        // 뒤로가기 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        if (intent != null) {
            Long couponId = intent.getLongExtra("COUPON_ID", -1L);
            String brandName = intent.getStringExtra("BRAND_NAME");
            String productName = intent.getStringExtra("PRODUCT_NAME");
            int points = intent.getIntExtra("POINTS", 0);
            String category = intent.getStringExtra("CATEGORY");
            int imageResId = intent.getIntExtra("IMAGE_RES_ID", 0);
            String expireDate = intent.getStringExtra("EXPIRE_DATE");

            // 가져온 데이터로 Coupon 객체 생성
            coupon = new Coupon(brandName, productName, points, category, expireDate, imageResId);
            if (couponId != -1L) {
                coupon.setId(couponId);
            }
            coupon.setValidityDays(30); // 기본 유효기간 설정

            // 데이터 설정
            setCouponData(coupon);

            // 구매 버튼 이벤트 설정
            setupPurchaseButton();
        } else {
            // Intent가 없는 경우 테스트용 더미 데이터 사용
            Coupon dummyCoupon = new Coupon(
                    "스타벅스",
                    "아메리카노",
                    4500,
                    "카페",
                    "2025-06-10까지",
                    R.drawable.ic_cafe
            );
            dummyCoupon.setId(4L); // 더미 데이터에도 ID 설정
            dummyCoupon.setValidityDays(30);
            coupon = dummyCoupon;

            // 데이터 설정
            setCouponData(dummyCoupon);

            // 구매 버튼 이벤트 설정
            setupPurchaseButton();
        }
    }

    private void setupPurchaseButton() {
        btnPurchase.setOnClickListener(v -> {
            // 사용자 포인트 확인
            User user = userManager.getCurrentUser();
            if (user != null) {
                // 스타벅스 아메리카노와 ABC 초코쿠키쿠앤크만 구매 가능
                boolean isStarbucksAmericano = coupon.getBrandName().equals("스타벅스") &&
                        (coupon.getProductName().equals("아메리카노") ||
                                coupon.getProductName().contains("아메리카노"));
                boolean isCUCookie = coupon.getBrandName().equals("CU") &&
                        (coupon.getProductName().equals("ABC초코쿠키쿠앤크") ||
                                coupon.getProductName().contains("쿠키"));

                if (isStarbucksAmericano || isCUCookie) {
                    int userPoints = user.getPoints();
                    if (userPoints >= coupon.getPoints()) {
                        // 구매 확인 다이얼로그 표시
                        showPurchaseConfirmDialog(userPoints);
                    } else {
                        // 포인트 부족 메시지
                        Toast.makeText(this,
                                "포인트가 부족합니다. 현재 보유 포인트: " + userPoints + "P",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 다른 상품권은 구현중 메시지 표시
                    Toast.makeText(this, "구현중입니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 로그인 필요 메시지
                Toast.makeText(this, "로그인이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPurchaseConfirmDialog(int userPoints) {
        new AlertDialog.Builder(this)
                .setTitle("상품권 구매")
                .setMessage(coupon.getProductName() + "을(를) " + coupon.getPoints() + "P로 구매하시겠습니까?\n" +
                        "구매 후 남은 포인트: " + (userPoints - coupon.getPoints()) + "P")
                .setPositiveButton("구매", (dialog, which) -> {
                    // 구매 처리 로직
                    performPurchase();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void performPurchase() {
        // 로그인 확인
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "로그인이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = userManager.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "인증 토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bearer 토큰 형식 확인 및 수정
        if (!authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        // 쿠폰 ID 확인
        if (coupon.getId() == null) {
            Toast.makeText(this, "쿠폰 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 사용자 포인트 재확인
        User currentUser = userManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getPoints() < coupon.getPoints()) {
            Toast.makeText(this, "포인트가 부족합니다. 현재 보유 포인트: " + currentUser.getPoints() + "P", Toast.LENGTH_SHORT).show();
            return;
        }

        // 구매 진행 중 로그
        android.util.Log.d("ShopDetailActivity", "Starting purchase - Coupon ID: " + coupon.getId() +
                ", Brand: " + coupon.getBrandName() +
                ", Product: " + coupon.getProductName() +
                ", Points: " + coupon.getPoints() +
                ", User Points: " + currentUser.getPoints());

        // 구매 API 호출
        apiService.purchaseCoupon(authToken, coupon.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                android.util.Log.d("ShopDetailActivity", "Purchase API response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();

                    // 응답 로그 출력
                    android.util.Log.d("ShopDetailActivity", "Purchase response: " + result.toString());

                    Boolean success = (Boolean) result.get("success");
                    if (success != null && success) {
                        // 사용자 포인트 업데이트
                        User currentUser = userManager.getCurrentUser();
                        if (currentUser != null) {
                            Object remainingPointsObj = result.get("remainingPoints");
                            if (remainingPointsObj instanceof Number) {
                                int remainingPoints = ((Number) remainingPointsObj).intValue();
                                currentUser.setPoints(remainingPoints);
                                userManager.saveUser(currentUser);
                                android.util.Log.d("ShopDetailActivity", "User points updated to: " + remainingPoints);
                            }
                        }

                        Toast.makeText(ShopDetailActivity.this,
                                coupon.getProductName() + " 구매가 완료되었습니다!",
                                Toast.LENGTH_SHORT).show();

                        // 구매 후 쿠폰함 화면으로 이동
                        Intent intent = new Intent(ShopDetailActivity.this, CouponHistoryActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = (String) result.getOrDefault("message", "구매에 실패했습니다.");
                        android.util.Log.e("ShopDetailActivity", "Purchase failed - Message: " + message);
                        Toast.makeText(ShopDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 에러 응답 처리
                    String errorMessage = "구매 처리 중 오류가 발생했습니다.";
                    if (response.code() == 401) {
                        errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
                    } else if (response.code() == 403) {
                        errorMessage = "권한이 없습니다.";
                    } else if (response.code() == 404) {
                        errorMessage = "쿠폰을 찾을 수 없습니다.";
                    } else if (response.code() == 400) {
                        errorMessage = "잘못된 요청입니다.";
                    } else if (response.code() == 500) {
                        errorMessage = "서버 내부 오류입니다. 잠시 후 다시 시도해주세요.";
                    }

                    android.util.Log.e("ShopDetailActivity", "Purchase failed - Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ShopDetailActivity", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("ShopDetailActivity", "Error reading error body", e);
                        }
                    }

                    Toast.makeText(ShopDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                android.util.Log.e("ShopDetailActivity", "Network error during purchase", t);
                String errorMessage = "네트워크 오류가 발생했습니다.";
                if (t.getMessage() != null) {
                    errorMessage += "\n" + t.getMessage();
                }
                Toast.makeText(ShopDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCouponData(Coupon coupon) {
        if (coupon != null) {
            // 이미지 설정
            if (coupon.getImageUrl() != null && !coupon.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(coupon.getImageUrl())
                        .into(imageProduct);
            } else if (coupon.getImageResId() != 0) {
                imageProduct.setImageResource(coupon.getImageResId());
            }

            // 브랜드명 설정
            textBrand.setText(coupon.getBrandName());

            // 상품명 설정
            textName.setText(String.format("[%s] %s",
                    coupon.getBrandName(),
                    coupon.getProductName()));

            // 포인트 설정
            textPoint.setText(String.format("%dP", coupon.getPoints()));

            // 유효기간 설정
            textValidity.setText(String.format("유효기간 %d일",
                    coupon.getValidityDays()));

            // 이용안내 설정
            textGuide.setText(getString(R.string.shop_detail_guide,
                    coupon.getBrandName()));
        }
    }
}