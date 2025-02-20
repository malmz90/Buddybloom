package com.example.buddybloom.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.repository.AccountRepository
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val accountRepository = AccountRepository()

    // Livedata for the registration result, updated in registerUser below and observed in RegisterFragment.
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult : LiveData<Boolean> get() = _registerResult

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Boolean>()
    val resetPasswordResult: LiveData<Boolean> get() = _resetPasswordResult

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> get() = _updateStatus

    /**
     * Calls upon function in AccountRepo to register user
     * Callback is used to set the registration result (livedata)
     */
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
        if(email.isBlank()){
            _resetPasswordResult.value =false
            return
        }
        accountRepository.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            _resetPasswordResult.value = task.isSuccessful
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        accountRepository.deleteAccount(onSuccess, onFailure)
    }

    fun signOutUser(callback: (Boolean) -> Unit){
        accountRepository.signOut{success->
            callback(success)
        }
    }

    fun updateUser(newEmail: String, newUsername: String) {
    viewModelScope.launch {
        val result = accountRepository.updateUserInfo(newEmail, newUsername)
        _updateStatus.postValue(result)
    }
    }

}