package com.example.greenlens.manager;

import static com.example.greenlens.constant.ConstPref.PREF_NAME;
import static com.example.greenlens.constant.ConstLog.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.model.User;
import com.example.greenlens.repository.UserRepository;
import com.example.greenlens.util.DevLog;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.greenlens.constant.ConstPref;
import com.example.greenlens.constant.ConstLog;
import org.json.JSONObject;

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
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        apiService = ApiClient.getInstance().getApiService();
        userRepository = UserRepository.getInstance(context);
        loadUser();
        loadToken();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
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

    public void saveToken(String token) {
        this.token = token;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    private void loadToken() {
        token = preferences.getString(KEY_TOKEN, null);
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        boolean tokenExists = token != null && !token.isEmpty();
        boolean isLoggedInPref = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean tokenNotExpired = !isTokenExpired();

        DevLog.d(TAG, "isLoggedIn check - tokenExists: " + tokenExists +
                ", isLoggedInPref: " + isLoggedInPref +
                ", tokenNotExpired: " + tokenNotExpired);

        // 토큰이 만료된 경우 세션 정리
        if (tokenExists && isLoggedInPref && !tokenNotExpired) {
            DevLog.d(TAG, "Token expired, clearing session");
            clearUserSession();
            return false;
        }

        // 메모리에 토큰이 있고, SharedPreferences에 로그인 상태가 저장되어 있고, 토큰이 만료되지 않았으면 로그인된 것으로 간주
        return tokenExists && isLoggedInPref && tokenNotExpired;
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
                callback.onError("네트워크 오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    public void updateUserProfile(User user, UserProfileCallback callback) {
        if (user == null || user.getUserId() == null || token == null) {
            callback.onError("사용자 정보가 올바르지 않습니다.");
            return;
        }

        apiService.updateUserProfile("Bearer " + token, user.getUserId(), user).enqueue(new Callback<User>() {
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
                callback.onError("네트워크 오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    public void logout() {
        currentUser = null;
        token = null;
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_EMAIL);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
        userRepository.clearUser();
    }

    public void logout(LogoutCallback callback) {
        try {
            logout();
            callback.onSuccess();
        } catch (Exception e) {
            DevLog.e(TAG, "Error during logout: " + e.getMessage());
            callback.onError("로그아웃 중 오류가 발생했습니다.");
        }
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface UserProfileCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void saveUserSession(String token, String email) {
        DevLog.d(TAG, "=== 사용자 세션 저장 시작 ===");
        DevLog.d(TAG, "받은 토큰: " + token);
        DevLog.d(TAG, "받은 이메일: " + email);

        // 토큰 저장 (Bearer 접두사 없이 원본 토큰만 저장)
        String tokenToSave = token;
        if (token != null && token.startsWith("Bearer ")) {
            tokenToSave = token.substring(7);
            DevLog.d(TAG, "Bearer 접두사 제거됨");
        }

        DevLog.d(TAG, "저장할 정제된 토큰: " + tokenToSave);
        this.token = tokenToSave;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, tokenToSave);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // 토큰 만료 시간은 서버에서 받아야 하므로 임시로 제거
        // TODO: 서버에서 토큰 만료 시간을 받아서 저장하도록 수정 필요
        // long expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24시간
        // editor.putLong("token_expires_at", expiresAt);

        editor.apply();
        DevLog.d(TAG, "SharedPreferences에 저장 완료");

        // 사용자 프로필 정보 가져오기
        fetchUserProfile(tokenToSave, new UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                // 프로필 정보 저장 완료
                DevLog.d(TAG, "사용자 프로필 가져오기 성공: " + user.getUsername());
                userRepository.saveUser(user);
            }

            @Override
            public void onError(String message) {
                // 에러 처리
                DevLog.e(TAG, "사용자 프로필 가져오기 실패: " + message);
                logout();
            }
        });
    }

    public boolean isTokenExpired() {
        if (token == null || token.isEmpty()) {
            DevLog.d(TAG, "토큰이 null 또는 비어있음 - 만료로 간주");
            return true;
        }

        try {
            // JWT 토큰은 header.payload.signature 형태
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                DevLog.e(TAG, "JWT 토큰 형식이 잘못됨 - 파트 수: " + parts.length);
                return true;
            }

            // payload 부분 디코딩
            String payload = parts[1];

            // Base64 URL-safe 디코딩을 위해 패딩 추가
            int padding = 4 - (payload.length() % 4);
            if (padding != 4) {
                for (int i = 0; i < padding; i++) {
                    payload += "=";
                }
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);

            DevLog.d(TAG, "JWT 페이로드 디코딩 성공: " + decodedPayload);

            JSONObject payloadJson = new JSONObject(decodedPayload);

            // exp 필드에서 만료 시간 확인 (Unix timestamp)
            if (payloadJson.has("exp")) {
                long expiration = payloadJson.getLong("exp");
                long currentTime = System.currentTimeMillis() / 1000; // 현재 시간을 초로 변환

                DevLog.d(TAG, "토큰 만료 시간: " + expiration + " (Unix timestamp)");
                DevLog.d(TAG, "현재 시간: " + currentTime + " (Unix timestamp)");
                DevLog.d(TAG, "토큰 만료까지 남은 시간: " + (expiration - currentTime) + "초");

                boolean isExpired = currentTime >= expiration;
                DevLog.d(TAG, "토큰 만료 여부: " + isExpired);

                return isExpired;
            } else {
                DevLog.e(TAG, "JWT 토큰에 exp 필드가 없음");
                return true;
            }

        } catch (Exception e) {
            DevLog.e(TAG, "토큰 만료 확인 중 오류 발생", e);
            return true;
        }
    }

    public String getAuthToken() {
        DevLog.d(TAG, "=== 인증 토큰 생성 ===");
        DevLog.d(TAG, "저장된 원본 토큰: " + token);

        // API 호출에 사용할 인증 토큰 반환 (Bearer 접두사 없이 순수 토큰만 반환)
        if (token == null || token.isEmpty()) {
            DevLog.e(TAG, "저장된 토큰이 null 또는 비어있음");
            return null;
        }
        return token;
    }

    public void clearUserSession() {
        DevLog.d(TAG, "Clearing user session");
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_EMAIL);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
        logout();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ApiService getApiService() {
        return apiService;
    }
} 