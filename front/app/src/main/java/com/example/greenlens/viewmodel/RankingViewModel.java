package com.example.greenlens.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.response.LeaderboardResponse;
import com.example.greenlens.util.DevLog;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.greenlens.manager.UserManager;

public class RankingViewModel extends ViewModel {
    private final ApiService apiService;
    private final MutableLiveData<List<Map<String, Object>>> rankings = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalRecycleCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RankingViewModel(UserManager userManager) {
        this.apiService = userManager.getApiService(); // ✅ UserManager에서 가져오기
    }

    public LiveData<List<Map<String, Object>>> getRankings() {
        return rankings;
    }

    public LiveData<Integer> getTotalRecycleCount() {
        return totalRecycleCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadRankings(String token) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getLeaderboard(authToken).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    LeaderboardResponse leaderboardResponse = response.body();
                    List<Map<String, Object>> rankingsList = leaderboardResponse.getRankings();
                    rankings.setValue(rankingsList);

                    // 총 분리수거 횟수 계산
                    int totalCount = 0;
                    for (Map<String, Object> ranking : rankingsList) {
                        Object recycleCountObj = ranking.get("recycleCount");
                        if (recycleCountObj != null) {
                            if (recycleCountObj instanceof Double) {
                                totalCount += ((Double) recycleCountObj).intValue();
                            } else if (recycleCountObj instanceof Integer) {
                                totalCount += (Integer) recycleCountObj;
                            }
                        }
                    }
                    totalRecycleCount.setValue(totalCount);

                    DevLog.d("RankingViewModel", "랭킹 데이터 로드 성공: " + rankingsList.size() + "개");
                } else {
                    errorMessage.setValue("랭킹을 불러오는데 실패했습니다: " + response.code());
                    DevLog.e("RankingViewModel", "랭킹 데이터 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
                DevLog.e("RankingViewModel", "랭킹 데이터 로드 실패", t);
            }
        });
    }
}