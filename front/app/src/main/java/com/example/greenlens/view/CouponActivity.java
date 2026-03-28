package com.example.greenlens.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.greenlens.R;
import com.example.greenlens.view.adapter.CouponPageAdapter;

public class CouponActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TextView tabUnused, tabUsed;
    private View tabIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_history);

        initViews();
        setupTabs();
        setupViewPager();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        viewPager = findViewById(R.id.viewPager);
        tabUnused = findViewById(R.id.tab_unused);
        tabUsed = findViewById(R.id.tab_used);
        tabIndicator = findViewById(R.id.tab_indicator);
    }

    private void setupTabs() {
        tabUnused.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabUsed.setOnClickListener(v -> viewPager.setCurrentItem(1));
    }

    private void setupViewPager() {
        CouponPageAdapter pagerAdapter = new CouponPageAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabs(position == 0);
            }
        });
    }

    private void updateTabs(boolean isUnused) {
        // 텍스트 색상 변경
        tabUnused.setTextColor(getColor(isUnused ? R.color.black : R.color.gray));
        tabUsed.setTextColor(getColor(isUnused ? R.color.gray : R.color.black));

        // 인디케이터 이동
        float translationX = isUnused ? 42 : viewPager.getWidth() / 2f + 42;  // 42dp는 layout에서 설정한 marginStart 값
        tabIndicator.animate()
                .translationX(translationX)
                .setDuration(300)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ViewPager2의 콜백 제거
        viewPager.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {});
    }
}