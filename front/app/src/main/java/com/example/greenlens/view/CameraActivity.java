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

    // 카메라 관련 변수 추가
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

        // API 서비스 초기화
        apiService = ApiClient.getInstance(this).getApiService();

        // UserManager 초기화 및 토큰 가져오기
        userManager = UserManager.getInstance(this);

        // 디버깅을 위한 상세 로그
        DevLog.d(TAG, "=== 카메라 액티비티 시작 ===");
        DevLog.d(TAG, "저장된 토큰: " + userManager.getToken());
        DevLog.d(TAG, "인증 토큰: " + userManager.getAuthToken());
        DevLog.d(TAG, "로그인 상태: " + userManager.isLoggedIn());
        DevLog.d(TAG, "토큰 만료 여부: " + userManager.isTokenExpired());

        // 로그인 상태 확인
        if (!userManager.isLoggedIn()) {
            DevLog.e(TAG, "로그인되지 않은 상태로 카메라 접근 시도");
            Toast.makeText(this, "로그인이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 인증 토큰 가져오기 (자동으로 Bearer 접두사 추가됨)
        authToken = userManager.getAuthToken();
        DevLog.d(TAG, "최종 사용할 인증 토큰: " + authToken);

        // 토큰이 없으면 에러 메시지 표시 후 종료
        if (authToken == null || authToken.isEmpty()) {
            DevLog.e(TAG, "인증 토큰이 null 또는 비어있음");
            Toast.makeText(this, "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            // 로그인 화면으로 이동
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if (allPermissionsGranted()) {
            // 권한이 있으면 onCreate에서는 카메라를 초기화만 하고 onResume에서 시작
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this, ConstPref.REQUIRED_PERMISSIONS, ConstPref.REQUEST_CODE_PERMISSIONS);
        }

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 보일 때 카메라 시작
        if (allPermissionsGranted()) {
            if (binding.viewFinder.getVisibility() == View.VISIBLE) {
                // 카메라 상태 확인 후 필요시 리소스 해제 및 재시작
                try {
                    if (cameraProvider != null) {
                        // 카메라 리소스가 이미 있는 경우 일단 해제
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
                    // 초기화 상태 재설정 후 다시 시도
                    isCameraInitialized = false;
                    initCamera();
                }
            }
        } else if (!isFinishing()) {
            // 권한이 없는 경우 요청
            ActivityCompat.requestPermissions(this, ConstPref.REQUIRED_PERMISSIONS, ConstPref.REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 액티비티가 일시 중지될 때 카메라 리소스 해제
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 더 이상 보이지 않을 때 카메라 리소스 강제 해제
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
            // 재촬영 버튼 클릭 시
            binding.viewFinder.setVisibility(View.VISIBLE);
            binding.capturedImageView.setVisibility(View.GONE);
            binding.btnCapture.setVisibility(View.VISIBLE);
            binding.btnRetake.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
            startCamera();
        });
        binding.btnConfirm.setOnClickListener(v -> {
            // 확인 버튼 클릭 시 이미지 분석 시작
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
                // 이전 인스턴스가 존재할 경우 먼저 해제
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }

                // 새로운 인스턴스 가져오기
                cameraProvider = cameraProviderFuture.get();

                // 예방 차원에서 다시 한 번 이전 바인딩 모두 해제
                cameraProvider.unbindAll();

                // 뷰가 보이지 않으면 카메라를 시작하지 않음
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
                    // 카메라를 현재 액티비티의 라이프사이클에 바인딩
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
                showCapturedImage();  // analyzeImage() 대신 showCapturedImage() 호출
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

        // 이미지 파일 생성
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
            currentPhotoFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    photoDir
            );
            DevLog.d(TAG, "Created photo file: " + currentPhotoFile.getAbsolutePath());

            // 이미지 캡처 설정
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(currentPhotoFile).build();

            // 이미지 캡처 실행
            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            DevLog.d(TAG, "Photo capture succeeded: " + output.getSavedUri());
                            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                                // 촬영된 이미지 표시
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
        // 사진이 찍혔으면 카메라 리소스 해제
        releaseCamera();

        // 촬영된 이미지 표시
        binding.viewFinder.setVisibility(View.GONE);
        binding.capturedImageView.setVisibility(View.VISIBLE);
        binding.btnCapture.setVisibility(View.GONE);
        binding.btnRetake.setVisibility(View.VISIBLE);
        binding.btnConfirm.setVisibility(View.VISIBLE);

        // Glide를 사용하여 이미지 로드
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
            // 이미지 압축
            File compressedFile = compressImage(currentPhotoFile);
            if (compressedFile == null || !compressedFile.exists()) {
                DevLog.e(TAG, "Failed to compress image");
                Toast.makeText(this, "이미지 압축에 실패했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 파일명 영문, .jpg로 보장
            String fileName = compressedFile.getName();
            if (!fileName.toLowerCase().endsWith(".jpg")) {
                fileName = fileName + ".jpg";
            }
            // 파일명에 한글/공백/특수문자 제거 (영문, 숫자, 언더스코어만 허용)
            fileName = fileName.replaceAll("[^A-Za-z0-9_.]", "_");

            DevLog.d(TAG, "Compressed file size: " + compressedFile.length() + " bytes");
            DevLog.d(TAG, "Compressed file path: " + compressedFile.getAbsolutePath());
            DevLog.d(TAG, "Upload file name: " + fileName);

            // Multipart 요청 생성 (필드명 'image', 파일명 영문, Content-Type 'image/jpeg')
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", fileName, requestFile);

            // Authorization 헤더 'Bearer <token>'이 정확히 한 번만 붙도록 보장
            String finalAuthToken = authToken;
            if (!finalAuthToken.startsWith("Bearer ")) {
                finalAuthToken = "Bearer " + finalAuthToken;
            }
            if (finalAuthToken.startsWith("Bearer Bearer ")) {
                finalAuthToken = finalAuthToken.replaceFirst("Bearer ", "");
                finalAuthToken = "Bearer " + finalAuthToken;
            }
            DevLog.d(TAG, "최종 Authorization 헤더: " + finalAuthToken);

            // API 호출
            DevLog.d(TAG, "Sending image to server... Size: " + compressedFile.length() + " bytes, Name: " + fileName);
            apiService.analyzeImageMultipart(finalAuthToken, body).enqueue(new Callback<AnalyzeResponse>() {
                @Override
                public void onResponse(@NonNull Call<AnalyzeResponse> call, @NonNull Response<AnalyzeResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        AnalyzeResponse analyzeResponse = response.body();
                        String message = analyzeResponse.getMessage();
                        Long analysisId = analyzeResponse.getAnalysisId();
                        // message가 있으면 먼저 다이얼로그로 띄움
                        if (message != null && !message.isEmpty()) {
                            new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this)
                                    .setTitle("알림")
                                    .setMessage(message)
                                    .setPositiveButton("확인", (dialog, which) -> {
                                        // 확인 누르면 결과창 띄우기
                                        if (analysisId != null) {
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                getAnalysisResult(analysisId);
                                            }, 2000);
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else if (analysisId != null) {
                            // 메시지가 없으면 바로 결과창 띄우기
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                getAnalysisResult(analysisId);
                            }, 2000);
                        }
                    } else {
                        DevLog.e(TAG, "API call failed with code: " + response.code());
                        DevLog.e(TAG, "Response headers: " + response.headers());
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
                    DevLog.e(TAG, "Error message: " + t.getMessage());
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

            // 에러 코드별 처리
            switch (errorCode) {
                case 403:
                    finalErrorMessage = "현재 서버에서 이미지 분석 서비스에 문제가 있습니다.\n잠시 후 다시 시도해주세요.";
                    DevLog.e(TAG, "403 오류 발생 - 토큰: " + authToken);
                    DevLog.e(TAG, "403 오류 발생 - 원본 토큰: " + userManager.getToken());

                    // 사용자에게 재시도 옵션 제공
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
                    // 재촬영으로 돌아가기
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
        // Authorization 헤더 'Bearer <token>'이 정확히 한 번만 붙도록 보장
        String finalAuthToken = authToken;
        if (!finalAuthToken.startsWith("Bearer ")) {
            finalAuthToken = "Bearer " + finalAuthToken;
        }
        if (finalAuthToken.startsWith("Bearer Bearer ")) {
            finalAuthToken = finalAuthToken.replaceFirst("Bearer ", "");
            finalAuthToken = "Bearer " + finalAuthToken;
        }
        DevLog.d(TAG, "[getAnalysisResult] 최종 Authorization 헤더: " + finalAuthToken);
        apiService.getAnalysisResult(finalAuthToken, analysisId).enqueue(new Callback<AnalysisResultResponse>() {
            @Override
            public void onResponse(Call<AnalysisResultResponse> call, Response<AnalysisResultResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AnalysisResultResponse result = response.body();
                    String disposalMethod = result.getDisposalMethod();
                    String type = result.getTypeForApp();

                    // 분석 결과가 있으면 결과창만 띄움 (포인트 적립/분리수거 기록 API 호출 X)
                    if (type != null && !type.isEmpty()) {
                        showResult(type, disposalMethod);
                    } else {
                        // 타입이 없는 경우
                        Toast.makeText(CameraActivity.this,
                                "분석 결과를 확인할 수 없습니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT).show();
                        showRetryDialog();
                    }
                } else if (response.code() == 403 || response.code() == 401) {
                    Toast.makeText(CameraActivity.this, "인증이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    String errorMessage = "결과 조회에 실패했습니다: " + response.code();
                    Toast.makeText(CameraActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
        bottomSheet.show(getSupportFragmentManager(), "result_bottom_sheet");
        // 결과창이 닫힐 때(뒤로가기 등) 촬영화면으로 전환
        getSupportFragmentManager().executePendingTransactions();
        bottomSheet.getDialog().setOnDismissListener(dialog -> {
            // 촬영화면(카메라 프리뷰)로 전환
            binding.viewFinder.setVisibility(View.VISIBLE);
            binding.capturedImageView.setVisibility(View.GONE);
            binding.btnCapture.setVisibility(View.VISIBLE);
            binding.btnRetake.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
            startCamera();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 카메라 리소스 명시적 해제
        releaseCamera();
        binding = null;
    }

    /**
     * 이미지 압축 함수
     * @param originalFile 원본 이미지 파일
     * @return 압축된 이미지 파일
     */
    private File compressImage(File originalFile) {
        try {
            DevLog.d(TAG, "Original file size: " + originalFile.length() + " bytes");
            DevLog.d(TAG, "Original file path: " + originalFile.getAbsolutePath());

            // 원본 이미지를 Bitmap으로 로드
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            // 적절한 샘플링 크기 계산
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            int maxDimension = 1024; // 최대 해상도를 1024px로 제한

            DevLog.d(TAG, "Original image dimensions: " + imageWidth + "x" + imageHeight);
            DevLog.d(TAG, "Original image mime type: " + options.outMimeType);

            int inSampleSize = 1;
            if (imageHeight > maxDimension || imageWidth > maxDimension) {
                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;

                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2;
                }
            }

            // 실제 이미지 로드
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) {
                DevLog.e(TAG, "이미지 압축 실패: Bitmap 생성 실패");
                return null;
            }

            DevLog.d(TAG, "Compressed bitmap dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            DevLog.d(TAG, "Compressed bitmap config: " + bitmap.getConfig());

            // 압축된 파일 생성
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis());
            File compressedFile = new File(getExternalCacheDir(), "compressed_" + timeStamp + ".jpg");
            DevLog.d(TAG, "Compressed file path: " + compressedFile.getAbsolutePath());

            java.io.FileOutputStream out = new java.io.FileOutputStream(compressedFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out); // 품질을 70%로 낮춤
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

    // type 매핑 함수 추가
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