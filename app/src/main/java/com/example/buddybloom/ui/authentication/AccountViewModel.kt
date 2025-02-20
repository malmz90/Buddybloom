package com.example.buddybloom.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.repository.AccountRepository
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val accountRepository = AccountRepository()

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult : LiveData<Boolean> get() = _registerResult

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    //LiveData to keep result of reset password(true if success, else false)
    private val _resetPasswordResult = MutableLiveData<Boolean>()
    val resetPasswordResult: LiveData<Boolean> get() = _resetPasswordResult

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> get() = _updateStatus

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

    //Sends reset password email if user forgot password, updates reset password to true if the email is sent successfully
    fun sendPasswordResetEmail(email: String){
        if(email.isBlank()){
            _resetPasswordResult.value =false
            return
        }
        accountRepository.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            _resetPasswordResult.value = task.isSuccessful
        }
    }

    //function if user wants to delete their account, calls from accountrepo. OnSuccess if deletion is successful or onFailure if it fails
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