package com.example.retrofittest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SpecificActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific)

        val infoList = intent.getParcelableExtra<Row>("Info")
        Log.d("TAG",infoList.toString())

    }
}