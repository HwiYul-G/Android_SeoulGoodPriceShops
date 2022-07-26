package com.example.retrofittest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.retrofittest.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import org.chromium.base.Log


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // 주소처리에 필요한 객체
    private lateinit var addr: List<Address>  // addr[0].subLocality ->  ex)노원구 , addr[0].throughfare -> ㅇㅇ동 (날라오는 데이터가 동이 없어서 동 제외

    private lateinit var db: FirebaseFirestore

    private var isFinishedsearchShop: Boolean = false // 좋은 방법이 아닌듯..

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
        // db
        db = FirebaseFirestore.getInstance()


        // 주소처리
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btnLocationSearch.setOnClickListener {
            fetchLocation() // 주소 처리 함수
            retrofitWork() // from retrofit to db
        }


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
                "기타서비스업종" -> "013"
                else -> ""
            }
            // 선택된 번호를 retrofit 일하는 함수로 넘김 (retrofit이 web에 서 정보를 가져와 리사이클려뷰로 넣어줌)
            // retrofitWork(selectedItem)
            showLoadingDialog()
            CoroutineScope(Dispatchers.IO).launch {
                searchShop(selectedItem)
            }


        }

        //  RecyclerView에서 아이템 클릭시 다음 activity로 넘어가는 것
        shopInfoAdapter.setOnItemClickListener(object : ShopInfoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val intent = Intent(this@MainActivity, SpecificActivity::class.java)
                intent.putExtra("Info", shopInfoAdapter.currentList[position])
                startActivity(intent)

            }
        })

    }

    // selectedItem : String이 있엇음.
    private fun retrofitWork() {
        val service = RetrofitApi.shopInfoService // service instance

        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getDataCoroutine()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val result = response.body()?.listPriceModelStoreService?.row
                    result?.let {
                        //shopInfoAdapter.submitList(it) // Adapter에 넣는 처리

                        //db에 넣는 처리
                        for (row in it) {
                            var addArr = row!!.sHADDR.toString().split(" ")
//                            when (addArr[1]) {
//                                "종로구" -> {
//                                    db.collection("shops").document("종로구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "중구" -> {
//                                    db.collection("shops").document("중구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "용산구" -> {
//                                    db.collection("shops").document("용산구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "성동구" -> {
//                                    db.collection("shops").document("성동구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "광진구" -> {
//                                    db.collection("shops").document("광진구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "동대문구" -> {
//                                    db.collection("shops").document("동대문구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "중랑구" -> {
//                                    db.collection("shops").document("중랑구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "성북구" -> {
//                                    db.collection("shops").document("성북구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "강북구" -> {
//                                    db.collection("shops").document("강북구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "도봉구" -> {
//                                    db.collection("shops").document("도봉구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "노원구" -> {
//                                    db.collection("shops").document("노원구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "은평구" -> {
//                                    db.collection("shops").document("은평구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "서대문구" -> {
//                                    db.collection("shops").document("서대문구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "마포구" -> {
//                                    db.collection("shops").document("마포구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "양천구" -> {
//                                    db.collection("shops").document("양천구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "강서구" -> {
//                                    db.collection("shops").document("강서구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "구로구" -> {
//                                    db.collection("shops").document("구로구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "금천구" -> {
//                                    db.collection("shops").document("금천구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "영등포구" -> {
//                                    db.collection("shops").document("영등포구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "동작구" -> {
//                                    db.collection("shops").document("동작구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "관악구" -> {
//                                    db.collection("shops").document("관악구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "서초구" -> {
//                                    db.collection("shops").document("종로구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "강남구" -> {
//                                    db.collection("shops").document("강남구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "송파구" -> {
//                                    db.collection("shops").document("송파구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                                "강동구" -> {
//                                    db.collection("shops").document("강동구")
//                                        .collection(row!!.iNDUTYCODESE.toString())
//                                        .document(row!!.sHID.toString()).set(row)
//                                }
//                            }
                        }
                    }
                } else {
                    Log.d("TAG", response.code().toString())
                }
            }
        }
    }

    private fun documentSnapshotToRow(document: DocumentSnapshot): Row {
        val row: Row = Row(
            document.get("baseym").toString(),
            document.get("indutycodese").toString(),
            document.get("indutycodesename").toString(),
            document.get("shaddr").toString(),
            document.get("shid").toString(),
            document.get("shinfo").toString(),
            document.get("shname").toString(),
            document.get("shphone").toString(),
            document.get("shphoto").toString(),
            document.get("shpride").toString(),
            document.get("shrcmn").toString().toInt(),
            document.get("shway").toString()
        )
        return row
    }

    // 위치기반으로 찾아진 구와 Spinner로 선택된 범주로 데이터 가져오는 함수
    private suspend fun searchShop(selectedItem: String) {
        isFinishedsearchShop = false

        when (binding.tvLocation.text.toString()) {
            "종로구" -> {
                val colRef = db.collection("shops").document("종로구").collection(selectedItem)
                var tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    for (document in result) {
                        val row = documentSnapshotToRow(document)
                        // tempList.add(document.data as Row)
                    }
                }
                shopInfoAdapter.submitList(tempList)
            }
            "중구" -> {

            }
            "용산구" -> {
                val docRef = db.collection("용산구").document(selectedItem)
            }
            "성동구" -> {
                val docRef = db.collection("성동구").document(selectedItem)
            }
            "광진구" -> {
                val docRef = db.collection("광진구").document(selectedItem)
            }
            "동대문구" -> {
                val docRef = db.collection("동대문구").document(selectedItem)
            }
            "중랑구" -> {
                val docRef = db.collection("중랑구").document(selectedItem)
            }
            "성북구" -> {
                val docRef = db.collection("성북구").document(selectedItem)
            }
            "강북구" -> {
                val docRef = db.collection("강북구").document(selectedItem)
            }
            "도봉구" -> {
                val docRef = db.collection("도봉구").document(selectedItem)
            }
            "노원구" -> {
                val docRef = db.collection("노원구").document(selectedItem)
            }
            "은평구" -> {
                val docRef = db.collection("은평구").document(selectedItem)
            }
            "서대문구" -> {
                val colRef = db.collection("shops").document("서대문구").collection(selectedItem)
                var tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    for (document in result) {
                        val row = documentSnapshotToRow(document) // 문제 없음.
                        tempList.add(row)
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }
                shopInfoAdapter.submitList(tempList)

            }
            "마포구" -> {
                val docRef = db.collection("마포구").document(selectedItem)
            }
            "양천구" -> {
                val docRef = db.collection("양천구").document(selectedItem)
            }
            "강서구" -> {
                val docRef = db.collection("강서구").document(selectedItem)
            }
            "구로구" -> {
                val docRef = db.collection("구로구").document(selectedItem)
            }
            "금천구" -> {
                val docRef = db.collection("금천구").document(selectedItem)
            }
            "영등포구" -> {
                val docRef = db.collection("영등포구").document(selectedItem)
            }
            "동작구" -> {
                val docRef = db.collection("동작구").document(selectedItem)
            }
            "관악구" -> {
                val docRef = db.collection("관악구").document(selectedItem)
            }
            "서초구" -> {
                val docRef = db.collection("서초구").document(selectedItem)
            }
            "강남구" -> {
                val docRef = db.collection("강남구").document(selectedItem)
            }
            "송파구" -> {
                val docRef = db.collection("송파구").document(selectedItem)
            }
            "강동구" -> {
                val docRef = db.collection("강동구").document(selectedItem)
            }
        }
        isFinishedsearchShop = true
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
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                // it이 위치정보를 가지고 있음. it.latitude (위도(가로)), it.longitude (경도(세로))
                // Toast.makeText(applicationContext, "${it.latitude} ${it.longitude}",Toast.LENGTH_SHORT).show() // 디버깅용
                // binding.tvLocation.text = "${it.latitude}, ${it.longitude}"
                val geocoder: Geocoder = Geocoder(this)
                addr = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                binding.tvLocation.text = "${addr[0].subLocality}"
            }
        }

    }

    // === Loading Dialog ====
    private fun showLoadingDialog() {
        val dialog = LoadingDialog(this@MainActivity)
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("TAG", "showLoadingDialog() inside 1 ")
            dialog.show()
            while (true) {
                Log.d("TAG", "showLoadingDialog() inside 2 ")
                delay(30000) // 30초 씩 정지? 시간을 넣어야하는데...
                if (isFinishedsearchShop) break
            }
            dialog.dismiss()
            Log.d("TAG", "showLoadingDialog() inside 3 ")
        }
    }
}
