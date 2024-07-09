package com.example.Test.UI.client.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConfigViewModel : ViewModel() {

    private val _editTextIp = MutableLiveData<String>()
    val editTextIp: LiveData<String> = _editTextIp

    private val _editTextPort = MutableLiveData<Int>()
    val editTextPort: LiveData<Int> = _editTextPort

    private val _editTextPeriodicity = MutableLiveData<Int>()
    val editTextPeriodicity: LiveData<Int> = _editTextPeriodicity

    fun setConf(ip: String, port: Int, periodically: Int){
        _editTextIp.value = ip
        _editTextPort.value = port
        _editTextPeriodicity.value = periodically
    }

}