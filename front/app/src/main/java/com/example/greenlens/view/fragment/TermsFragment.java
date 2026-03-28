package com.example.greenlens.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenlens.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TermsFragment extends Fragment {
    private TextView termsTextView;
    private String termsType;

    public static TermsFragment newInstance(String type) {
        TermsFragment fragment = new TermsFragment();
        Bundle args = new Bundle();
        args.putString("terms_type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            termsType = getArguments().getString("terms_type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms, container, false);
        termsTextView = view.findViewById(R.id.termsTextView);
        loadTerms();
        return view;
    }

    private void loadTerms() {
        String fileName;
        switch (termsType) {
            case "service":
                fileName = "terms_of_service.txt";
                break;
            case "privacy":
                fileName = "privacy_policy.txt";
                break;
            default:
                fileName = "terms_of_service.txt";
        }

        try {
            InputStream is = requireContext().getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            termsTextView.setText(text.toString());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            termsTextView.setText("약관을 불러오는데 실패했습니다.");
        }
    }
}