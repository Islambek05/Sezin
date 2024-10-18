package com.example.sezin.serversezin

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("connect")
    fun connectToDatabase(): Call<String>
}
