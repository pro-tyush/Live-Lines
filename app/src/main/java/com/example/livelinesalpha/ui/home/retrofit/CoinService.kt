package com.example.livelinesalpha.ui.home.retrofit

import com.example.livelinesalpha.ui.home.model.Coin
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://api.coincap.io/v2/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface CoinService {
    @GET("assets")
    fun getAssetInfo(): Call<Coin>
}

object CryptoApi {
    val retrofitService: CoinService by lazy {
        retrofit.create(CoinService::class.java)
    }
}