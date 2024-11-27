package com.example.sezin.ui.sos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SOSViewModel : ViewModel() {

    // LiveData to expose UI-related data
    private val _text = MutableLiveData<String>().apply {
        value = "SOS Feature is ready to use!"
    }
    val text: LiveData<String> = _text

    fun updateStatusMessage(message: String) {
        _text.value = message
    }
}
