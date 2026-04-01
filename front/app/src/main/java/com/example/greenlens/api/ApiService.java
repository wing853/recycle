package com.example.greenlens.api;

import com.example.greenlens.model.User;
import com.example.greenlens.model.request.LoginRequest;
import com.example.greenlens.model.request.SignupRequest;
import com.example.greenlens.model.response.LoginResponse;
import com.example.greenlens.model.response.SignupResponse;
import com.example.greenlens.model.response.AnalyzeResponse;
import com.example.greenlens.model.response.AnalysisResultResponse;
import com.example.greenlens.model.response.PointResponse;
import com.example.greenlens.model.request.PointUseRequest;
import com.example.greenlens.model.AppSettings;
import com.example.greenlens.model.response.LeaderboardResponse;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;
import java.util.List;

import okhttp3.MultipartBody;

public interface ApiService {

    // ===== 사용자 =====

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("users/signup")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);

    // ⭐ 내 정보 조회 (JWT 기반)
    @GET("users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    // ⭐ 내 정보 수정
    @PUT("users/profile")
    Call<User> updateUserProfile(
            @Header("Authorization") String token,
            @Body User user
    );

    // ⭐ 회원 탈퇴
    @DELETE("users/profile")
    Call<Void> deleteAccount(@Header("Authorization") String token);

    @POST("users/logout")
    Call<Void> logout(@Header("Authorization") String token);


    // ===== 이미지 분석 =====

    @POST("recycle/analyze")
    Call<AnalyzeResponse> analyzeImage(
            @Header("Authorization") String token,
            @Body String base64Image
    );

    @Multipart
    @POST("recycle/analyze")
    Call<AnalyzeResponse> analyzeImageMultipart(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    @GET("recycle/result/{analysis_id}")
    Call<AnalysisResultResponse> getAnalysisResult(
            @Header("Authorization") String token,
            @Path("analysis_id") Long analysisId
    );


    // ===== 포인트 =====

    @GET("users/{user_id}/points")
    Call<PointResponse> getUserPoints(
            @Header("Authorization") String token,
            @Path("user_id") Long userId
    );

    @POST("users/{user_id}/points/use")
    Call<PointResponse> usePoints(
            @Header("Authorization") String token,
            @Path("user_id") Long userId,
            @Body PointUseRequest request
    );

    @GET("users/{user_id}/points/history")
    Call<List<Map<String, Object>>> getPointHistory(
            @Header("Authorization") String token,
            @Path("user_id") Long userId
    );


    // ===== 분리수거 =====

    @GET("recycle/logs")
    Call<List<Map<String, Object>>> getRecycleActivities(
            @Header("Authorization") String token
    );

    @DELETE("recycle/log/{log_id}")
    Call<Map<String, Object>> deleteRecycleActivity(
            @Header("Authorization") String token,
            @Path("log_id") Long logId
    );


    // ===== 앱 설정 =====

    @GET("settings")
    Call<AppSettings> getAppSettings(
            @Header("Authorization") String token
    );

    @PUT("settings")
    Call<AppSettings> updateAppSettings(
            @Header("Authorization") String token,
            @Body AppSettings settings
    );


    // ===== 랭킹 =====

    @GET("ranking/leaderboard")
    Call<LeaderboardResponse> getLeaderboard(
            @Header("Authorization") String token
    );


    // ===== 쿠폰 =====

    @GET("coupons")
    Call<Map<String, Object>> getCoupons(@Header("Authorization") String token);

    @POST("shop/coupons/{coupon_id}/purchase")
    Call<Map<String, Object>> purchaseCoupon(
            @Header("Authorization") String token,
            @Path("coupon_id") Long couponId
    );

    @POST("shop/coupons/{couponId}/use")
    Call<Map<String, Object>> useCoupon(
            @Header("Authorization") String token,
            @Path("couponId") Long couponId
    );

    @GET("users/{user_id}/coupons")
    Call<Map<String, Object>> getUserCoupons(
            @Header("Authorization") String token,
            @Path("user_id") Long userId
    );
}