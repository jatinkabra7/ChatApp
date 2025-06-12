package com.jk.chatapp.presentation.auth_screen

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jk.chatapp.domain.ChatRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AuthViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _event = Channel<AuthEvents>()
    val event = _event.receiveAsFlow()

    private fun insertUser(phoneNumber: String) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                chatRepository.insertUser("+91".plus(phoneNumber))
            }
        }
    }

    private fun sendOtp(phoneNumber: String, activity: Activity) {

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object :
                PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Firebase.auth.signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                _event.trySend(AuthEvents.ShowToast("Sent"))
                            }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _event.trySend(AuthEvents.ShowToast("${e.message}"))
                }

                override fun onCodeSent(
                    p0: String,
                    p1: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(p0, p1)
                    _state.update { it.copy(verificationId = p0) }
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(vid : String, otp : String) : Task<AuthResult?> {
        val credential = PhoneAuthProvider.getCredential(
            vid,
            otp
        )

        return Firebase.auth.signInWithCredential(credential)
    }

    fun onAction(action : AuthActions) {

        viewModelScope.launch {

            when(action) {
                is AuthActions.OnGetOtpClick -> {
                    _state.update {
                        it.copy(
                            isOtpTextFieldVisible = true,
                            isGetOtpButtonVisible = false,
                            isContinueButtonVisible = true,
                            otpTextFieldValue = "",
                            isPhoneTextFieldEnabled = false
                        )
                    }

                    sendOtp(action.phoneNumber, action.activity)
                }

                is AuthActions.OnContinueClick -> {

                    val vid = state.value.verificationId

                    if(vid != null) {

                        val result = verifyOtp(vid,action.otp)

                        result.addOnCompleteListener {
                            if(it.isSuccessful) {
                                _event.trySend(AuthEvents.ShowToast("Logged In"))

                                _state.update { it.copy(isOtpVerified = true) }

                                insertUser(state.value.phoneTextFieldValue)
                            }
                            else _event.trySend(AuthEvents.ShowToast("Wrong OTP"))
                        }
                    }
                }

                is AuthActions.OnOtpTextFieldValueChange -> {

                    val newValue = action.value

                    if(newValue.all { it.isDigit() } && newValue.length <= 6) {
                        _state.update { it.copy(otpTextFieldValue = newValue) }
                    }
                }
                is AuthActions.OnPhoneTextFieldValueChange -> {

                    val newValue = action.value

                    if(newValue.all { it.isDigit() } && newValue.length <= 10) {
                        _state.update { it.copy(phoneTextFieldValue = newValue) }
                    }
                }
                AuthActions.OnEditNumberClick -> {
                    _state.update {
                        it.copy(
                            isOtpTextFieldVisible = false,
                            isContinueButtonVisible = false,
                            isGetOtpButtonVisible = true,
                            isPhoneTextFieldEnabled = true
                        )
                    }
                }
            }
        }
    }


}