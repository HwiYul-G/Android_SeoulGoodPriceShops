package com.example.retrofittest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.retrofittest.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.chromium.base.Log


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // 주소처리에 필요한 객체
    private lateinit var addr : List<Address>  // addr[0].subLocality ->  ex)노원구 , addr[0].throughfare -> ㅇㅇ동

    // MainActivity.xml과 .kt 의 binding 처리
    // tv_addr = findViewById(@어쩌구) 했던 것 없이 binding.tvAddr.text = "텍스트" 이런 식으로 사용가능
    private val binding: ActivityMainBinding by lazy { 
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val shopInfoAdapter: ShopInfoAdapter by lazy { // RecyclerView의 Adapter 연결
        ShopInfoAdapter()
    }
    
    // ====== onCreate() ======
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 주소처리
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation() // 주소 처리 함수
        
        // 검색 범주를 드롭다운하는 Spinner에 어댑터 연결 -> 드롭다운 시 아이템이 보임
        ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
        // RecycelrView의 어뎁터 적용
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = shopInfoAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // 버튼 클릭 이벤트
        binding.btnSearch.setOnClickListener {
            // 스피너에 선택된 아이템을 001 등의 번호로 변경
            // VTR 대여, 노래방, 수영장/볼링장/당구장/골프연습장은 제공해주는 데이터가 없어서 갱신이 안되기 때문에 빼기로 함
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
            // 선택된 번호를 retrofit 일하는 함수로 넘김 (retrofit이 web에 서 정보를 가져와 리사이클려뷰로 넣어줌)
            retrofitWork(selectedItem)
        }

        //  RecyclerView에서 아이템 클릭시 다음 activity로 넘어가는 것
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

    // ===== Location =====
    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if(it != null){
                // it이 위치정보를 가지고 있음. it.latitude (위도(가로)), it.longitude (경도(세로))
                // Toast.makeText(applicationContext, "${it.latitude} ${it.longitude}",Toast.LENGTH_SHORT).show() // 디버깅용
                // binding.tvLocation.text = "${it.latitude}, ${it.longitude}"
                val geocoder : Geocoder = Geocoder(this)
                addr = geocoder.getFromLocation(it.latitude,it.longitude,1)
                binding.tvLocation.text = "${addr[0].subLocality} ${addr[0].thoroughfare}"
            }
        }

    }


}
