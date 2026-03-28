package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.greenlens.R;
import com.example.greenlens.databinding.FragmentRecycleGuideBinding;

public class RecycleGuideFragment extends Fragment {
    private FragmentRecycleGuideBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecycleGuideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SafeArgs를 사용하여 매개변수 가져오기
        String type = RecycleGuideFragmentArgs.fromBundle(getArguments()).getGuideText();
        setGuideContent(type);

        // 뒤로 가기 버튼 클릭 시 NavController 사용
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
    }

    private void setGuideContent(String type) {
        if (type == null) return;

        int layoutResId;
        String title;

        switch (type) {
            case "glass":
                title = "유리병류";
                layoutResId = R.layout.fragment_glass_recycle;
                break;
            case "paper":
                title = "종이류";
                layoutResId = R.layout.fragment_paper_recycle;
                break;
            case "plastic":
                title = "플라스틱 용기류";
                layoutResId = R.layout.fragment_plastic_recycle;
                break;
            case "vinyl":
                title = "비닐류";
                layoutResId = R.layout.fragment_vinyl_recycle;
                break;
            case "metal":
                title = "금속캔";
                layoutResId = R.layout.fragment_metal_recycle;
                break;
            case "styrofoam":
                title = "스티로폼";
                layoutResId = R.layout.fragment_styrofoam_recycle;
                break;
            default:
                title = "알 수 없음";
                layoutResId = 0;
                break;
        }

        // 타이틀 설정
        binding.textTitle.setText(title);

        // 해당하는 레이아웃 동적으로 추가
        if (layoutResId != 0) {
            getLayoutInflater().inflate(layoutResId, binding.layoutContent, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
