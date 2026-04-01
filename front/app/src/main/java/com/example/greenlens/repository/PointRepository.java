package com.example.greenlens.repository;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.Point;
import com.example.greenlens.model.response.PointResponse;
import com.example.greenlens.model.request.PointUseRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;

public class PointRepository {
    private static PointRepository instance;
    private final ApiService apiService;

    private PointRepository(Context context) {
        Context appContext = context.getApplicationContext(); // ✅ 이 줄 있어야 함
        apiService = ApiClient.getInstance(appContext).getApiService();
    }

    public static synchronized PointRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PointRepository(context);
        }
        return instance;
    }

    public void getUserPoints(String token, Long userId, PointCallback<PointResponse> callback) {
        apiService.getUserPoints(userId).enqueue(new Callback<PointResponse>() {
            @Override
            public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("포인트 정보를 가져오는데 실패했습니다: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PointResponse> call, Throwable t) {
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void usePoints(String token, Long userId, int pointsToUse, String reason, PointCallback<PointResponse> callback) {
        PointUseRequest request = new PointUseRequest(pointsToUse, reason);
        apiService.usePoints(userId, request).enqueue(new Callback<PointResponse>() {
            @Override
            public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("포인트 사용에 실패했습니다: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PointResponse> call, Throwable t) {
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void getPointHistory(String token, Long userId, PointCallback<List<Point>> callback) {
        apiService.getPointHistory(userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Map을 Point 객체로 변환해서 콜백에 전달하거나, 필요시 Map 그대로 전달
                    // 여기서는 예시로 Map 그대로 전달
                    // callback.onSuccess(response.body());
                } else {
                    callback.onError("포인트 내역 조회에 실패했습니다: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public interface PointCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }
}