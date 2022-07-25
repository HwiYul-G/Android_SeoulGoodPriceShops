package com.example.retrofittest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


class SpecificActivity : AppCompatActivity() {
    // 연결은 임시로 연결..
    lateinit var imageView : ImageView
    lateinit var storeTxt : TextView
    lateinit var addressTxt : TextView
    lateinit var phoneTxt : TextView
    lateinit var introTxt : TextView
    lateinit var infoTxt : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific)

        imageView = findViewById(R.id.imageView)
        storeTxt = findViewById(R.id.storeTxt)
        addressTxt = findViewById(R.id.addressTxt)
        phoneTxt = findViewById(R.id.phoneTxt)
        introTxt = findViewById(R.id.introTxt)
        infoTxt = findViewById(R.id.infoTxt)

        // 받은 데이터 사용법 infoList.어쩌구
        val infoList = intent.getParcelableExtra<Row>("Info")

        imageView = findViewById(R.id.imageView)
        Glide.with(this).load(infoList!!.sHPHOTO).into(imageView)
        storeTxt.text = infoList.sHNAME
        addressTxt.text = infoList.sHADDR
        phoneTxt.text = infoList.sHPHONE
        introTxt.text = infoList.sHPRIDE
        infoTxt.text = infoList.sHINFO

    }
}