package com.disnak.puas.model;

/**
 * Created by Lenovo on 16/04/2018.
 */

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RegisterAPI {
    @FormUrlEncoded
    @POST("insert.php")
    Call<Value> btnBerikut(@Field("soalInsert") String soalInsert,
                           @Field("jawaban") String jawaban,
                           @Field("volume") String volume,
                           @Field("nip") String nip,
                           @Field("username") String username);
}
