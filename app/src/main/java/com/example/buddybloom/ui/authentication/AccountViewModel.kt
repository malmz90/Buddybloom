package com.example.buddybloom.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddybloom.data.repository.AccountRepository

class AccountViewModel : ViewModel() {

    private val accountRepository = AccountRepository()

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult : LiveData<Boolean> get() = _registerResult

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Boolean>()
    val resetPasswordResult: LiveData<Boolean> get() = _resetPasswordResult

    fun registerUser(email : String, password : String, name : String) {
        accountRepository.registerUser(email, password, name) { success ->
            _registerResult.value = success
        }

    }
    fun loginUser(email: String, password: String) {
        accountRepository.loginUser(email, password) { success ->
            _loginResult.value = success
        }
    }

    fun sendPasswordResetEmail(email: String){
        if(email.isEmpty()){
            _resetPasswordResult.value =false
            return
        }
        accountRepository.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            _resetPasswordResult.value = task.isSuccessful
        }
    }
}