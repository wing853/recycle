package com.example.greenlens.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.User;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER = "user";
    private static UserRepository instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final ApiService apiService;
    private User currentUser;

    private UserRepository(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        apiService = ApiClient.getInstance().getApiService();
        loadUser();
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(User user) {
        currentUser = user;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    private void loadUser() {
        String userJson = preferences.getString(KEY_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void fetchUserProfile(String token, UserProfileCallback callback) {
        String authToken = token;
        if (token != null && !token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getUserProfile(authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    saveUser(user);
                    callback.onSuccess(user);
                } else {
                    callback.onError("사용자 정보를 가져오는데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("네트워크 오류가 발생했습니다.");
            }
        });
    }

    public void updateUserProfile(String token, User user, UserProfileCallback callback) {
        if (user == null || user.getUserId() == null) {
            callback.onError("사용자 정보가 올바르지 않습니다.");
            return;
        }

        String authToken = token;
        if (token != null && !token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.updateUserProfile(authToken, user.getUserId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    saveUser(updatedUser);
                    callback.onSuccess(updatedUser);
                } else {
                    callback.onError("사용자 정보 수정에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("네트워크 오류가 발생했습니다.");
            }
        });
    }

    public void clearUser() {
        currentUser = null;
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER);
        editor.apply();
    }

    public interface UserProfileCallback {
        void onSuccess(User user);
        void onError(String message);
    }
}