package com.example.retrofittest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
    private lateinit var addr: List<Address>
    private lateinit var db: FirebaseFirestore
    private var isFinishedsearchShop: Boolean = false // 좋은 방법이 아닌듯..
    private lateinit var arrayAdr : Array<String>

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val shopInfoAdapter: ShopInfoAdapter by lazy { // RecyclerView의 Adapter 연결
        ShopInfoAdapter()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "착한 업소 찾기"
        db = FirebaseFirestore.getInstance()
        arrayAdr = resources.getStringArray(R.array.adr_array)   //구 배열

        checkPermission()
        binding.recyclerView.visibility = View.GONE

        // Update DB. User can't using this btn
        binding.btnUpdateDb.setOnClickListener {
            retrofitWork() // from retrofit to db
        }

        binding.imageButton.setOnClickListener {
            // Dialog만들기
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("위치 설정")

            // adr_spinner와 adapter 연결
            val adrSpinner = mDialogView.findViewById<Spinner>(R.id.spinner_adr)
            ArrayAdapter.createFromResource(
                this,
                R.array.adr_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adrSpinner.adapter = adapter
            }
            val mAlertDialog = mBuilder.show()


            // 현재위치 버튼 클릭시
            val btn_myLocation = mDialogView.findViewById<Button>(R.id.btn_myLocation)
            btn_myLocation.setOnClickListener {

                // 현재 주소 가져오기
                fetchLocation(adrSpinner)
            }

            // 확인 버튼 클릭시
            val btn_ok = mDialogView.findViewById<Button>(R.id.btn_ok)
            btn_ok.setOnClickListener {
                binding.tvLocation.text = adrSpinner.selectedItem.toString()
                mAlertDialog.dismiss()
            }
        }

        // 주소처리
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Apply an adapter to the search category Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
        // Applying an Adapter to the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = shopInfoAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Get the data from db when user click the search btn
        binding.btnSearch.setOnClickListener {
            binding.recyclerView.visibility = View.VISIBLE
            binding.scrollviewDescription.visibility = View.GONE

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
                else ->""
            }
            showLoadingDialog()
            searchShop(selectedItem)
        }

        //  Switching Activity when clicking an item in RecyclerView
        shopInfoAdapter.setOnItemClickListener(object : ShopInfoAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

                val intent = Intent(this@MainActivity, SpecificActivity::class.java)
                intent.putExtra("Info", shopInfoAdapter.currentList[position])
                startActivity(intent)
            }
        })

    }

    // Insert data into DB using Retrofit (Http)
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
                            when (addArr[1]) {
                                "종로구" -> {
                                    db.collection("shops").document("종로구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "중구" -> {
                                    db.collection("shops").document("중구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "용산구" -> {
                                    db.collection("shops").document("용산구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "성동구" -> {
                                    db.collection("shops").document("성동구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "광진구" -> {
                                    db.collection("shops").document("광진구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "동대문구" -> {
                                    db.collection("shops").document("동대문구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "중랑구" -> {
                                    db.collection("shops").document("중랑구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "성북구" -> {
                                    db.collection("shops").document("성북구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "강북구" -> {
                                    db.collection("shops").document("강북구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "도봉구" -> {
                                    db.collection("shops").document("도봉구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "노원구" -> {
                                    db.collection("shops").document("노원구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "은평구" -> {
                                    db.collection("shops").document("은평구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "서대문구" -> {
                                    db.collection("shops").document("서대문구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "마포구" -> {
                                    db.collection("shops").document("마포구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "양천구" -> {
                                    db.collection("shops").document("양천구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "강서구" -> {
                                    db.collection("shops").document("강서구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "구로구" -> {
                                    db.collection("shops").document("구로구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "금천구" -> {
                                    db.collection("shops").document("금천구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "영등포구" -> {
                                    db.collection("shops").document("영등포구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "동작구" -> {
                                    db.collection("shops").document("동작구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "관악구" -> {
                                    db.collection("shops").document("관악구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "서초구" -> {
                                    db.collection("shops").document("종로구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "강남구" -> {
                                    db.collection("shops").document("강남구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "송파구" -> {
                                    db.collection("shops").document("송파구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                                "강동구" -> {
                                    db.collection("shops").document("강동구")
                                        .collection(row!!.iNDUTYCODESE.toString())
                                        .document(row!!.sHID.toString()).set(row)
                                }
                            }
                        }
                    }
                } else {
                    Log.d("TAG", response.code().toString())
                }
            }
        }
    }

    // Action Bar's Menu Add
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    // Action Bar's Menu Click Event
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_btn_home ->{
                binding.recyclerView.visibility =View.GONE
                binding.scrollviewDescription.visibility = View.VISIBLE
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Check the use of location permissions from users
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
    }

    // Get my (x, y) coordinated and Replace them with Gu
    private fun fetchLocation(adrSpinner: Spinner) {
        val task = fusedLocationProviderClient.lastLocation

        checkPermission()

        task.addOnCompleteListener {
            // it이 위치정보를 가지고 있음. it.latitude (위도(가로)), it.longitude (경도(세로))

            val geocoder : Geocoder = Geocoder(this)
            addr = geocoder.getFromLocation(it.result.latitude, it.result.longitude,1)

            //adr_array의 몇번째 인덱스인지 가져오기
            var index : Int = arrayAdr.indexOf("${addr[0].subLocality}")

            //spinner의 선택된 아이템 변경
            adrSpinner.setSelection(index)
        }
    }

    // Parsing Data Type for getting data
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

    // Get data From DB
    private  fun searchShop(selectedItem: String) {
        isFinishedsearchShop = false
        when (binding.tvLocation.text.toString()) {
            "종로구" -> {
                val colRef = db.collection("shops").document("종로구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "중구" -> {
                val colRef = db.collection("shops").document("중구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "용산구" -> {
                val colRef = db.collection("shops").document("용산구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }
            }
            "성동구" -> {
                val colRef = db.collection("shops").document("성동구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "광진구" -> {
                val colRef = db.collection("shops").document("광진구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "동대문구" -> {
                val colRef = db.collection("shops").document("동대문구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "중랑구" -> {
                val colRef = db.collection("shops").document("중랑구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "성북구" -> {
                val colRef = db.collection("shops").document("성북구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "강북구" -> {
                val colRef = db.collection("shops").document("강북구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "도봉구" -> {
                val colRef = db.collection("shops").document("도봉구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "노원구" -> {
                val colRef = db.collection("shops").document("노원구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "은평구" -> {
                val colRef = db.collection("shops").document("은평구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "서대문구" -> {
                val colRef = db.collection("shops").document("서대문구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    val tempList: MutableList<Row> = mutableListOf<Row>()
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "마포구" -> {
                val colRef = db.collection("shops").document("마포구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "양천구" -> {
                val colRef = db.collection("shops").document("양천구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "강서구" -> {
                val colRef = db.collection("shops").document("강서구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "구로구" -> {
                val colRef = db.collection("shops").document("구로구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "금천구" -> {
                val colRef = db.collection("shops").document("금천구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "영등포구" -> {
                val colRef = db.collection("shops").document("영등포구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "동작구" -> {
                val colRef = db.collection("shops").document("동작구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "관악구" -> {
                val colRef = db.collection("shops").document("관악구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "서초구" -> {
                val colRef = db.collection("shops").document("서초구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }

            }
            "강남구" -> {
                val colRef = db.collection("shops").document("강남구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }
            }
            "송파구" -> {
                val colRef = db.collection("shops").document("송파구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }
            }
            "강동구" -> {
                val colRef = db.collection("shops").document("강동구").collection(selectedItem)
                val tempList: MutableList<Row> = mutableListOf<Row>()
                colRef.get().addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for (document in result) {
                            val row = documentSnapshotToRow(document)
                            tempList.add(row)
                        }
                        runOnUiThread {
                            shopInfoAdapter.submitList(tempList)
                        }
                        return@addOnSuccessListener
                    }
                    runOnUiThread {
                        isFinishedsearchShop = true
                        Toast.makeText(this,"검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.d("TAG", "Error : ", e)
                }
            }

        }
        isFinishedsearchShop = true
    }

    // when getting data, show loading
    private fun showLoadingDialog() {
        val dialog = LoadingDialog(this@MainActivity)
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            while (true) {
                delay(700)
                if(isFinishedsearchShop) break
            }
            dialog.dismiss()
        }
    }
}
