package com.example.dermabsa.ui

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dermabsa.model.BodyRegion

class MainViewModel : ViewModel() {
    val selectedRegion = MutableLiveData<BodyRegion>()
    val patientPhoto = MutableLiveData<Bitmap>()
}