package com.example.greenlens.view;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.greenlens.R;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.databinding.ActivityCouponUseBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.util.DevLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CouponUseActivity extends AppCompatActivity {
    private ActivityCouponUseBinding binding;
    private Long couponId;
    private String productName;
    private boolean isSaved = false;
    private static final String TAG = "CouponUseActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ApiService apiService;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCouponUseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // API 서비스 초기화
        apiService = ApiClient.getInstance(this).getApiService();
        userManager = UserManager.getInstance(this);

        // Intent에서 쿠폰 ID와 상품명 가져오기
        couponId = getIntent().getLongExtra("couponId", -1);
        productName = getIntent().getStringExtra("productName");

        if (couponId == -1 || productName == null) {
            Toast.makeText(this, "잘못된 쿠폰 정보입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        displayGifticon();
    }

    private void setupViews() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener(v -> {
            if (!isSaved) {
                Toast.makeText(this, "기프티콘을 저장해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            finish();
        });

        // 저장하기 버튼
        binding.btnSaveToGallery.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                saveGifticon();
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveGifticon();
            } else {
                Toast.makeText(this, "갤러리 저장을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayGifticon() {
        // 상품명에 따라 기프티콘 이미지 설정
        if (productName.contains("쿠키")) {
            binding.ivCouponImage.setImageResource(R.drawable.img_cookie_coupon);
        } else if (productName.contains("아메리카노")) {
            binding.ivCouponImage.setImageResource(R.drawable.img_americano_coupon);
        }

        // 저장 버튼 활성화
        binding.btnSaveToGallery.setEnabled(true);
        binding.btnSaveToGallery.setBackgroundResource(R.drawable.bg_green_round);
    }

    private void saveGifticon() {
        try {
            // 이미지를 Bitmap으로 변환
            BitmapDrawable drawable = (BitmapDrawable) binding.ivCouponImage.getDrawable();
            if (drawable == null) {
                Toast.makeText(this, "저장할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = drawable.getBitmap();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 이상에서는 MediaStore API 사용
                String fileName = "Gifticon_" + productName + "_" + System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GreenLens");

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    }
                }
            } else {
                // Android 9 이하에서는 기존 방식 사용
                String fileName = "Gifticon_" + productName + "_" + System.currentTimeMillis() + ".jpg";
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "GreenLens");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File imageFile = new File(storageDir, fileName);

                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                // 갤러리에 이미지 추가
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        imageFile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            }

            Toast.makeText(this, "기프티콘이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show();
            isSaved = true;
            binding.btnSaveToGallery.setEnabled(false);
            binding.btnSaveToGallery.setText("저장 완료");

            // 쿠폰 사용 API 호출
            useCoupon();

        } catch (IOException e) {
            DevLog.e(TAG, "기프티콘 저장 실패", e);
            Toast.makeText(this, "기프티콘 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void useCoupon() {
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = userManager.getAuthToken();
        if (!authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        // 상품명에 따라 실제 서버 쿠폰 ID 매핑
        int serverCouponId;
        if (productName.contains("아메리카노")) {
            serverCouponId = 4;  // 스타벅스 아메리카노
        } else if (productName.contains("쿠키")) {
            serverCouponId = 1;  // CU ABC 초코쿠키쿠앤크
        } else {
            Toast.makeText(this, "지원하지 않는 쿠폰입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DevLog.d(TAG, "Using coupon - ID: " + serverCouponId + ", Token: " + authToken);

        apiService.useCoupon(authToken, (long)serverCouponId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                DevLog.d(TAG, "API Response - Code: " + response.code());
                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        DevLog.e(TAG, "Error Body: " + errorBody);
                    } catch (IOException e) {
                        DevLog.e(TAG, "Error reading error body", e);
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    DevLog.d(TAG, "Response Body: " + result);
                    boolean success = (boolean) result.get("success");
                    if (success) {
                        Toast.makeText(CouponUseActivity.this, "쿠폰이 사용되었습니다.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = (String) result.get("message");
                        DevLog.e(TAG, "API Error - Message: " + message);
                        Toast.makeText(CouponUseActivity.this, message != null ? message : "쿠폰 사용에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "쿠폰 사용에 실패했습니다.";
                    switch (response.code()) {
                        case 403:
                            errorMessage = "권한이 없습니다. 다시 로그인해주세요.";
                            break;
                        case 404:
                            errorMessage = "쿠폰을 찾을 수 없습니다.";
                            break;
                        case 409:
                            errorMessage = "이미 사용된 쿠폰입니다.";
                            break;
                    }
                    DevLog.e(TAG, "API Error - Code: " + response.code());
                    Toast.makeText(CouponUseActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                DevLog.e(TAG, "API Call Failed", t);
                Toast.makeText(CouponUseActivity.this, "서버 연결에 실패했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isSaved) {
            Toast.makeText(this, "기프티콘을 저장해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
