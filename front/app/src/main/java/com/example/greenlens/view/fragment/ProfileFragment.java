package com.example.greenlens.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenlens.R;
import com.example.greenlens.model.User;
import com.example.greenlens.repository.UserRepository;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.PointHistoryActivity;
import com.example.greenlens.view.CouponHistoryActivity;
import com.example.greenlens.view.SettingActivity;
import com.example.greenlens.manager.UserManager;

public class ProfileFragment extends Fragment {
    private TextView tvNickname;
    private TextView tvEmail;
    private TextView tvPoint;
    private View btnPoint;
    private View btnCoupon;
    private View btnSettings;
    private View layoutPoint;
    private UserRepository userRepository;
    private UserManager userManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance(requireContext());
        userManager = UserManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews(View view) {
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPoint = view.findViewById(R.id.tv_point);
        btnPoint = view.findViewById(R.id.btn_point);
        btnCoupon = view.findViewById(R.id.btn_coupon);
        btnSettings = view.findViewById(R.id.btn_settings);
        layoutPoint = view.findViewById(R.id.layout_point);
    }

    private void setupClickListeners() {
        // 포인트 영역 클릭 시 포인트 내역 화면으로 이동
        layoutPoint.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PointHistoryActivity.class);
            startActivity(intent);
        });

        // 포인트 버튼 클릭 시 포인트 내역 화면으로 이동
        btnPoint.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PointHistoryActivity.class);
            startActivity(intent);
        });

        // 쿠폰함 버튼 클릭 시 쿠폰 내역 화면으로 이동
        btnCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CouponHistoryActivity.class);
            startActivity(intent);
        });

        // 설정 버튼 클릭 시 설정 화면으로 이동
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        String token = userManager.getToken();
        DevLog.d("ProfileFragment", "loadUserInfo - Token: " + token);

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            tvNickname.setText("로그인이 필요합니다");
            tvEmail.setText("");
            tvPoint.setText("0P");
            return;
        }

        DevLog.d("ProfileFragment", "Fetching user profile with token: " + token);
        userRepository.fetchUserProfile(token, new UserRepository.UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() == null || !isAdded()) return;

                DevLog.d("ProfileFragment", "User profile fetched successfully: " + user.getUsername());
                tvNickname.setText(user.getUsername());
                tvEmail.setText(user.getEmail());
                tvPoint.setText(String.format("%d P", user.getPoints()));
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null || !isAdded()) return;
                DevLog.e("ProfileFragment", "Error fetching user profile: " + message);
                // 토큰으로 사용자 정보를 가져오는데 실패한 경우, 로그인 상태를 초기화
                userManager.clearUserSession();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // UI 업데이트
                tvNickname.setText("로그인이 필요합니다");
                tvEmail.setText("");
                tvPoint.setText("0P");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 사용자 정보 갱신
        loadUserInfo();
    }
}