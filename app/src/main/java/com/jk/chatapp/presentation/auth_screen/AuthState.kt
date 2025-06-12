package com.jk.chatapp.presentation.auth_screen

data class AuthState(
    val phoneTextFieldValue : String = "",
    val otpTextFieldValue : String = "",
    val isOtpTextFieldVisible : Boolean = false,
    val isGetOtpButtonVisible : Boolean = true,
    val isContinueButtonVisible : Boolean = false,
    val isOtpVerified : Boolean = false,
    val isPhoneTextFieldEnabled : Boolean = true,
    val verificationId : String? = null
)
