package com.example.buddybloom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult : LiveData<Boolean> get() = _registerResult




    fun registerUser(email : String, password : String, name : String) {
        firebaseManager.registerUser(email, password, name) { success ->
            _registerResult.value = success
        }
    }


}