package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.greenlens.R;
import com.example.greenlens.databinding.FragmentHomeBinding;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 가져오기
        NavController navController = Navigation.findNavController(view);

        // 클릭 리스너 설정
        setupClickListeners(navController);

        // 애니메이션 적용
        applyAnimations();
    }

    private void setupClickListeners(NavController navController) {
        MaterialCardView[] cards = {
                binding.cardPaper,
                binding.cardPlastic,
                binding.cardGlass,
                binding.cardCan,
                binding.cardVinyl,
                binding.cardStyrofoam
        };

        String[] types = {"paper", "plastic", "glass", "metal", "vinyl", "styrofoam"};

        for (int i = 0; i < cards.length; i++) {
            final String type = types[i];
            cards[i].setOnClickListener(v -> navigateToRecycleGuide(navController, type));
        }
    }

    private void applyAnimations() {
        // 카드들에 스태거드 애니메이션 적용
        MaterialCardView[] cards = {
                binding.cardPaper,
                binding.cardPlastic,
                binding.cardGlass,
                binding.cardCan,
                binding.cardVinyl,
                binding.cardStyrofoam
        };

        for (int i = 0; i < cards.length; i++) {
            cards[i].setAlpha(0f);
            cards[i].setTranslationY(100f);
            cards[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(100 + (i * 100))
                    .start();
        }
    }

    private void navigateToRecycleGuide(NavController navController, String type) {
        HomeFragmentDirections.ActionHomeFragmentToRecycleGuideFragment action =
                HomeFragmentDirections.actionHomeFragmentToRecycleGuideFragment(type);
        navController.navigate(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
