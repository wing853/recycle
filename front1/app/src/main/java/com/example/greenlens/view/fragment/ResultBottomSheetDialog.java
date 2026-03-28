package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.greenlens.R;
import com.example.greenlens.view.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ResultBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_DISPOSAL_METHOD = "disposal_method";

    public static ResultBottomSheetDialog newInstance(String type) {
        return newInstance(type, null);
    }

    public static ResultBottomSheetDialog newInstance(String type, String disposalMethod) {
        ResultBottomSheetDialog fragment = new ResultBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        if (disposalMethod != null) {
            args.putString(ARG_DISPOSAL_METHOD, disposalMethod);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String type = getArguments().getString(ARG_TYPE);
        String disposalMethod = getArguments().getString(ARG_DISPOSAL_METHOD);

        ImageView iconView = view.findViewById(R.id.ivResultIcon);
        TextView titleView = view.findViewById(R.id.tvResultTitle);
        TextView descView = view.findViewById(R.id.tvResultDescription);
        ImageView moreIcon = view.findViewById(R.id.btn_more);

        setupResultView(type, iconView, titleView, descView);

        // 서버에서 받아온 처리 방법이 있으면 표시
        if (disposalMethod != null && !disposalMethod.isEmpty()) {
            descView.setText(disposalMethod);
        }

        // 더보기 아이콘에만 리스너 연결 (null 체크)
        if (moreIcon != null) {
            moreIcon.setOnClickListener(v -> navigateToRecycleGuide(type));
        }

        // 전체 뷰 클릭 시에도 이동할 수 있도록 설정
        view.setOnClickListener(v -> navigateToRecycleGuide(type));
    }

    private void setupResultView(String type, ImageView iconView,
                                 TextView titleView, TextView descView) {
        switch (type) {
            case "pet":
                iconView.setImageResource(R.drawable.ic_plastic_detail);
                titleView.setText("페트병");
                descView.setText("무색 투명한 먹는샘물, 음료\n폴리에틸렌테레프탈레이트(PET)병");
                // 페트병은 플라스틱으로 처리하되, 화면에는 "페트병"으로 표시
                type = "plastic";
                break;
            case "plastic":
                iconView.setImageResource(R.drawable.ic_plastic_detail);
                titleView.setText("플라스틱 용기류");
                descView.setText("요구르트병, 샴푸통, 세제통 등\n플라스틱으로 된 재활용품");
                break;
            case "paper":
                iconView.setImageResource(R.drawable.ic_paper_detail);
                titleView.setText("종이류");
                descView.setText("신문, 책자, 노트 등\n종이로 된 재활용품");
                break;
            case "glass":
                iconView.setImageResource(R.drawable.ic_glass_detail);
                titleView.setText("유리병류");
                descView.setText("음료수병, 기타병류\n유리로 된 재활용품");
                break;
            case "metal":
                iconView.setImageResource(R.drawable.ic_metal_detail);
                titleView.setText("금속캔");
                descView.setText("알루미늄캔, 철캔\n캔으로 된 재활용품");
                break;
            case "vinyl":
                iconView.setImageResource(R.drawable.ic_vinyl_detail);
                titleView.setText("비닐류");
                descView.setText("과자봉지, 라면봉지\n비닐로 된 재활용품");
                break;
            case "styrofoam":
                iconView.setImageResource(R.drawable.ic_styrofoam_detail);
                titleView.setText("스티로폼");
                descView.setText("스티로폼 포장재\n스티로폼으로 된 재활용품");
                break;
        }
    }

    private void navigateToRecycleGuide(String type) {
        if (getActivity() instanceof com.example.greenlens.view.CameraActivity) {
            android.content.Intent intent = new android.content.Intent();
            intent.putExtra("guide_type", type);
            getActivity().setResult(android.app.Activity.RESULT_OK, intent);
            getActivity().finish();
        }
        dismiss();
    }
}