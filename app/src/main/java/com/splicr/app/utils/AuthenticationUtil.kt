package com.splicr.app.utils

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.splicr.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Locale

object AuthenticationUtil {
    fun authenticateWithGoogle(
        context: Context,
        webClientId: String,
        setFilterByAuthorizedAccounts: Boolean,
        scope: CoroutineScope,
        firebaseAuth: FirebaseAuth,
        onAuthenticationFailure: (errorMessage: String, errorMessageResource: Int) -> Unit,
        onAuthenticationSuccess: () -> Unit
    ) {
        val nonceBytes = ByteArray(16)
        SecureRandom().nextBytes(nonceBytes)
        val nonce = Base64.encodeToString(
            nonceBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        val googleIdOption =
            GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(setFilterByAuthorizedAccounts)
                .setServerClientId(webClientId).setAutoSelectEnabled(true).setNonce(nonce).build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = CredentialManager.create(context).getCredential(
                    request = request,
                    context = context,
                )
                handleGoogleAuthentication(
                    context = context,
                    result = result,
                    firebaseAuth = firebaseAuth,
                    onAuthenticationFailure = onAuthenticationFailure,
                    onAuthenticationSuccess = onAuthenticationSuccess
                )
            } catch (e: GetCredentialException) {
                if (e.type == android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL && setFilterByAuthorizedAccounts) {
                    authenticateWithGoogle(
                        context = context,
                        webClientId = webClientId,
                        setFilterByAuthorizedAccounts = false,
                        scope = scope,
                        firebaseAuth = firebaseAuth,
                        onAuthenticationFailure = onAuthenticationFailure,
                        onAuthenticationSuccess = onAuthenticationSuccess
                    )
                } else {
                    Log.e(
                        "AuthenticationErrorMessage",
                        e.localizedMessage?.toString()
                            ?: context.getString(R.string.an_unexpected_error_occurred)
                    )
                    onAuthenticationFailure(
                        e.localizedMessage?.toString()
                            ?: context.getString(R.string.an_unexpected_error_occurred), -1
                    )
                }
            } catch (e: Exception) {
                onAuthenticationFailure(
                    e.localizedMessage?.toString()
                        ?: context.getString(R.string.an_unexpected_error_occurred), 0
                )
            }
        }
    }

    private fun handleGoogleAuthentication(
        context: Context,
        result: GetCredentialResponse,
        firebaseAuth: FirebaseAuth,
        onAuthenticationFailure: (errorMessage: String, errorMessageResource: Int) -> Unit,
        onAuthenticationSuccess: () -> Unit
    ) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        firebaseAuth.signInWithCredential(
                            GoogleAuthProvider.getCredential(
                                GoogleIdTokenCredential.createFrom(
                                    credential.data
                                ).idToken, null
                            )
                        ).addOnCompleteListener(context as Activity) { task ->
                            if (task.isSuccessful) {
                                onAuthenticationSuccess()
                            } else {
                                onAuthenticationFailure(
                                    task.exception?.localizedMessage?.toString()
                                        ?: context.getString(R.string.an_unexpected_error_occurred), 0
                                )
                            }
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        onAuthenticationFailure(
                            e.localizedMessage?.toString() ?: context.getString(
                                R.string.an_unexpected_error_occurred
                            ), 0
                        )
                    }
                } else {
                    onAuthenticationFailure("", R.string.unexpected_type_of_credential)
                }
            }

            else -> {
                onAuthenticationFailure("", R.string.unexpected_type_of_credential)
            }
        }
    }

    fun authenticateWithApple(
        context: Context,
        firebaseAuth: FirebaseAuth,
        onAuthenticationFailure: (errorMessage: String, errorMessageResource: Int) -> Unit,
        onAuthenticationSuccess: () -> Unit
    ) {
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = arrayOf("email", "name").toMutableList()
        provider.addCustomParameter("locale", Locale.getDefault().language)
        val pending = firebaseAuth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener {
                onAuthenticationSuccess()
            }.addOnFailureListener {
                onAuthenticationFailure(
                    it.localizedMessage?.toString()
                        ?: context.getString(R.string.an_unexpected_error_occurred), 0
                )
            }
        } else {
            firebaseAuth.startActivityForSignInWithProvider(
                context as Activity, provider.build()
            ).addOnSuccessListener {
                onAuthenticationSuccess()
            }.addOnFailureListener {
                onAuthenticationFailure(
                    it.localizedMessage?.toString()
                        ?: context.getString(R.string.an_unexpected_error_occurred), 0
                )
            }
        }
    }
}