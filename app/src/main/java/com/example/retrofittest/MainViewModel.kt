package com.example.retrofittest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val isFinishedserachShopFunc : MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>()
    }

}