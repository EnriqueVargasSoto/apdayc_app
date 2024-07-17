package com.expediodigital.ventas360.util;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface APIInterface {

    @Multipart
    @POST("uploadApi.php")
    Call<ImageBotellasDTO> callProcessImage(
            @PartMap() Map<String, RequestBody> partMap,
            @Part MultipartBody.Part profileImage);

    @Multipart
    @POST("uploadImage.php")
    Call<ImageJavierDTO> sendImage(
            @PartMap() Map<String, RequestBody> partMap,
            @Part MultipartBody.Part profileImage);

}
