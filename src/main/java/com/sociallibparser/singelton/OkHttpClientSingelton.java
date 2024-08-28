package com.sociallibparser.singelton;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpClientSingelton {
    private static OkHttpClientSingelton INSTANCE;
    private final OkHttpClient client;
    private final ConfigReaderSingelton configReader;
    
    public OkHttpClientSingelton() {
        configReader = ConfigReaderSingelton.getInstance();
        String token = configReader.getProperty("token");
        client = new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux i675 ) AppleWebKit/537.2 (KHTML, like Gecko) Chrome/55.0.3924.176 Safari/534");
                
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }) .build();        
    }

    public synchronized static OkHttpClientSingelton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OkHttpClientSingelton();
        }
        return INSTANCE;
    }

    public OkHttpClient getClient() {
        return client;
    }
}
