public class ApiClient {
    private static final String BASE_URL = "https://recycle-9bar.onrender.com/";
    private static ApiClient instance;
    private final ApiService apiService;

    private ApiClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    String token = UserManager.getInstance(context).getToken();

                    Request.Builder requestBuilder = original.newBuilder();

                    if (token != null && !token.isEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(requestBuilder.build());
                })
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}