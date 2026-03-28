package com.example.greenlens.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlens.R;
import com.example.greenlens.model.Coupon;

import java.util.ArrayList;
import java.util.List;

public class UsedCouponAdapter extends RecyclerView.Adapter<UsedCouponAdapter.CouponViewHolder> {
    private List<Coupon> coupons = new ArrayList<>();

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_used_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);
        holder.bind(coupon);
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

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponImage = itemView.findViewById(R.id.image_coupon);
            brandText = itemView.findViewById(R.id.text_brand);
            nameText = itemView.findViewById(R.id.text_name);
            dateText = itemView.findViewById(R.id.text_date);
        }

        public void bind(Coupon coupon) {
            String formattedName = String.format("[%s] %s",
                    coupon.getBrandName(),
                    coupon.getProductName());

            brandText.setText(coupon.getBrandName());
            nameText.setText(formattedName);

            // 사용된 쿠폰의 경우 구매 날짜 표시
            String dateText = coupon.getExpireDate();
            if (dateText != null && !dateText.isEmpty()) {
                // "까지" 텍스트가 이미 포함되어 있으면 그대로 사용, 아니면 추가
                if (!dateText.contains("까지")) {
                    dateText = dateText + " 구매";
                }
            } else {
                dateText = "날짜 정보 없음";
            }
            this.dateText.setText(dateText);

            if (coupon.getImageResId() != 0) {
                couponImage.setImageResource(coupon.getImageResId());
            }

            // 사용된 쿠폰은 전체적으로 흐리게 표시
            itemView.setAlpha(0.5f);

            // 디버깅 로그
            android.util.Log.d("UsedCouponAdapter", "Binding used coupon - Brand: " + coupon.getBrandName() +
                    ", Product: " + coupon.getProductName() + ", Date: " + dateText);
        }
    }
}