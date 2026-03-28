package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlens.R;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.adapter.RankingAdapter;
import com.example.greenlens.viewmodel.RankingViewModel;

public class RankingFragment extends Fragment {
    private RankingViewModel viewModel;
    private RankingAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textTotalUsers;
    private View emptyView;
    private UserManager userManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RankingViewModel.class);
        userManager = UserManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRanking);
        progressBar = view.findViewById(R.id.progressBar);
        textTotalUsers = view.findViewById(R.id.textTotalUsers);
        emptyView = view.findViewById(R.id.emptyView);

        setupRecyclerView();
        setupObservers();
        loadRankings();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RankingAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getRankings().observe(getViewLifecycleOwner(), rankings -> {
            if (rankings != null) {
                adapter.submitList(rankings);
                boolean isEmpty = rankings.isEmpty();
                recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                DevLog.d("RankingFragment", "랭킹 목록 업데이트: " + rankings.size() + "개");
            }
        });

        viewModel.getTotalRecycleCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                textTotalUsers.setText(String.format("%,d", count));
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                DevLog.e("RankingFragment", "에러 발생: " + error);
                // TODO: 에러 메시지 표시
            }
        });
    }

    private void loadRankings() {
        String token = userManager.getToken();
        if (token != null) {
            viewModel.loadRankings(token);
        } else {
            DevLog.e("RankingFragment", "토큰이 없습니다");
            // TODO: 로그인 화면으로 이동
        }
    }
}