package com.example.Test.UI.server

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Test.data.repository.ServerRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServerViewModel : ViewModel() {
    val serverRepository = ServerRepositoryImpl()

    private val _editTextPort = MutableLiveData<Int>()
    val editTextPort: LiveData<Int> = _editTextPort

    fun serverOn(port:Int = editTextPort.value!!){
        viewModelScope.launch(Dispatchers.IO){
            serverRepository.startServer(port)
        }
    }


    fun serverOff(){
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.stopServer()
        }
    }


    fun setPort(text: String) {
        _editTextPort.value = text.toInt()

    }
}