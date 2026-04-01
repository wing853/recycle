package com.example.greenlens.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.Point;
import com.example.greenlens.util.DevLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointHistoryViewModel extends ViewModel {
    private static final String TAG = "PointHistoryViewModel";
    private final MutableLiveData<List<Map<String, Object>>> pointHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ApiService apiService;
    private final UserManager userManager;

    public PointHistoryViewModel(UserManager userManager) {
        this.userManager = userManager;
        this.apiService = userManager.getApiService();
    }

    public LiveData<List<Map<String, Object>>> getPointHistory() {
        return pointHistory;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadPointHistory() {
        if (!userManager.isLoggedIn()) {
            error.setValue("로그인이 필요합니다.");
            return;
        }

        String authToken = userManager.getAuthToken();
        Long userId = userManager.getCurrentUser() != null ? userManager.getCurrentUser().getUserId() : null;
        if (authToken == null || authToken.isEmpty() || userId == null) {
            error.setValue("로그인 세션이 만료되었습니다.");
            return;
        }
        // Bearer가 이미 붙어있으면 추가하지 않음
        String finalToken = authToken.startsWith("Bearer ") ? authToken : "Bearer " + authToken;

        isLoading.setValue(true);
        DevLog.d(TAG, "포인트 내역 불러오기 시작...");

        apiService.getPointHistory(finalToken, userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> history = response.body();
                    DevLog.d(TAG, "포인트 내역 응답 성공: " + history.size() + "개 항목");

                    // 적립(type=="적립")이면서 brandName==null인 항목은 제외
                    List<Map<String, Object>> filteredHistory = new ArrayList<>();
                    for (Map<String, Object> item : history) {
                        String type = (String) item.get("type");
                        String brandName = (String) item.get("brandName");
                        if ("적립".equals(type) && brandName == null) {
                            continue;
                        }
                        filteredHistory.add(item);
                    }

                    // 날짜 기준 최신순(내림차순) 정렬
                    filteredHistory.sort((a, b) -> {
                        String dateA = (String) a.get("date");
                        String dateB = (String) b.get("date");
                        if (dateA == null) return 1;
                        if (dateB == null) return -1;
                        return dateB.compareTo(dateA); // 내림차순
                    });

                    pointHistory.setValue(filteredHistory);
                    DevLog.d(TAG, "포인트 내역 " + filteredHistory.size() + "개 로드 완료");
                } else {
                    error.setValue("포인트 내역을 불러올 수 없습니다.");
                    DevLog.e(TAG, "포인트 내역 조회 실패: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            DevLog.e(TAG, "에러 응답: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("네트워크 오류: " + t.getMessage());
                DevLog.e(TAG, "포인트 내역 로드 실패: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
