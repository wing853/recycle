package com.example.greenlens.api;

import com.example.greenlens.model.User;
import com.example.greenlens.model.request.LoginRequest;
import com.example.greenlens.model.request.SignupRequest;
import com.example.greenlens.model.response.LoginResponse;
import com.example.greenlens.model.response.SignupResponse;
import com.example.greenlens.model.response.AnalyzeResponse;
import com.example.greenlens.model.response.AnalysisResultResponse;
import com.example.greenlens.model.Point;
import com.example.greenlens.model.response.PointResponse;
import com.example.greenlens.model.request.PointUseRequest;
import com.example.greenlens.model.AppSettings;
import com.example.greenlens.model.response.LeaderboardResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

import java.util.Map;
import java.util.List;
import okhttp3.MultipartBody;

public interface ApiService {
    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("users/signup")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);

    @GET("users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    @PUT("users/{userId}")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Path("userId") Long userId, @Body User user);

    @DELETE("users/{userId}")
    Call<Void> deleteAccount(@Header("Authorization") String token, @Path("userId") Long userId);

    @POST("users/logout")
    Call<Void> logout(@Header("Authorization") String token);

    // 이미지 분석 요청 API
    @POST("recycle/analyze")
    Call<AnalyzeResponse> analyzeImage(
            @Header("Authorization") String token,
            @Body String base64Image);

    // Multipart 이미지 분석 요청 API
    @Multipart
    @POST("recycle/analyze")
    Call<AnalyzeResponse> analyzeImageMultipart(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image);

    // 분석 결과 조회 API
    @GET("recycle/result/{analysis_id}")
    Call<AnalysisResultResponse> getAnalysisResult(
            @Header("Authorization") String token,
            @Path("analysis_id") Long analysisId);

    // 포인트 조회 API
    @GET("users/{user_id}/points")
    Call<PointResponse> getUserPoints(
            @Header("Authorization") String token,
            @Path("user_id") Long userId);

    // 포인트 사용 API
    @POST("users/{user_id}/points/use")
    Call<PointResponse> usePoints(
            @Header("Authorization") String token,
            @Path("user_id") Long userId,
            @Body PointUseRequest request);

    // 포인트 내역 조회 API
    @GET("users/{user_id}/points/history")
    Call<List<Map<String, Object>>> getPointHistory(
            @Header("Authorization") String token,
            @Path("user_id") Long userId
    );

    // 분리수거 활동 조회 API
    @GET("recycle/logs")
    Call<List<Map<String, Object>>> getRecycleActivities(
            @Header("Authorization") String token);

    // 분리수거 활동 삭제 API
    @DELETE("recycle/log/{log_id}")
    Call<Map<String, Object>> deleteRecycleActivity(
            @Header("Authorization") String token,
            @Path("log_id") Long logId);

    // 앱 설정 조회 API
    @GET("settings")
    Call<AppSettings> getAppSettings(
            @Header("Authorization") String token);

    // 앱 설정 변경 API
    @PUT("settings")
    Call<AppSettings> updateAppSettings(
            @Header("Authorization") String token,
            @Body AppSettings settings);

    // 랭킹 조회 API
    @GET("ranking/leaderboard")
    Call<LeaderboardResponse> getLeaderboard(
            @Header("Authorization") String token);

    // ======== 쿠폰/상품권 관련 API ========

    // 상품권 목록 조회 API
    @GET("coupons")
    Call<Map<String, Object>> getCoupons(@Header("Authorization") String token);

    // 상품권 구매 API
    @POST("shop/coupons/{coupon_id}/purchase")
    Call<Map<String, Object>> purchaseCoupon(
            @Header("Authorization") String token,
            @Path("coupon_id") Long couponId);

    // 쿠폰 사용 API
    @POST("shop/coupons/{couponId}/use")
    Call<Map<String, Object>> useCoupon(
            @Header("Authorization") String token,
            @Path("couponId") Long couponId
    );

    // 사용자 쿠폰함 조회 API
    @GET("users/{user_id}/coupons")
    Call<Map<String, Object>> getUserCoupons(@Header("Authorization") String token, @Path("user_id") Long userId);
}