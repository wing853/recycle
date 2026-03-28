package com.example.greenlens.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.greenlens.R;
import com.example.greenlens.view.adapter.CouponPageAdapter;
import com.example.greenlens.databinding.ActivityCouponHistoryBinding;

public class CouponHistoryActivity extends AppCompatActivity {
    private ActivityCouponHistoryBinding binding;
    private ViewPager2 viewPager;
    private TextView tabUnused;
    private TextView tabUsed;
    private View tabIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCouponHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupViewPager();
        setupClickListeners();
        setupTabIndicator();
    }

    private void initViews() {
        viewPager = binding.viewPager;
        tabUnused = binding.tabUnused;
        tabUsed = binding.tabUsed;
        tabIndicator = binding.tabIndicator;

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        CouponPageAdapter pagerAdapter = new CouponPageAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabs(position);
                moveIndicator(position);
            }
        });
    }

    private void setupClickListeners() {
        tabUnused.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabUsed.setOnClickListener(v -> viewPager.setCurrentItem(1));
    }

    private void setupTabIndicator() {
        // 탭 텍스트의 크기가 결정된 후에 인디케이터 설정
        tabUnused.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 리스너 제거
                tabUnused.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // 초기 인디케이터 설정
                updateIndicator(tabUnused);
            }
        });
    }

    private void updateTabs(int position) {
        if (position == 0) {
            tabUnused.setTextColor(getColor(R.color.black));
            tabUsed.setTextColor(getColor(R.color.gray));
            updateIndicator(tabUnused);
        } else {
            tabUnused.setTextColor(getColor(R.color.gray));
            tabUsed.setTextColor(getColor(R.color.black));
            updateIndicator(tabUsed);
        }
    }

    private void moveIndicator(int position) {
        updateIndicator(position == 0 ? tabUnused : tabUsed);
    }

    private void updateIndicator(TextView selectedTab) {
        // 선택된 탭의 너비와 위치 계산
        int tabWidth = selectedTab.getWidth();
        float tabX = selectedTab.getX();

        // 탭이 FrameLayout 내에 있으므로 부모 FrameLayout의 X 좌표를 더해줌
        View parent = (View) selectedTab.getParent();
        tabX += parent.getX();

        // 인디케이터 너비 설정
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tabIndicator.getLayoutParams();
        params.width = tabWidth;
        tabIndicator.setLayoutParams(params);

        // 부드러운 이동 애니메이션 적용
        tabIndicator.animate()
                .x(tabX)
                .setDuration(300)  // 애니메이션 지속 시간 300ms
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}