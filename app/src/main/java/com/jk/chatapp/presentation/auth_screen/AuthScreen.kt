package com.jk.chatapp.presentation.auth_screen

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    state: AuthState,
    event : Flow<AuthEvents>,
    onAction : (AuthActions) -> Unit,
    navigateToHome : () -> Unit
) {


    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    LaunchedEffect(event) {
        event.collect {
            when(it) {
                is AuthEvents.ShowToast -> {
                    Toast.makeText(context,it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(state.isOtpVerified) {
        if(state.isOtpVerified == true) navigateToHome()
    }

    val firebaseAppCheck = FirebaseAppCheck.getInstance()
    firebaseAppCheck.installAppCheckProviderFactory(
        PlayIntegrityAppCheckProviderFactory.getInstance()
    )

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
        ) {

            Spacer(Modifier.fillMaxHeight(0.3f))

            Text(
                text = "Welcome to Chat App",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(50.dp))

            PhoneTextField(
                value = state.phoneTextFieldValue,
                onValueChange = {onAction(AuthActions.OnPhoneTextFieldValueChange(it))},
                isEnabled = state.isPhoneTextFieldEnabled
            )

            AnimatedVisibility(
                state.isOtpTextFieldVisible
            ) {

                Column {

                    Spacer(Modifier.height(20.dp))

                    OtpTextField(
                        value = state.otpTextFieldValue,
                        onValueChange = {onAction(AuthActions.OnOtpTextFieldValueChange(it))}
                    )
                }
            }

            Spacer(Modifier.height(50.dp))

            // get otp button

            AnimatedVisibility(
                state.isGetOtpButtonVisible
            ) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20))
                        .clickable {
                            focusManager.clearFocus()

                            if(state.phoneTextFieldValue.length == 10) {

                                onAction(AuthActions.OnGetOtpClick("+91"+state.phoneTextFieldValue, context as Activity))
                            }
                            else {
                                Toast.makeText(context,"Wrong Number", Toast.LENGTH_SHORT).show()
                            }

                        }
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Get OTP",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // continue button
            AnimatedVisibility(state.isContinueButtonVisible) {
                Column {

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20))
                            .clickable {
                                focusManager.clearFocus()

                                if(state.otpTextFieldValue.length == 6) {

                                    onAction(AuthActions.OnContinueClick(otp = state.otpTextFieldValue))
                                }

                            }
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    TextButton(onClick = {onAction(AuthActions.OnEditNumberClick)}) {
                        Text(text = "Edit Number", textAlign = TextAlign.Center)
                    }
                }
            }

        }
    }
}

@Composable
fun PhoneTextField(
    modifier: Modifier = Modifier,
    value : String,
    onValueChange : (String) -> Unit,
    isEnabled : Boolean
) {
    OutlinedTextField(
        enabled = isEnabled,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Spacer(Modifier.widthIn(10.dp))

                Text(
                    text = "+91",
                    fontSize = 20.sp
                )

                Spacer(Modifier.widthIn(10.dp))

                VerticalDivider(Modifier.height(30.dp))
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground
        ),
        textStyle = TextStyle(
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        modifier = Modifier
            .width(200.dp)
    )
}

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    value : String,
    onValueChange : (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Spacer(Modifier.widthIn(10.dp))

                Text(
                    text = "OTP",
                    fontSize = 20.sp
                )

                Spacer(Modifier.widthIn(10.dp))

                VerticalDivider(Modifier.height(30.dp))
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground
        ),
        textStyle = TextStyle(
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        modifier = Modifier
            .width(200.dp)
    )
}