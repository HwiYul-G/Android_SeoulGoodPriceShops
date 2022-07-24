package com.example.retrofittest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Retrofit은 비용이 높아서 자원이 낭비되므로 object로 싱글톤 처리
object RetrofitApi {
    private const val BASE_URL = "http://openAPI.seoul.go.kr:8088/416b415768796a6931303673584b596d/json/"

    // 서버와 클라이언트 사이에서 데이터를 가로채는 인터셉터임
    private val okHttpClient : OkHttpClient by lazy{
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit: Retrofit by lazy{
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create()) //DTO 변환에 사용하는 JSON 컨버터를 MoshiConverter로 지정
            .client(okHttpClient) 
            .baseUrl(BASE_URL) 
            .build() // retrofit 객체 생성
    }

    val shopInfoService : ShopInfoService by lazy {
        retrofit.create(ShopInfoService::class.java)
    }
}