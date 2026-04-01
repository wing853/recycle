package com.example.greenlens.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.AppSettings;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppSettingsViewModel extends AndroidViewModel {
    private ApiService apiService;

    private MutableLiveData<AppSettings> settings = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    public AppSettingsViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getInstance(application).getApiService();
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
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
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
                    errorMessage.setValue("설정 변경 실패");
                    updateSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<AppSettings> call, Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("네트워크 오류: " + t.getMessage());
                updateSuccess.setValue(false);
            }
        });
    }

    public void updateTheme(String token, String newTheme) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setTheme(newTheme);
            updateSettings(token, currentSettings);
        }
    }

    public void updateNotifications(String token, boolean notifications) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setNotifications(notifications);
            updateSettings(token, currentSettings);
        }
    }

    public void updateLanguage(String token, String language) {
        AppSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setLanguage(language);
            updateSettings(token, currentSettings);
        }
    }
}