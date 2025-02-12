package com.example.buddybloom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult : LiveData<Boolean> get() = _registerResult

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult



    fun registerUser(email : String, password : String, name : String) {
        firebaseManager.registerUser(email, password, name) { success ->
            _registerResult.value = success
        }

    }
    fun loginUser(email: String, password: String) {
        firebaseManager.loginUser(email, password) { success ->
            _loginResult.value = success
        }
    }

}