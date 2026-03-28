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

import java.util.Map;

public class RankingAdapter extends ListAdapter<Map<String, Object>, RankingAdapter.RankingViewHolder> {

    public RankingAdapter() {
        super(new DiffUtil.ItemCallback<Map<String, Object>>() {
            @Override
            public boolean areItemsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                String oldId = String.valueOf(oldItem.get("userId"));
                String newId = String.valueOf(newItem.get("userId"));
                return oldId.equals(newId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking_user, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Map<String, Object> ranking = getItem(position);
        holder.bind(ranking, position + 1);
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textRank;
        private final TextView textUsername;
        private final TextView textRecycleCount;
        private final View rankBackground;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            textRank = itemView.findViewById(R.id.textRank);
            textUsername = itemView.findViewById(R.id.textUsername);
            textRecycleCount = itemView.findViewById(R.id.textRecycleCount);
            rankBackground = itemView.findViewById(R.id.rankBackground);
        }

        public void bind(Map<String, Object> ranking, int position) {
            try {
                String username = String.valueOf(ranking.get("username"));
                Object recycleCountObj = ranking.get("recycleCount");
                int recycleCount = 0;

                if (recycleCountObj instanceof Double) {
                    recycleCount = ((Double) recycleCountObj).intValue();
                } else if (recycleCountObj instanceof Integer) {
                    recycleCount = (Integer) recycleCountObj;
                }

                textRank.setText(String.valueOf(position));
                textUsername.setText(username);
                textRecycleCount.setText(String.format("%,d회", recycleCount));

                // 순위에 따른 배경색 설정
                if (position <= 3) {
                    rankBackground.setAlpha(0.8f);  // 1~3위: 30% 불투명도
                } else {
                    rankBackground.setAlpha(0.4f); // 4위 이하: 15% 불투명도
                }
            } catch (Exception e) {
                DevLog.e("RankingAdapter", "데이터 바인딩 중 오류 발생", e);
            }
        }
    }
}