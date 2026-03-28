package com.example.greenlens.view.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.greenlens.model.Coupon;
import java.util.ArrayList;
import java.util.List;

public abstract class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder> {
    protected List<Coupon> items = new ArrayList<>();
    protected OnCouponClickListener listener;

    public interface OnCouponClickListener {
        void onUseClick(Coupon coupon, int position);
    }

    // 생성자
    public CouponAdapter() {
        this.items = new ArrayList<>();
    }

    public void setOnCouponClickListener(OnCouponClickListener listener) {
        this.listener = listener;
    }

    public List<Coupon> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Coupon> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // 각 Adapter가 구현해야 할 레이아웃 리소스 ID
    protected abstract int getItemLayout();

    // ViewHolder 추상 클래스 정의
    abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull android.view.View view) {
            super(view);
            setupViews(view);
            setupClickListener();
        }

        // 각 ViewHolder가 구현해야 할 메서드들
        protected abstract void setupViews(@NonNull android.view.View view);
        protected abstract void bind(Coupon item);

        protected void setupClickListener() {
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUseClick(items.get(position), position);
                }
            });
        }
    }

    @NonNull
    @Override
    public abstract ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }
}