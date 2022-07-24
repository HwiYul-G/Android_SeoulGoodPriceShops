package com.example.retrofittest

//import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
//import retrofit2.http.Query

interface ShopInfoService {
//    @GET("ListPriceModelStoreService/1/40/")
//    fun getShopInfoData(): Call<ShopInfoResponse>

    @GET("ListPriceModelStoreService/1/40/{INDUTY_CODE_SE}")
    suspend fun getDataCoroutine(@Path("INDUTY_CODE_SE") INDUTY_CODE_SE: String): Response<ShopInfoResponse>

//    fun getShopInfoData(
//        @Query("KEY") KEY: String,
//        @Query("Type") Type: String
//    ) : Call<ShopInfoResponse>
}