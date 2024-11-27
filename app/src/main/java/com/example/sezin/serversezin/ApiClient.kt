package com.example.sezin.serversezin

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://192.168.89.18:3000/"

    val gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(OkHttpClient.Builder().build())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

