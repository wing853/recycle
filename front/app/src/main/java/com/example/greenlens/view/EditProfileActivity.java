package com.example.greenlens.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.databinding.ActivityEditProfileBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.User;
import com.example.greenlens.model.response.PointResponse;
import com.example.greenlens.repository.UserRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private UserManager userManager;
    private ApiService apiService;
    private User currentUser;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userManager = UserManager.getInstance(this);
        apiService = ApiClient.getInstance(this).getApiService();
        userRepository = UserRepository.getInstance(this);

        setupViews();
        loadUserProfile();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> updateProfile());
    }

    private void loadUserProfile() {
        showLoading(true);
        String token = userManager.getToken();

        if (token == null) {
            showLoading(false);
            Toast.makeText(EditProfileActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userManager.fetchUserProfile(token, new UserManager.UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);
                currentUser = user;
                updateUI(currentUser);
                // 사용자 정보 로드 후 포인트 조회
                if (user.getUserId() != null) {
                    loadUserPoints(token, user.getUserId());
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "프로필 정보를 불러오는데 실패했습니다: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPoints(String token, Long userId) {
        if (userId == null) {
            return;
        }

        if (token == null) {
            return;
        }

        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.getUserPoints(userId).enqueue(new Callback<PointResponse>() {
            @Override
            public void onResponse(Call<PointResponse> call, Response<PointResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PointResponse pointResponse = response.body();
                    binding.tvPoints.setText(String.valueOf(pointResponse.getPoints()));
                }
            }

            @Override
            public void onFailure(Call<PointResponse> call, Throwable t) {
                // 오류 발생 시 처리 (User 객체의 포인트를 사용)
                if (currentUser != null) {
                    binding.tvPoints.setText(String.valueOf(currentUser.getPoints()));
                }
            }
        });
    }

    private void updateUI(User user) {
        binding.etNickname.setText(user.getUsername());
        binding.etEmail.setText(user.getEmail());
        binding.tvPoints.setText(String.valueOf(user.getPoints()));
        binding.tvRecycleCount.setText(String.valueOf(user.getRecycleCount()));
    }

    private void updateProfile() {
        if (!validateInputs()) return;

        showLoading(true);
        String token = userManager.getToken();

        if (token == null) {
            showLoading(false);
            Toast.makeText(EditProfileActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUser.getUserId() == null) {
            showLoading(false);
            Toast.makeText(EditProfileActivity.this, "사용자 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 사용자 정보 업데이트
        currentUser.setUsername(binding.etNickname.getText().toString());
        currentUser.setEmail(binding.etEmail.getText().toString());

        // 사용자 정보 수정 요청
        String authToken = token;
        if (!token.startsWith("Bearer ")) {
            authToken = "Bearer " + token;
        }

        apiService.updateUserProfile(currentUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // 수정된 사용자 정보를 로컬에 저장
                    User updatedUser = response.body();
                    userRepository.saveUser(updatedUser);
                    userManager.saveUser(updatedUser);

                    Toast.makeText(EditProfileActivity.this, "프로필이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();

                    // 이전 화면으로 돌아가기
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "프로필 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        String nickname = binding.etNickname.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        if (nickname.isEmpty()) {
            binding.etNickname.setError("닉네임을 입력해주세요");
            return false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("이메일을 입력해주세요");
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}