package com.example.greenlens.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.greenlens.R;
import com.example.greenlens.api.ApiClient;
import com.example.greenlens.api.ApiService;
import com.example.greenlens.constant.ConstPref;
import com.example.greenlens.databinding.ActivityCameraBinding;
import com.example.greenlens.manager.UserManager;
import com.example.greenlens.model.User;
import com.example.greenlens.model.response.AnalysisResultResponse;
import com.example.greenlens.model.response.AnalyzeResponse;
import com.example.greenlens.repository.UserRepository;
import com.example.greenlens.util.DevLog;
import com.example.greenlens.view.fragment.ResultBottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;
    private ImageCapture imageCapture;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "CameraActivity";

    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean isCameraInitialized = false;

    private ApiService apiService;
    private String authToken;
    private UserManager userManager;
    private File currentPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getApiService();
        userManager = UserManager.getInstance(this);

        DevLog.d(TAG, "=== 카메라 액티비티 시작 ===");
        DevLog.d(TAG, "저장된 토큰: " + userManager.getToken());
        DevLog.d(TAG, "인증 토큰: " + userManager.getAuthToken());
        DevLog.d(TAG, "로그인 상태: " + userManager.isLoggedIn());
        DevLog.d(TAG, "토큰 만료 여부: " + userManager.isTokenExpired());

        if (!userManager.isLoggedIn()) {
            DevLog.e(TAG, "로그인되지 않은 상태로 카메라 접근 시도");
            Toast.makeText(this, "로그인이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authToken = userManager.getAuthToken();
        DevLog.d(TAG, "최종 사용할 인증 토큰: " + authToken);

        if (authToken == null || authToken.isEmpty()) {
            DevLog.e(TAG, "인증 토큰이 null 또는 비어있음");
            Toast.makeText(this, "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if (allPermissionsGranted()) {
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this, ConstPref.REQUIRED_PERMISSIONS, ConstPref.REQUEST_CODE_PERMISSIONS);
        }

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            if (binding.viewFinder.getVisibility() == View.VISIBLE) {
                try {
                    if (cameraProvider != null) {
                        cameraProvider.unbindAll();
                    }
                    if (isCameraInitialized) {
                        startCamera();
                    } else {
                        initCamera();
                    }
                    DevLog.d(TAG, "onResume: 카메라 재시작 완료");
                } catch (Exception e) {
                    DevLog.e(TAG, "onResume: 카메라 재시작 실패", e);
                    isCameraInitialized = false;
                    initCamera();
                }
            }
        } else if (!isFinishing()) {
            ActivityCompat.requestPermissions(this, ConstPref.REQUIRED_PERMISSIONS, ConstPref.REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraProvider != null) {
            try {
                DevLog.d(TAG, "onStop에서 카메라 리소스 해제");
                cameraProvider.unbindAll();
            } catch (Exception e) {
                DevLog.e(TAG, "onStop에서 카메라 리소스 해제 실패", e);
            }
        }
    }

    private void initCamera() {
        try {
            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            isCameraInitialized = true;
            startCamera();
        } catch (Exception e) {
            isCameraInitialized = false;
            DevLog.e(TAG, "카메라 초기화 실패", e);
            Toast.makeText(this, "카메라를 초기화할 수 없습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseCamera() {
        try {
            if (cameraProvider != null) {
                DevLog.d(TAG, "카메라 리소스 해제 중...");
                cameraProvider.unbindAll();
            }
        } catch (Exception e) {
            DevLog.e(TAG, "카메라 리소스 해제 실패", e);
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCapture.setOnClickListener(v -> takePhoto());
        binding.btnRetake.setOnClickListener(v -> {
            binding.viewFinder.setVisibility(View.VISIBLE);
            binding.capturedImageView.setVisibility(View.GONE);
            binding.btnCapture.setVisibility(View.VISIBLE);
            binding.btnRetake.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
            startCamera();
        });
        binding.btnConfirm.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            analyzeImage();
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : ConstPref.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ConstPref.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        if (cameraProviderFuture == null) {
            initCamera();
            return;
        }

        cameraProviderFuture.addListener(() -> {
            try {
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }

                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                if (binding.viewFinder.getVisibility() != View.VISIBLE) {
                    DevLog.d(TAG, "뷰파인더가 보이지 않아 카메라 시작 취소");
                    return;
                }

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    DevLog.d(TAG, "카메라 시작 성공");
                } catch (Exception e) {
                    DevLog.e(TAG, "카메라 바인딩 오류", e);
                    Toast.makeText(this, "카메라 초기화 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } catch (ExecutionException | InterruptedException e) {
                DevLog.e(TAG, "카메라 시작 실패", e);
                Toast.makeText(this, "카메라를 시작할 수 없습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                DevLog.e(TAG, "카메라 시작 중 예상치 못한 오류", e);
                Toast.makeText(this, "카메라 초기화 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstPref.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            DevLog.d(TAG, "Camera result received");
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                DevLog.d(TAG, "Photo file exists at: " + currentPhotoFile.getAbsolutePath());
                showCapturedImage();
            } else {
                DevLog.e(TAG, "Photo file is null or does not exist");
                Toast.makeText(this, "이미지 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            DevLog.d(TAG, "Camera capture cancelled");
        } else {
            DevLog.e(TAG, "Camera capture failed with result code: " + resultCode);
            Toast.makeText(this, "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {
        DevLog.d(TAG, "takePhoto() called");
        if (imageCapture == null) {
            DevLog.e(TAG, "imageCapture is null");
            Toast.makeText(this, "카메라가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File photoDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "GreenLens");
            if (!photoDir.exists()) {
                if (!photoDir.mkdirs()) {
                    DevLog.e(TAG, "Failed to create directory");
                    Toast.makeText(this, "이미지 저장 디렉토리를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            currentPhotoFile = File.createTempFile(imageFileName, ".jpg", photoDir);
            DevLog.d(TAG, "Created photo file: " + currentPhotoFile.getAbsolutePath());

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(currentPhotoFile).build();

            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            DevLog.d(TAG, "Photo capture succeeded: " + output.getSavedUri());
                            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                                showCapturedImage();
                            } else {
                                DevLog.e(TAG, "Photo file is null or does not exist after capture");
                                Toast.makeText(CameraActivity.this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exc) {
                            DevLog.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                            Toast.makeText(CameraActivity.this, "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (IOException e) {
            DevLog.e(TAG, "Failed to create image file", e);
            Toast.makeText(this, "이미지 파일을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCapturedImage() {
        releaseCamera();

        binding.viewFinder.setVisibility(View.GONE);
        binding.capturedImageView.setVisibility(View.VISIBLE);
        binding.btnCapture.setVisibility(View.GONE);
        binding.btnRetake.setVisibility(View.VISIBLE);
        binding.btnConfirm.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(currentPhotoFile)
                .into(binding.capturedImageView);
    }

    private void analyzeImage() {
        DevLog.d(TAG, "analyzeImage() called");
        if (currentPhotoFile == null || !currentPhotoFile.exists()) {
            DevLog.e(TAG, "Photo file is null or does not exist");
            Toast.makeText(this, "이미지 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File compressedFile = compressImage(currentPhotoFile);
            if (compressedFile == null || !compressedFile.exists()) {
                DevLog.e(TAG, "Failed to compress image");
                Toast.makeText(this, "이미지 압축에 실패했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = compressedFile.getName();
            if (!fileName.toLowerCase().endsWith(".jpg")) {
                fileName = fileName + ".jpg";
            }
            fileName = fileName.replaceAll("[^A-Za-z0-9_.]", "_");

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", fileName, requestFile);

            String finalAuthToken = authToken;
            if (!finalAuthToken.startsWith("Bearer ")) {
                finalAuthToken = "Bearer " + finalAuthToken;
            }
            if (finalAuthToken.startsWith("Bearer Bearer ")) {
                finalAuthToken = finalAuthToken.replaceFirst("Bearer ", "");
                finalAuthToken = "Bearer " + finalAuthToken;
            }

            apiService.analyzeImageMultipart(body).enqueue(new Callback<AnalyzeResponse>() {
                @Override
                public void onResponse(@NonNull Call<AnalyzeResponse> call, @NonNull Response<AnalyzeResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        AnalyzeResponse analyzeResponse = response.body();
                        String message = analyzeResponse.getMessage();
                        String category = analyzeResponse.getCategory();
                        String disposalMethod = analyzeResponse.getDisposalMethod();
                        String type = AnalysisResultResponse.mapCategoryToType(category);

                        if (message != null && !message.isEmpty()) {
                            new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this)
                                    .setTitle("알림")
                                    .setMessage(message)
                                    .setPositiveButton("확인", (dialog, which) -> {
                                        if (category != null && !category.isEmpty()) {
                                            showResult(type, disposalMethod);
                                        } else {
                                            Long analysisId = analyzeResponse.getAnalysisId();
                                            if (analysisId != null) {
                                                getAnalysisResult(analysisId);
                                            }
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            if (category != null && !category.isEmpty()) {
                                showResult(type, disposalMethod);
                            } else {
                                Long analysisId = analyzeResponse.getAnalysisId();
                                if (analysisId != null) {
                                    getAnalysisResult(analysisId);
                                }
                            }
                        }
                    } else {
                        DevLog.e(TAG, "API call failed with code: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            DevLog.e(TAG, "Error response body: " + errorBody);
                        } catch (IOException e) {
                            DevLog.e(TAG, "Failed to read error body", e);
                        }
                        handleApiError(response.code(), "이미지 분석에 실패했습니다.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AnalyzeResponse> call, @NonNull Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    DevLog.e(TAG, "Network error", t);
                    Toast.makeText(CameraActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            DevLog.e(TAG, "Error processing image", e);
            Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleApiError(int errorCode, String errorMessage) {
        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            String finalErrorMessage = "이미지 분석 요청에 실패했습니다: " + errorCode + "\n" + errorMessage;

            switch (errorCode) {
                case 403:
                    DevLog.e(TAG, "403 오류 발생 - 토큰: " + authToken);
                    showRetryDialog();
                    return;
                case 401:
                    finalErrorMessage = "인증에 실패했습니다. 다시 로그인해주세요.";
                    redirectToLogin();
                    return;
                case 400:
                    finalErrorMessage = "이미지 형식이 올바르지 않습니다. 다른 이미지를 사용해주세요.";
                    break;
                case 413:
                    finalErrorMessage = "이미지 파일이 너무 큽니다. 더 작은 이미지를 사용해주세요.";
                    break;
                case 500:
                    finalErrorMessage = "서버 내부 오류입니다. 잠시 후 다시 시도해주세요.";
                    break;
            }

            Toast.makeText(CameraActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
        });
    }

    private void showRetryDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("분석 실패")
                .setMessage("이미지 분석에 실패했습니다.\n다시 시도하시겠습니까?")
                .setPositiveButton("재시도", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    analyzeImage();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    binding.viewFinder.setVisibility(View.VISIBLE);
                    binding.capturedImageView.setVisibility(View.GONE);
                    binding.btnCapture.setVisibility(View.VISIBLE);
                    binding.btnRetake.setVisibility(View.GONE);
                    binding.btnConfirm.setVisibility(View.GONE);
                    startCamera();
                })
                .setCancelable(false)
                .show();
    }

    private void redirectToLogin() {
        userManager.clearUserSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getAnalysisResult(Long analysisId) {
        DevLog.d(TAG, "[getAnalysisResult] analysisId: " + analysisId);
        apiService.getAnalysisResult(analysisId).enqueue(new Callback<AnalysisResultResponse>() {
            @Override
            public void onResponse(Call<AnalysisResultResponse> call, Response<AnalysisResultResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AnalysisResultResponse result = response.body();
                    String disposalMethod = result.getDisposalMethod();
                    String type = result.getTypeForApp();

                    if (type != null && !type.isEmpty()) {
                        showResult(type, disposalMethod);
                    } else {
                        Toast.makeText(CameraActivity.this,
                                "분석 결과를 확인할 수 없습니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT).show();
                        showRetryDialog();
                    }
                } else if (response.code() == 403 || response.code() == 401) {
                    Toast.makeText(CameraActivity.this, "인증이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    Toast.makeText(CameraActivity.this,
                            "결과 조회에 실패했습니다: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    showRetryDialog();
                }
            }

            @Override
            public void onFailure(Call<AnalysisResultResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CameraActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showRetryDialog();
            }
        });
    }

    private void showResult(String type) {
        showResult(type, null);
    }

    private void showResult(String type, String disposalMethod) {
        ResultBottomSheetDialog bottomSheet = ResultBottomSheetDialog.newInstance(type, disposalMethod);
        bottomSheet.setOnDismissCallback(() -> {
            binding.viewFinder.setVisibility(View.VISIBLE);
            binding.capturedImageView.setVisibility(View.GONE);
            binding.btnCapture.setVisibility(View.VISIBLE);
            binding.btnRetake.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
            startCamera();
        });
        bottomSheet.show(getSupportFragmentManager(), "result_bottom_sheet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        binding = null;
    }

    private File compressImage(File originalFile) {
        try {
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            int maxDimension = 1024;

            int inSampleSize = 1;
            if (imageHeight > maxDimension || imageWidth > maxDimension) {
                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) {
                DevLog.e(TAG, "이미지 압축 실패: Bitmap 생성 실패");
                return null;
            }

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis());
            File compressedFile = new File(getExternalCacheDir(), "compressed_" + timeStamp + ".jpg");

            java.io.FileOutputStream out = new java.io.FileOutputStream(compressedFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();
            bitmap.recycle();

            DevLog.d(TAG, "이미지 압축 완료: " + originalFile.length() + " -> " + compressedFile.length() + " bytes");
            return compressedFile;

        } catch (java.io.IOException e) {
            DevLog.e(TAG, "이미지 압축 중 오류 발생", e);
            return null;
        } catch (OutOfMemoryError e) {
            DevLog.e(TAG, "이미지 압축 중 메모리 부족", e);
            return null;
        }
    }

    private String mapTypeForGuide(String type) {
        if (type == null) return null;
        switch (type.toLowerCase()) {
            case "플라스틱":
            case "plastic":
            case "페트병":
            case "pet":
                return "plastic";
            case "캔":
            case "metal":
                return "metal";
            case "종이":
            case "paper":
                return "paper";
            case "비닐":
            case "vinyl":
                return "vinyl";
            case "유리병":
            case "glass":
                return "glass";
            case "스티로폼":
            case "styrofoam":
                return "styrofoam";
            default:
                return null;
        }
    }
}