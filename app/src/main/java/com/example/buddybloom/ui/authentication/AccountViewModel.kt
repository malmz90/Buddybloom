package com.example.buddybloom.ui.authentication

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddybloom.data.model.User
import com.example.buddybloom.data.repository.AccountRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel(private val accountRepository: AccountRepository) : ViewModel() {

    private val _loginStatus = MutableLiveData<FirebaseUser?>()
    val loginStatus: LiveData<FirebaseUser?> get() = _loginStatus

    // Livedata for the registration result, updated in registerUser below and observed in RegisterFragment.
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> get() = _registerResult

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    //LiveData to keep result of reset password(true if success, else false)
    private val _resetPasswordResult = MutableLiveData<Boolean>()
    val resetPasswordResult: LiveData<Boolean> get() = _resetPasswordResult

    // LiveData for update Email
    private val _updateEmailStatus = MutableLiveData<Result<Unit>>()
    val updateEmailStatus: LiveData<Result<Unit>> get() = _updateEmailStatus

    // LiveData for updateUserName
    private val _usernameUpdateStatus = MutableLiveData<Result<Unit>>()
    val usernameUpdateStatus: LiveData<Result<Unit>> = _usernameUpdateStatus

    //LiveData for Username and Email
    private val _currentUserData = MutableLiveData<User?>()
    val currentUserData: LiveData<User?> get() = _currentUserData

    private val _isSigningOut = MutableLiveData(false)
    val isSigningOut: LiveData<Boolean> get() = _isSigningOut

    private val _isLoggingIn = MutableLiveData(false)
    val isLoggingIn: LiveData<Boolean> get() = _isLoggingIn

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.getUserData()
                .onSuccess { user ->
                    if (isSigningOut.value != true) {
                        _currentUserData.postValue(user)
                    }
                }
                .onFailure { error ->
                    _errorMessage.postValue(error.message)
                }
        }
    }

    /**
     * Calls upon function in AccountRepo to register user
     * Callback is used to set the registration result (livedata)
     */
    fun registerUser(email: String, password: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.registerUser(email, password, name)
                .onSuccess {
                    _registerResult.postValue(true)
                    loadUserData()
                }
                .onFailure { error ->
                    _registerResult.postValue(false)
                    _errorMessage.postValue(error.message)
                }
        }
    }

    fun loginUser(email: String, password: String) {
        _isLoggingIn.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.loginUser(email, password)
                .onSuccess {
                    _loginResult.postValue(true)
                    loadUserData()
                    _isLoggingIn.postValue(false)
                }
                .onFailure { error ->
                    _errorMessage.postValue(error.message)
                }
        }
    }

    //Sends reset password email if user forgot password, updates reset password to true if the email is sent successfully
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _resetPasswordResult.postValue(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _resetPasswordResult.postValue(true)
                }
                .onFailure { error ->
                    _errorMessage.postValue(error.message)
                }
        }
    }

    //function if user wants to delete their account, calls from accountrepo. OnSuccess if deletion is successful or onFailure if it fails
    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.deleteAccount()
                .onSuccess {
                    _currentUserData.postValue(null)
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.postValue("Failed to delete account: ${error.message}")
                }
        }
    }

    fun signOutUser() {
        _isSigningOut.postValue(true)
        accountRepository.signOut()
        _currentUserData.postValue(null)
    }

    fun updateUserEmail(newEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountRepository.updateUserEmail(newEmail)
                .onSuccess {
                    loadUserData()
                }
            _updateEmailStatus.postValue(result)
        }
    }

    fun updateUserName(newUserName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountRepository.updateUserName(newUserName)
                .onSuccess {
                    loadUserData()
                }
            _usernameUpdateStatus.postValue(result)
        }
    }

    fun getGoogleSignInIntent(): Intent {
        _isLoggingIn.value = true
        return accountRepository.signInGoogleIntent()
    }

    fun authenticateWithGoogle(task: Task<GoogleSignInAccount>, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoggingIn.postValue(true)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: run {
                    Log.e("GoogleSignIn", "Google sign-in account is null")
                    callback(false)
                    return@launch
                }
                accountRepository.firebaseAuthWithGoogle(idToken)
                    .onSuccess {
                        loadUserData()
                        _isLoggingIn.postValue(false)
                        withContext(Dispatchers.Main) { callback(true) }
                    }
                    .onFailure { error ->
                        _errorMessage.postValue(error.message)
                    }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google sign-in failed: ${e.statusCode}", e)
                _isLoggingIn.postValue(false)
                callback(false)
            }
        }
    }
}