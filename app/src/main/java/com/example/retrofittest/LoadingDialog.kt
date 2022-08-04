package com.example.retrofittest

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class LoadingDialog
constructor(context: Context) : Dialog(context) {

    init {
        // 다이얼로그 외부 화면을 터치할 때 다이얼로그가 종료되지 않도록 함
        setCanceledOnTouchOutside(false)
        // 다이얼로그의 배경이 투명이 되도록 하는 처리
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_loading)
    }
}