package com.example.greenlens.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.greenlens.view.fragment.UnusedCouponFragment;
import com.example.greenlens.view.fragment.UsedCouponFragment;

public class CouponPageAdapter extends FragmentStateAdapter {
    public CouponPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UnusedCouponFragment();
            case 1:
                return new UsedCouponFragment();
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}