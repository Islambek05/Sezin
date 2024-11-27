package com.example.sezin.serversezin

import com.example.sezin.user.User
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("connect")
    fun connectToDatabase(): Call<String>

    @POST("register")
    fun registerUser(@Body user: User): Call<JsonElement>
}
