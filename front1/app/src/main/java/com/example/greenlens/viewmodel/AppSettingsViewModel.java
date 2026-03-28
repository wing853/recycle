package com.example.greenlens.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.AppSettings;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppSettingsViewModel extends ViewModel {
    private ApiService apiService;

    private MutableLiveData<AppSettings> settings = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    public AppSettingsViewModel() {
        apiService = ApiClient.getInstance().getApiService();
        // 기본 설정값 설정
        settings.setValue(new AppSettings());
    }

    public LiveData<AppSettings> getSettings() {
        return settings;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isUpdateSuccess() {
        return updateSuccess;
    }

    public void loadSettings(String token) {
        loading.setValue(true);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getAppSettings(authToken).enqueue(new Callback<AppSettings>() {
            @Override
            public void onResponse(Call<AppSettings> call, Response<AppSettings> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    settings.setValue(response.body());
                } else {
                    errorMessage.setValue("설정을 불러오는데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<AppSettings> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    public void updateSettings(String token, AppSettings newSettings) {
        loading.setValue(true);
        updateSuccess.setValue(false);

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.updateAppSettings(authToken, newSettings).enqueue(new Callback<AppSettings>() {
            @Override
            public void onResponse(Call<AppSettings> call, Response<AppSettings> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    settings.setValue(response.body());
                    updateSuccess.setValue(true);
                } else {
                    errorMessage.setValue("설정 변경에 실패했습니다.");
                    updateSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<AppSettings> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류가 발생했습니다: " + t.getMessage());
                updateSuccess.setValue(false);
            }
        });
    }

    // 테마 변경
    public void updateTheme(String token, String newTheme) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setTheme(newTheme);
            updateSettings(token, currentSettings);
        }
    }

    // 알림 설정 변경
    public void updateNotifications(String token, boolean notifications) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setNotifications(notifications);
            updateSettings(token, currentSettings);
        }
    }

    // 언어 설정 변경
    public void updateLanguage(String token, String language) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setLanguage(language);
            updateSettings(token, currentSettings);
        }
    }
}