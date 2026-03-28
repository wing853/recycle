package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.greenlens.view.adapter.UsedCouponAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsedCouponFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsedCouponAdapter adapter;
    private ApiService apiService;
    private UserManager userManager;
    private static final String TAG = "UsedCouponFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_used_coupon, container, false);

        // API 서비스 초기화
        apiService = ApiClient.getInstance().getApiService();
        userManager = UserManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recycler_used_coupons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UsedCouponAdapter();
        recyclerView.setAdapter(adapter);

        // 실제 데이터 로드
        loadUsedCoupons();

        return view;
    }

    private void loadUsedCoupons() {
        if (!userManager.isLoggedIn()) {
            DevLog.d(TAG, "User not logged in");
            adapter.setCoupons(new ArrayList<>());
            return;
        }

        String authToken = userManager.getAuthToken();
        if (!authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        User currentUser = userManager.getCurrentUser();
        if (currentUser == null) {
            DevLog.e(TAG, "Current user is null");
            adapter.setCoupons(new ArrayList<>());
            return;
        }

        Long userId = currentUser.getUserId();
        DevLog.d(TAG, "Loading used coupons for user ID: " + userId);

        apiService.getUserCoupons(authToken, userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                DevLog.d(TAG, "API Response - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    DevLog.d(TAG, "Response body: " + result.toString());

                    List<Map<String, Object>> usedCoupons = (List<Map<String, Object>>) result.get("usedCoupons");
                    DevLog.d(TAG, "Used coupons data: " + usedCoupons);

                    if (usedCoupons != null && !usedCoupons.isEmpty()) {
                        List<Coupon> coupons = convertToCoupons(usedCoupons);
                        DevLog.d(TAG, "Converted " + coupons.size() + " used coupons");
                        adapter.setCoupons(coupons);
                    } else {
                        DevLog.d(TAG, "No used coupons found");
                        adapter.setCoupons(new ArrayList<>());
                    }
                } else {
                    DevLog.e(TAG, "Failed to load used coupons - Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            DevLog.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            DevLog.e(TAG, "Error reading error body", e);
                        }
                    }
                    adapter.setCoupons(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                DevLog.e(TAG, "Failed to load used coupons", t);
                adapter.setCoupons(new ArrayList<>());
            }
        });
    }

    private List<Coupon> convertToCoupons(List<Map<String, Object>> couponData) {
        List<Coupon> coupons = new ArrayList<>();

        for (Map<String, Object> data : couponData) {
            try {
                Long id = ((Number) data.get("id")).longValue();
                String brandName = (String) data.get("brandName");
                String productName = (String) data.get("productName");
                String purchaseDate = (String) data.get("purchaseDate");

                Log.d(TAG, "Converting used coupon data - ID: " + id +
                        ", Brand: " + brandName +
                        ", Product: " + productName +
                        ", Purchase Date: " + purchaseDate);

                Coupon coupon = new Coupon(brandName, productName, 0, "", purchaseDate, 0);
                coupon.setId(id);

                // 상품명에 따라 기본 이미지 설정
                int imageResId = getDefaultImageForProduct(productName);
                coupon.setImageResId(imageResId);

                coupons.add(coupon);
            } catch (Exception e) {
                Log.e(TAG, "Error converting used coupon data", e);
                e.printStackTrace();
            }
        }

        return coupons;
    }

    private int getDefaultImageForProduct(String productName) {
        if (productName == null) return R.drawable.img_cookie;

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