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
        this.apiService = ApiClient.getInstance().getApiService();
        this.userManager = userManager;
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

        String finalToken = authToken.startsWith("Bearer ") ? authToken : "Bearer " + authToken;

        Long userId = null;
        try {
            if (userManager != null && userManager.getCurrentUser() != null) {
                userId = userManager.getCurrentUser().getUserId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userId == null) {
            loading.setValue(false);
            errorMessage.setValue("로그인 정보가 올바르지 않습니다.");
            return;
        }

        apiService.getPointHistory(finalToken, userId).enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Map<String, Object>>> call, retrofit2.Response<List<Map<String, Object>>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    pointHistory.setValue(response.body());
                } else {
                    errorMessage.setValue("포인트 내역을 불러오는데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Map<String, Object>>> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    public void loadUserPoints(String token, Long userId) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getUserPoints(authToken, userId).enqueue(new Callback<PointResponse>() {
            @Override
            public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    pointInfo.setValue(response.body());
                } else {
                    errorMessage.setValue("포인트 정보를 불러오는데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<PointResponse> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    public void usePoints(String token, Long userId, int pointsToUse, String reason) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.usePoints(authToken, userId, new com.example.greenlens.model.request.PointUseRequest(pointsToUse, reason))
                .enqueue(new Callback<PointResponse>() {
                    @Override
                    public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            pointInfo.setValue(response.body());
                        } else {
                            errorMessage.setValue("포인트 사용에 실패했습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<PointResponse> call, Throwable t) {
                        loading.setValue(false);
                        errorMessage.setValue("네트워크 오류가 발생했습니다: " + t.getMessage());
                    }
                });
    }
}