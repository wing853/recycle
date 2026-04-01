package com.example.greenlens.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.Point;
import com.example.greenlens.model.User;
import com.example.greenlens.model.response.PointResponse;
import com.example.greenlens.manager.UserManager;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointViewModel extends ViewModel {
    private ApiService apiService;
    private UserManager userManager;

    private MutableLiveData<List<Map<String, Object>>> pointHistory = new MutableLiveData<>();
    private MutableLiveData<PointResponse> pointInfo = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PointViewModel(UserManager userManager) {
        this.userManager = userManager;
        this.apiService = userManager.getApiService(); // ✅ 여기 수정
    }

    public LiveData<List<Map<String, Object>>> getPointHistory() {
        return pointHistory;
    }

    public LiveData<PointResponse> getPointInfo() {
        return pointInfo;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadPointHistory(String token) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        Long userId = null;
        if (userManager != null && userManager.getCurrentUser() != null) {
            userId = userManager.getCurrentUser().getUserId();
        }

        if (userId == null) {
            loading.setValue(false);
            errorMessage.setValue("로그인 정보가 올바르지 않습니다.");
            return;
        }

        apiService.getPointHistory(userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    pointHistory.setValue(response.body());
                } else {
                    errorMessage.setValue("포인트 내역 불러오기 실패");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void loadUserPoints(String token, Long userId) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getUserPoints(userId).enqueue(new Callback<PointResponse>() {
            @Override
            public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    pointInfo.setValue(response.body());
                } else {
                    errorMessage.setValue("포인트 정보 불러오기 실패");
                }
            }

            @Override
            public void onFailure(Call<PointResponse> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void usePoints(String token, Long userId, int pointsToUse, String reason) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.usePoints(userId, new com.example.greenlens.model.request.PointUseRequest(pointsToUse, reason))
                .enqueue(new Callback<PointResponse>() {
                    @Override
                    public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            pointInfo.setValue(response.body());
                        } else {
                            errorMessage.setValue("포인트 사용 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<PointResponse> call, Throwable t) {
                        loading.setValue(false);
                        errorMessage.setValue("네트워크 오류: " + t.getMessage());
                    }
                });
    }
}