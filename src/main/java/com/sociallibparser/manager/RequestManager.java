package com.sociallibparser.manager;

import java.io.IOException;

import com.sociallibparser.singelton.OkHttpClientSingelton;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestManager {
    private final OkHttpClient client = OkHttpClientSingelton.getInstance().getClient();

    @SneakyThrows
    public Response Request(Request request) {
        return client.newCall(request).execute();
    }

    @SneakyThrows
    public Response RequestWithRetries(Request request) {
        while(true) {
            Response response = client.newCall(request).execute();
            if (response.code() == 429) {
                Thread.sleep(10000);
            } else {
                return response;
            }
        } 
    }
}
