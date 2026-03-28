package com.example.greenlens.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.greenlens.R;
import com.example.greenlens.databinding.ActivityPointHistoryBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.adapter.PointHistoryAdapter;
import com.example.greenlens.viewmodel.PointHistoryViewModel;

public class PointHistoryActivity extends AppCompatActivity {
    private ActivityPointHistoryBinding binding;
    private PointHistoryAdapter adapter;
    private UserManager userManager;
    private PointHistoryViewModel viewModel;
    private static final String TAG = "PointHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 뒤로가기 버튼 설정
        binding.btnBack.setOnClickListener(v -> finish());

        userManager = UserManager.getInstance(this);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new PointHistoryViewModel(userManager);
            }
        }).get(PointHistoryViewModel.class);

        setupRecyclerView();
        setupObservers();
        loadPointHistory();
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter();
        binding.recyclerPointHistory.setAdapter(adapter);
        binding.recyclerPointHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        viewModel.getPointHistory().observe(this, pointHistory -> {
            if (pointHistory != null && !pointHistory.isEmpty()) {
                adapter.submitList(pointHistory);
                showEmptyView(false);
            } else {
                showEmptyView(true);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPointHistory() {
        viewModel.loadPointHistory();
    }

    private void showEmptyView(boolean isEmpty) {
        binding.recyclerPointHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
