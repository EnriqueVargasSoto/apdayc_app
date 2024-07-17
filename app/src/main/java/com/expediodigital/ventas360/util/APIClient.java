package com.expediodigital.ventas360.util;


import com.expediodigital.ventas360.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class    APIClient {

    public static String BASE_URL = "http://208.117.85.123/dlbebidas/";
    public static String BASE_URL_2 = "https://images.atiendo.pe/";

    public static APIInterface getClient() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_2)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getRetrofitClient())
                .build();

        return retrofit.create(APIInterface.class);
    }

    public static APIInterface getClientTemp() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getRetrofitClient())
                .build();

        return retrofit.create(APIInterface.class);
    }

    private static OkHttpClient getRetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.readTimeout(60, TimeUnit.SECONDS);
        client.connectTimeout(60, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG)
            client.addInterceptor(logging);
        return client.build();
    }
}
