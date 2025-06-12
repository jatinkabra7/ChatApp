package com.jk.chatapp.presentation.auth_screen

import android.app.Activity

sealed interface AuthActions {
    data class OnPhoneTextFieldValueChange(val value : String) : AuthActions
    data class OnOtpTextFieldValueChange(val value : String) : AuthActions
    data class OnGetOtpClick(val phoneNumber : String, val activity: Activity) : AuthActions
    data class OnContinueClick(val otp : String) : AuthActions
    data object OnEditNumberClick: AuthActions
}