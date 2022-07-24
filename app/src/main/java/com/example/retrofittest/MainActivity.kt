package com.example.retrofittest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.retrofittest.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import okhttp3.Dispatcher
import org.chromium.base.Log
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val shopInfoAdapter: ShopInfoAdapter by lazy {
        ShopInfoAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = shopInfoAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        binding.btnSearch.setOnClickListener {
            val selectedItem = when (binding.spinnerCategory.selectedItem) {
                "한식" -> "001"
                "중식" -> "002"
                "경양식,일식" -> "003"
                "기타외식업(다방,패스트푸드등)" -> "004"
                "이 미용업" -> "005"
                "목욕업" -> "006"
                "세탁업" -> "007"
                "숙박업(호텔,여관)" -> "008"
                "영화관람" -> "009"
                "VTR 대여" -> "010"
                "노래방" -> "011"
                "수영장/볼링장/당구장/골프연습장" -> "012"
                "기타서비스업종" -> "013"
                else ->""
            }
            retrofitWork(selectedItem)
        }

        shopInfoAdapter.setOnItemClickListener(object : ShopInfoAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

                val intent = Intent(this@MainActivity, SpecificActivity::class.java)
                intent.putExtra("Info", shopInfoAdapter.currentList[position])
                startActivity(intent)

            }
        })

    }

    private fun retrofitWork(selectedItem : String) {
        val service = RetrofitApi.shopInfoService // service instance
/*        service.getShopInfoData()
            .enqueue(object : Callback<ShopInfoResponse> {

                override fun onResponse(
                    call: Call<ShopInfoResponse>,
                    response: Response<ShopInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        // Log.d("TAG", response.body().toString())
                        val result = response.body()?.listPriceModelStoreService?.row
                        shopInfoAdapter.submitList(result!!)
                    }
                }

                override fun onFailure(call: Call<ShopInfoResponse>, t: Throwable) {
                    Log.d("TAG", t.message.toString())
                }
            })*/

        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getDataCoroutine(selectedItem)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val result = response.body()?.listPriceModelStoreService?.row
                    result?.let {
                        shopInfoAdapter.submitList(it)
                    }
                } else {
                    Log.d("TAG", response.code().toString())
                }
            }
        }

    }
}
