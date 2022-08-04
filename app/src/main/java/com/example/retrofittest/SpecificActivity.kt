package com.example.retrofittest

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.retrofittest.databinding.ActivitySpecificBinding


class SpecificActivity : AppCompatActivity() {
    private val binding: ActivitySpecificBinding by lazy {
        ActivitySpecificBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "상세 정보"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get Intent
        val infoList = intent.getParcelableExtra<Row>("Info")


        Glide.with(this).load(infoList!!.sHPHOTO).into(binding.imageView)
        binding.storeTxt.text = infoList.sHNAME.toString()
        binding.addressTxt.text = infoList.sHADDR.toString()
        binding.phoneTxt.text = infoList.sHPHONE.toString()
        binding.introTxt.text = infoList.sHPRIDE.toString()
        binding.infoTxt.text = infoList.sHINFO.toString()

        // 구글 지도로 이어지는 버튼
        binding.btnToMap.setOnClickListener {
            var intent: Intent = Intent(Intent.ACTION_VIEW)

            intent.setData(Uri.parse("https://www.google.com/maps/search/?api=1&query=${infoList.sHNAME}+${infoList.sHADDR}"))
            startActivity(intent)
        }
    }

    // action Bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}