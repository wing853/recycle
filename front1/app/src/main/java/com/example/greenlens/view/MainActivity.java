package com.example.greenlens.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.Navigation;

import com.example.greenlens.R;
import com.example.greenlens.databinding.ActivityMainBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.fragment.HomeFragmentDirections;
import com.example.greenlens.view.fragment.HomeFragmentDirections;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int CAMERA_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.bottomNavigationView.setBackground(null);

        // 토큰 상태 확인
        UserManager userManager = UserManager.getInstance(this);
        DevLog.d("MainActivity", "Token on startup: " + userManager.getToken());
        DevLog.d("MainActivity", "User is logged in: " + userManager.isLoggedIn());

        // NavHostFragment 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // BottomNavigationView와 NavController 연결
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        // FloatingActionButton 클릭 시 cameraFragment로 이동
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String guideType = data.getStringExtra("guide_type");
            if (guideType != null) {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                int currentDest = navController.getCurrentDestination().getId();
                if (currentDest != R.id.homeFragment) {
                    // HomeFragment로 이동
                    navController.popBackStack(R.id.homeFragment, false);
                }
                // HomeFragment가 최상단이 된 후에만 이동
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToRecycleGuideFragment(guideType));
            }
        }
    }
}