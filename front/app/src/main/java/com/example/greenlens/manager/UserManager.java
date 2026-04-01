package com.example.greenlens.manager;

import static com.example.greenlens.constant.ConstPref.PREF_NAME;
import static com.example.greenlens.constant.ConstLog.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.User;
import com.example.greenlens.repository.UserRepository;
import com.example.greenlens.util.DevLog;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManager {

    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static UserManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final ApiService apiService;
    private User currentUser;
    private String token;
    private UserRepository userRepository;

    private UserManager(Context context) {
        Context appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        apiService = ApiClient.getInstance(appContext).getApiService();
        userRepository = UserRepository.getInstance(appContext);

        loadUser();
        loadToken();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    // -------------------------
    // 사용자 정보 저장/로드
    // -------------------------
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

    // -------------------------
    // 토큰 관리
    // -------------------------
    public void saveToken(String token) {
        this.token = token;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.commit(); // 동기 저장
    }

    private void loadToken() {
        token = preferences.getString(KEY_TOKEN, null);
    }

    public String getToken() {
        if (token == null) {
            token = preferences.getString(KEY_TOKEN, null);
        }
        return token;
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        String savedToken = getToken();
        boolean tokenExists = savedToken != null && !savedToken.isEmpty();
        boolean isLoggedInPref = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean tokenNotExpired = !isTokenExpired();

        DevLog.d(TAG, "isLoggedIn check - tokenExists: " + tokenExists +
                ", isLoggedInPref: " + isLoggedInPref +
                ", tokenNotExpired: " + tokenNotExpired);

        if (tokenExists && isLoggedInPref && !tokenNotExpired) {
            DevLog.d(TAG, "Token expired, clearing session");
            clearUserSession();
            return false;
        }

        return tokenExists && isLoggedInPref && tokenNotExpired;
    }

    // -------------------------
    // 로그인 세션 저장
    // -------------------------
    public void saveUserSession(String token, String email) {
        DevLog.d(TAG, "=== 사용자 세션 저장 시작 ===");

        String tokenToSave = token;
        if (token != null && token.startsWith("Bearer ")) {
            tokenToSave = token.substring(7);
        }

        this.token = tokenToSave;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, tokenToSave);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit(); // ⭐ 매우 중요 (동기 저장)

        DevLog.d(TAG, "SharedPreferences에 저장 완료");

        // 프로필 가져오기 (실패해도 로그아웃 하지 않음)
        fetchUserProfile(tokenToSave, new UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                DevLog.d(TAG, "사용자 프로필 가져오기 성공");
                saveUser(user);
                userRepository.saveUser(user);
            }

            @Override
            public void onError(String message) {
                DevLog.e(TAG, "사용자 프로필 가져오기 실패: " + message);
                // ❌ logout() 제거
            }
        });
    }

    // -------------------------
    // 사용자 프로필 조회
    // -------------------------
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
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }

    // -------------------------
    // 로그아웃
    // -------------------------
    public void logout() {
        currentUser = null;
        token = null;

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_EMAIL);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.commit();

        userRepository.clearUser();
    }

    public void clearUserSession() {
        DevLog.d(TAG, "Clearing user session");
        logout();
    }

    // -------------------------
    // JWT 만료 체크
    // -------------------------
    public boolean isTokenExpired() {
        String savedToken = getToken();

        if (savedToken == null || savedToken.isEmpty()) {
            DevLog.d(TAG, "토큰이 null 또는 비어있음 - 만료로 간주");
            return true;
        }

        try {
            String[] parts = savedToken.split("\\.");
            if (parts.length != 3) return true;

            String payload = parts[1];

            int padding = 4 - (payload.length() % 4);
            if (padding != 4) {
                for (int i = 0; i < padding; i++) {
                    payload += "=";
                }
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);
            JSONObject payloadJson = new JSONObject(decodedPayload);

            long expiration = payloadJson.getLong("exp");
            long currentTime = System.currentTimeMillis() / 1000;

            return currentTime >= expiration;

        } catch (Exception e) {
            DevLog.e(TAG, "토큰 만료 확인 중 오류", e);
            return true;
        }
    }

    public String getAuthToken() {
        return getToken();
    }

    public ApiService getApiService() {
        return apiService;
    }

    public void logout(LogoutCallback callback) {
        try {
            logout(); // 기존 logout 호출
            if (callback != null) {
                callback.onSuccess();
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("로그아웃 중 오류 발생");
            }
        }
    }

    // -------------------------
    // 콜백 인터페이스
    // -------------------------
    public interface LogoutCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface UserProfileCallback {
        void onSuccess(User user);
        void onError(String message);
    }
}

