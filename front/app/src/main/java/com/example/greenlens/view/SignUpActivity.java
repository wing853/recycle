package com.example.greenlens.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.greenlens.R;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.databinding.ActivitySignUpBinding;
import com.example.greenlens.model.request.SignupRequest;
import com.example.greenlens.model.response.SignupResponse;
import com.example.greenlens.view.fragment.TermsFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import androidx.appcompat.app.AlertDialog;
import android.widget.ScrollView;
import android.widget.LinearLayout;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener(v -> finish());

        // 전체 동의 체크박스 설정
        binding.cbAllAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.cbTerms.setChecked(isChecked);
            binding.cbPrivacy.setChecked(isChecked);
        });

        // 개별 체크박스 변경 시 전체 동의 상태 업데이트
        setupCheckBoxListener();
    }

    private void setupCheckBoxListener() {
        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateAllAgreeState());
        binding.cbPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateAllAgreeState());
    }

    private void updateAllAgreeState() {
        boolean allChecked = binding.cbTerms.isChecked() &&
                binding.cbPrivacy.isChecked();
        binding.cbAllAgree.setChecked(allChecked);
    }

    private void setupClickListeners() {
        binding.btnSignUp.setOnClickListener(v -> {
            if (validateInputs() && checkRequiredAgreements()) {
                performSignUp();
            }
        });
        binding.btnBack.setOnClickListener(v -> finish());

        // 약관 상세보기 버튼들
        binding.tvTermsDetail.setOnClickListener(v -> showTermsDialog("service"));
        binding.tvPrivacyDetail.setOnClickListener(v -> showTermsDialog("privacy"));
    }

    private boolean validateInputs() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString().trim();
        String nickname = binding.etNickname.getText().toString().trim();

        // 이메일 검증
        if (!isValidEmail(email)) {
            binding.etEmail.setError("올바른 이메일 형식이 아닙니다");
            return false;
        }

        // 비밀번호 검증
        if (password.length() < 8) {
            binding.etPassword.setError("비밀번호는 8자 이상이어야 합니다");
            return false;
        }

        // 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            binding.etPasswordConfirm.setError("비밀번호가 일치하지 않습니다");
            return false;
        }

        // 닉네임 검증
        if (nickname.isEmpty()) {
            binding.etNickname.setError("닉네임을 입력해주세요");
            return false;
        }

        return true;
    }

    private boolean checkRequiredAgreements() {
        if (!binding.cbTerms.isChecked() || !binding.cbPrivacy.isChecked()) {
            Toast.makeText(this, "필수 약관에 동의해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performSignUp() {
        showLoading(true);
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String username = binding.etNickname.getText().toString().trim();

        ApiService apiService = ApiClient.getInstance(this).getApiService();
        SignupRequest signupRequest = new SignupRequest(username, email, password);

        try {
            apiService.signup(signupRequest).enqueue(new Callback<SignupResponse>() {
                @Override
                public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        startLoginActivity();
                    } else {
                        String errorMessage = "회원가입에 실패했습니다.";
                        if (response.code() == 409) {
                            errorMessage = "이미 가입된 이메일입니다.";
                        } else if (response.code() == 400) {
                            errorMessage = "잘못된 요청입니다.";
                        }
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SignupResponse> call, Throwable t) {
                    showLoading(false);
                    String errorMessage = "네트워크 오류가 발생했습니다: " + t.getMessage();
                    Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            String errorMessage = "예기치 않은 오류가 발생했습니다: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showTermsDialog(String type) {
        // 타입에 따라 제목 설정
        String title;
        String fileName;

        switch (type) {
            case "service":
                title = "서비스 이용약관";
                fileName = "terms_of_service.txt";
                break;
            case "privacy":
                title = "개인정보 처리방침";
                fileName = "privacy_policy.txt";
                break;
            default:
                title = "약관";
                fileName = "terms_of_service.txt";
                break;
        }

        // assets 폴더에서 약관 내용 로드
        String content = "";
        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }

            content = contentBuilder.toString();
            reader.close();
        } catch (IOException e) {
            content = "약관 내용을 불러오는데 실패했습니다.";
            e.printStackTrace();
        }

        // TextView를 LinearLayout으로 감싸기
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);

        TextView textView = new TextView(this);
        textView.setText(content);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setTextSize(14);
        textView.setPadding(40, 40, 40, 40);  // 여백 설정
        textView.setLineSpacing(0, 1.2f);  // 줄 간격 설정

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.WHITE);
        scrollView.addView(textView);
        linearLayout.addView(scrollView);

        // 다이얼로그 스타일 설정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setView(linearLayout)
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // 다이얼로그 타이틀 색상 설정
        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(Color.parseColor("#333333"));
        }

        // 다이얼로그 버튼 색상 설정
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.main_green));
        }
    }

    private void showLoading(boolean show) {
        binding.progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSignUp.setEnabled(!show);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}