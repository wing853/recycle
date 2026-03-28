package com.example.greenlens.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlens.R;
import com.example.greenlens.util.DevLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PointHistoryAdapter extends ListAdapter<Map<String, Object>, PointHistoryAdapter.PointHistoryViewHolder> {

    public PointHistoryAdapter() {
        super(new DiffUtil.ItemCallback<Map<String, Object>>() {
            @Override
            public boolean areItemsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                return oldItem.get("logId").equals(newItem.get("logId"));
            }

            @Override
            public boolean areContentsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public PointHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point_history, parent, false);
        return new PointHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointHistoryViewHolder holder, int position) {
        Map<String, Object> history = getItem(position);
        holder.bind(history);
    }

    static class PointHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDate;
        private final TextView textCategory;
        private final TextView textEarnedPoint;
        private final TextView textTotalPoint;
        private final TextView textUsedPoint;
        private final View layoutEarnedPoint;
        private final View layoutUsedPoint;
        private final SimpleDateFormat dateFormat;

        public PointHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
            textCategory = itemView.findViewById(R.id.text_category);
            textEarnedPoint = itemView.findViewById(R.id.text_earned_point);
            textTotalPoint = itemView.findViewById(R.id.text_total_point);
            textUsedPoint = itemView.findViewById(R.id.text_used_point);
            layoutEarnedPoint = itemView.findViewById(R.id.layout_earned_point);
            layoutUsedPoint = itemView.findViewById(R.id.layout_used_point);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

        public void bind(Map<String, Object> history) {
            try {
                // 날짜 설정
                String dateStr = (String) history.get("date");
                if (dateStr != null) {
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).parse(dateStr);
                    if (date != null) {
                        textDate.setText(dateFormat.format(date));
                    }
                }

                // 카테고리(brandName) 설정
                String brandName = (String) history.get("brandName");
                if (brandName != null && !brandName.isEmpty()) {
                    textCategory.setText(brandName);
                    textCategory.setVisibility(View.VISIBLE);
                } else {
                    textCategory.setText("분리수거");
                    textCategory.setVisibility(View.VISIBLE);
                }

                // 잔여 포인트
                Object balanceObj = history.get("balance");
                if (balanceObj != null) {
                    long balance = balanceObj instanceof Number ? ((Number) balanceObj).longValue() : 0;
                    textTotalPoint.setText(String.format("%dP", balance));
                } else {
                    textTotalPoint.setText("0P");
                }

                // 적립/사용 구분
                String type = (String) history.get("type");
                Object pointsObj = history.get("points");
                long points = pointsObj instanceof Number ? ((Number) pointsObj).longValue() : 0;
                if ("적립".equals(type)) {
                    layoutEarnedPoint.setVisibility(View.VISIBLE);
                    layoutUsedPoint.setVisibility(View.GONE);
                    textEarnedPoint.setText(String.format("%dP", points));
                } else {
                    layoutEarnedPoint.setVisibility(View.GONE);
                    layoutUsedPoint.setVisibility(View.VISIBLE);
                    long absPoints = Math.abs(points);
                    textUsedPoint.setText(String.format("%dP", absPoints));
                }
            } catch (Exception e) {
                DevLog.e("PointHistoryAdapter", "데이터 바인딩 중 오류 발생", e);
            }
        }
    }
}
