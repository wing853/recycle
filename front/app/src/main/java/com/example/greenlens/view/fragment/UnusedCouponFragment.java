package com.example.greenlens.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlens.R;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.Coupon;
import com.example.greenlens.model.User;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.CouponUseActivity;
import com.example.greenlens.view.adapter.UnusedCouponAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnusedCouponFragment extends Fragment {
    private RecyclerView recyclerView;
    private UnusedCouponAdapter adapter;
    private ApiService apiService;
    private UserManager userManager;
    private static final int REQUEST_COUPON_USE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unused_coupon, container, false);

        // API 서비스 초기화
        apiService = ApiClient.getInstance(requireActivity().getApplication()).getApiService();;
        userManager = UserManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recycler_unused_coupons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UnusedCouponAdapter();
        recyclerView.setAdapter(adapter);

        // 쿠폰 사용 클릭 리스너 설정
        adapter.setOnCouponClickListener(this::onCouponUseClick);

        // 실제 데이터 로드
        loadUnusedCoupons();

        return view;
    }

    private void onCouponUseClick(Coupon coupon) {
        // CouponUseActivity로 이동
        Intent intent = new Intent(getActivity(), CouponUseActivity.class);
        intent.putExtra("couponId", coupon.getId());
        intent.putExtra("productName", coupon.getProductName());
        startActivityForResult(intent, REQUEST_COUPON_USE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COUPON_USE && resultCode == Activity.RESULT_OK) {
            // 쿠폰 사용 성공 시 목록 새로고침
            loadUnusedCoupons();
        }
    }

    private void loadUnusedCoupons() {
        if (!userManager.isLoggedIn()) {
            adapter.setCoupons(new ArrayList<>());
            return;
        }

        String authToken = userManager.getAuthToken();
        if (!authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        User currentUser = userManager.getCurrentUser();
        if (currentUser == null) {
            DevLog.e("UnusedCouponFragment", "Current user is null");
            adapter.setCoupons(new ArrayList<>());
            return;
        }

        Long userId = currentUser.getUserId();
        DevLog.d("UnusedCouponFragment", "Loading coupons for user ID: " + userId);

        apiService.getUserCoupons(userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    List<Map<String, Object>> unusedCoupons = (List<Map<String, Object>>) result.get("unusedCoupons");

                    if (unusedCoupons != null) {
                        List<Coupon> coupons = convertToCoupons(unusedCoupons);
                        adapter.setCoupons(coupons);
                    } else {
                        adapter.setCoupons(new ArrayList<>());
                    }
                } else {
                    DevLog.e("UnusedCouponFragment", "Failed to load coupons - Code: " + response.code());
                    adapter.setCoupons(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                DevLog.e("UnusedCouponFragment", "Failed to load coupons", t);
                adapter.setCoupons(new ArrayList<>());
            }
        });
    }

    private void loadTestData() {
        // 상점과 동일한 형태의 테스트 더미 데이터
        List<Coupon> testCoupons = new ArrayList<>();

        // 스타벅스 아메리카노
        Coupon starbucks = new Coupon("스타벅스", "아메리카노", 5000, "카페", "2025-12-31", R.drawable.img_americano);
        starbucks.setId(4L);
        testCoupons.add(starbucks);

        // CU 쿠키
        Coupon cuCookie = new Coupon("CU", "ABC초코쿠키쿠앤크", 1500, "편의점", "2025-12-31", R.drawable.img_cookie);
        cuCookie.setId(1L);
        testCoupons.add(cuCookie);

        adapter.setCoupons(testCoupons);
    }

    private List<Coupon> convertToCoupons(List<Map<String, Object>> couponData) {
        List<Coupon> coupons = new ArrayList<>();

        for (Map<String, Object> data : couponData) {
            try {
                Long id = ((Number) data.get("id")).longValue();
                String brandName = (String) data.get("brandName");
                String productName = (String) data.get("productName");
                String purchaseDate = (String) data.get("purchaseDate");

                Log.d("UnusedCouponFragment", "Converting coupon data - ID: " + id +
                        ", Brand: " + brandName +
                        ", Product: " + productName);

                Coupon coupon = new Coupon(brandName, productName, 0, "", purchaseDate, 0);
                coupon.setId(id);

                // 상품명에 따라 기본 이미지 설정
                int imageResId = getDefaultImageForProduct(productName);
                coupon.setImageResId(imageResId);

                coupons.add(coupon);
            } catch (Exception e) {
                Log.e("UnusedCouponFragment", "Error converting coupon data", e);
                e.printStackTrace();
            }
        }

        return coupons;
    }

    private int getDefaultImageForProduct(String productName) {
        if (productName == null) return R.drawable.img_cookie
                ;

        String lowerProductName = productName.toLowerCase();

        if (lowerProductName.contains("아메리카노")) {
            return R.drawable.img_americano;
        } else if (lowerProductName.contains("쿠키")) {
            return R.drawable.img_cookie;
        } else if (lowerProductName.contains("아이스박스")) {
            return R.drawable.img_icebox;
        } else if (lowerProductName.contains("라떼")) {
            return R.drawable.img_ediya;
        } else if (lowerProductName.contains("몬스터")) {
            return R.drawable.img_hollys;
        } else if (lowerProductName.contains("버거") || lowerProductName.contains("와퍼")) {
            return R.drawable.img_mac;
        } else if (lowerProductName.contains("불고기")) {
            return R.drawable.img_ria;
        } else if (lowerProductName.contains("팝콘")) {
            return R.drawable.img_corn;
        } else if (lowerProductName.contains("도시락")) {
            return R.drawable.img_lunchbox;
        } else if (lowerProductName.contains("영화") || lowerProductName.contains("관람권")) {
            return R.drawable.img_cgv;
        } else if (lowerProductName.contains("티켓")) {
            return R.drawable.img_locinema;
        } else {
            return R.drawable.img_cookie;
        }
    }
}

