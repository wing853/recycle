package com.example.greenlens.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.databinding.ActivityLoginBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.request.LoginRequest;
import com.example.greenlens.model.response.LoginResponse;
import com.example.greenlens.util.DevLog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private UserManager userManager;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userManager = UserManager.getInstance(this);

        // 이미 로그인된 경우 메인 화면으로 이동
        if (userManager.isLoggedIn()) {
            DevLog.d(TAG, "이미 로그인 상태 → 메인으로 이동");
            startMainActivity();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvSignUp.setOnClickListener(v -> startSignupActivity());

        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google 로그인 준비 중입니다.", Toast.LENGTH_SHORT).show());

        binding.btnKakao.setOnClickListener(v ->
                Toast.makeText(this, "Kakao 로그인 준비 중입니다.", Toast.LENGTH_SHORT).show());

        binding.btnNaver.setOnClickListener(v ->
                Toast.makeText(this, "Naver 로그인 준비 중입니다.", Toast.LENGTH_SHORT).show());

        binding.tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "비밀번호 찾기 기능 준비 중입니다.", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        ApiService apiService = ApiClient.getInstance(this).getApiService();
        LoginRequest loginRequest = new LoginRequest(email, password);

        DevLog.d(TAG, "=== 로그인 API 호출 ===");

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                DevLog.d(TAG, "응답 코드: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String token = loginResponse.getToken();
                    String email = loginResponse.getEmail();

                    DevLog.d(TAG, "로그인 성공");
                    DevLog.d(TAG, "Token: " + token);
                    DevLog.d(TAG, "Email: " + email);

                    // 토큰 저장
                    userManager.saveUserSession(token, email);

                    // 저장 확인
                    DevLog.d(TAG, "저장된 토큰: " + userManager.getToken());
                    DevLog.d(TAG, "isLoggedIn: " + userManager.isLoggedIn());

                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                    startMainActivity();

                } else {
                    String errorMessage = "로그인 실패";

                    if (response.code() == 401) {
                        errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다.";
                    } else if (response.code() == 404) {
                        errorMessage = "존재하지 않는 계정입니다.";
                    } else if (response.code() == 500) {
                        errorMessage = "서버 오류입니다.";
                    }

                    DevLog.e(TAG, "로그인 실패: " + response.code());
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                DevLog.e(TAG, "네트워크 오류", t);
                Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
        binding.etPassword.setEnabled(!show);
        binding.tvForgotPassword.setEnabled(!show);
        binding.tvSignUp.setEnabled(!show);
        binding.btnGoogle.setEnabled(!show);
        binding.btnKakao.setEnabled(!show);
        binding.btnNaver.setEnabled(!show);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startSignupActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}