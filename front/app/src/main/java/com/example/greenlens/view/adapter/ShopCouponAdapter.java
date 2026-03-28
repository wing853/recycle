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
import com.example.greenlens.view.ShopDetailActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ShopCouponAdapter extends RecyclerView.Adapter<ShopCouponAdapter.ViewHolder> {
    private List<Coupon> items = new ArrayList<>();
    private OnCouponClickListener listener;

    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon, int position);
    }

    public void setOnCouponClickListener(OnCouponClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Coupon> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coupon item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProduct;
        private final TextView textBrand;
        private final TextView textName;
        private final TextView textPoint;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textBrand = itemView.findViewById(R.id.text_brand);
            textName = itemView.findViewById(R.id.text_name);
            textPoint = itemView.findViewById(R.id.text_point);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCouponClick(items.get(position), position);
                }
            });
        }

        void bind(Coupon item) {
            textBrand.setText(String.format("[%s]", item.getBrandName()));
            textName.setText(item.getProductName());
            textPoint.setText(String.format("%,dP", item.getPoints()));

            // 이미지 로드
            if (item.getImageResId() != 0) {
                Glide.with(itemView.getContext())
                        .load(item.getImageResId())
                        .centerCrop()
                        .into(imageProduct);
            }
        }
    }
}