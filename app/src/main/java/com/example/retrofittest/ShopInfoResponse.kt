package com.example.retrofittest


import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize


data class ShopInfoResponse(
    @field:Json(name = "ListPriceModelStoreService")
    val listPriceModelStoreService: ListPriceModelStoreService?
)

data class ListPriceModelStoreService(
    @field:Json(name = "list_total_count")
    val listTotalCount: Int?,
    @field:Json(name = "RESULT")
    val rESULT: RESULT?,
    @field:Json(name = "row")
    val row: List<Row?>?
)

data class RESULT(
    @field:Json(name = "CODE")
    val cODE: String?,
    @field:Json(name = "MESSAGE")
    val mESSAGE: String?
)

@Parcelize
data class Row(
    @field:Json(name = "BASE_YM") // 장미미용실
    val bASEYM: String?,
    @field:Json(name = "INDUTY_CODE_SE") // 005
    val iNDUTYCODESE: String?,
    @field:Json(name = "INDUTY_CODE_SE_NAME") // 이 미용업
    val iNDUTYCODESENAME: String?,
    @field:Json(name = "SH_ADDR") // 서울특별시 중랑구 봉화산로 56길 123, 중앙하이츠상가 1층
    val sHADDR: String?,
    @field:Json(name = "SH_ID") // 00000263
    val sHID: String?,
    @field:Json(name = "SH_INFO") // 영업시간 : 09시30분 -22시\r\n좌석수 : 5석\r\n 휴무올 : 화요일\r\n 예약 : 가능 등등등등
    val sHINFO: String?,
    @field:Json(name = "SH_NAME") // 장미미용실
    val sHNAME: String?,
    @field:Json(name = "SH_PHONE") // "" 
    val sHPHONE: String?,
    @field:Json(name = "SH_PHOTO") // "img 주소"
    val sHPHOTO: String?,
    @field:Json(name = "SH_PRIDE") // 주변보다 저렴한 가격으로 서비스하며 어르신 파마와 커트시 할인해주는 업소, \r\n 등등등
    val sHPRIDE: String?,
    @field:Json(name = "SH_RCMN") // 137
    val sHRCMN: Int?,
    @field:Json(name = "SH_WAY") // "신내1동 신내초등학교 후문 쪽"
    val sHWAY: String?
) : Parcelable
