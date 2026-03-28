package com.example.greenlens.view.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlens.R;
import com.example.greenlens.model.Coupon;
import com.example.greenlens.view.CouponUseActivity;

import java.util.ArrayList;
import java.util.List;

public class UnusedCouponAdapter extends RecyclerView.Adapter<UnusedCouponAdapter.CouponViewHolder> {
    private List<Coupon> coupons = new ArrayList<>();
    private OnCouponClickListener listener;

    // 클릭 이벤트를 위한 인터페이스
    public interface OnCouponClickListener {
        void onUseClick(Coupon coupon);
    }

    public void setOnCouponClickListener(OnCouponClickListener listener) {
        this.listener = listener;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unused_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);
        holder.bind(coupon, listener);
    }

    @Override
    public int getItemCount() {
        return coupons.size();
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        private final ImageView couponImage;
        private final TextView brandText;
        private final TextView nameText;
        private final TextView dateText;
        private final TextView useButton;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponImage = itemView.findViewById(R.id.image_coupon);
            brandText = itemView.findViewById(R.id.text_brand);
            nameText = itemView.findViewById(R.id.text_name);
            dateText = itemView.findViewById(R.id.text_date);
            useButton = itemView.findViewById(R.id.btn_use);
        }

        public void bind(Coupon coupon, OnCouponClickListener listener) {
            String formattedName = String.format("[%s] %s",
                    coupon.getBrandName(),
                    coupon.getProductName());

            brandText.setText(coupon.getBrandName());
            nameText.setText(formattedName);
            dateText.setText(coupon.getExpireDate() + "까지");

            if (coupon.getImageResId() != 0) {
                couponImage.setImageResource(coupon.getImageResId());
            }

            useButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUseClick(coupon);
                }
            });
        }
    }
}
