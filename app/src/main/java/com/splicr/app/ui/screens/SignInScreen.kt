@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTextField
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.components.SecondaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.AuthenticationUtil
import com.splicr.app.viewModel.SignInViewModel
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavHostController, signInViewModel: SignInViewModel = viewModel()
) {
    SplicrTheme(darkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(
                            id = R.dimen.spacingXl
                        ), end = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    )
            ) {
                val context = LocalContext.current
                val snackBarHostState = remember {
                    SnackbarHostState()
                }
                val scope = rememberCoroutineScope()
                val snackBarMessageResource = remember {
                    mutableIntStateOf(0)
                }
                val snackBarMessage = remember {
                    mutableStateOf("")
                }
                val snackBarIsError = remember {
                    mutableStateOf(true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 72.dp
                        )
                ) {
                    CustomTopNavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        startImageResource = R.drawable.back,
                        startStringResource = R.string.go_back,
                        startOnClick = { navController.popBackStack() },
                        centerStringResource = R.string.create_an_account_or_sign_in
                    )
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,
                        confirmValueChange = { false })
                    val showLoaderBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val label = rememberSaveable {
                        mutableIntStateOf(0)
                    }
                    val loaderDescription = rememberSaveable {
                        mutableIntStateOf(0)
                    }

                    CustomBottomSheet(
                        label = label.intValue,
                        showBottomSheet = showLoaderBottomSheet,
                        isDarkTheme = isDarkTheme,
                        navController = navController,
                        sheetState = sheetState,
                        scope = scope,
                        loaderDescription = loaderDescription.intValue
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val firebaseAuth = remember { mutableStateOf(Firebase.auth) }
                        val webClientId = stringResource(id = R.string.web_client_id)

                        val emailFocusRequester = remember {
                            FocusRequester()
                        }

                        val passwordFocusRequester = remember {
                            FocusRequester()
                        }

                        val allTextFieldsRequirementsMet = remember { mutableStateOf(false) }

                        allTextFieldsRequirementsMet.value = Patterns.EMAIL_ADDRESS.matcher(
                            signInViewModel.emailValue.value.text.trim()
                        ).matches() && signInViewModel.passwordValue.value.text.length >= 8

                        Text(
                            modifier = Modifier.padding(top = 44.dp),
                            text = stringResource(R.string.provide_your_credentials_below),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )

                        CustomTextField(
                            modifier = Modifier.padding(
                                top = dimensionResource(
                                    id = R.dimen.spacingXl
                                )
                            ),
                            placeHolderResource = R.string.enter_your_email,
                            focusRequester = emailFocusRequester,
                            value = signInViewModel.emailValue,
                            leadingImageResource = R.drawable.mail,
                            leadingErrorImageResource = R.drawable.error_mail,
                            errorMessageResource = signInViewModel.emailErrorMessageResource,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                capitalization = KeyboardCapitalization.None,
                                imeAction = ImeAction.Next
                            )
                        )

                        CustomTextField(
                            modifier = Modifier.padding(
                                top = dimensionResource(
                                    id = R.dimen.spacingMd
                                )
                            ),
                            placeHolderResource = R.string.enter_your_password_min_8_chars,
                            focusRequester = passwordFocusRequester,
                            value = signInViewModel.passwordValue,
                            leadingImageResource = R.drawable.lock,
                            leadingErrorImageResource = R.drawable.error_lock,
                            isPasswordField = true,
                            errorMessageResource = signInViewModel.passwordErrorMessageResource,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                capitalization = KeyboardCapitalization.None,
                                imeAction = ImeAction.Done
                            )
                        )

                        PrimaryButton(
                            modifier = Modifier.padding(
                                top = dimensionResource(id = R.dimen.spacingMd)
                            ), textResource = R.string.Continue
                        ) {
                            if (allTextFieldsRequirementsMet.value) {
                                val auth = FirebaseAuth.getInstance()
                                label.intValue = R.string.signing_in
                                loaderDescription.intValue =
                                    R.string.completing_your_sign_in_thank_you_for_your_patience
                                showLoaderBottomSheet.value = true
                                auth.signInWithEmailAndPassword(
                                    signInViewModel.emailValue.value.text.trim(),
                                    signInViewModel.passwordValue.value.text
                                ).addOnCompleteListener { signInTask ->
                                    if (signInTask.isSuccessful) {
                                        showLoaderBottomSheet.value = false
                                        navController.popBackStack()
                                    } else {
                                        label.intValue = R.string.creating_account
                                        loaderDescription.intValue =
                                            R.string.registering_your_details_please_hold_on_while_we_create_your_account
                                        auth.createUserWithEmailAndPassword(
                                            signInViewModel.emailValue.value.text.trim(),
                                            signInViewModel.passwordValue.value.text
                                        ).addOnCompleteListener { createUserTask ->
                                            showLoaderBottomSheet.value = false
                                            if (createUserTask.isSuccessful) {
                                                navController.popBackStack()
                                            } else {
                                                snackBarIsError.value = true
                                                snackBarMessageResource.intValue = 0
                                                snackBarMessage.value =
                                                    createUserTask.exception?.localizedMessage
                                                        ?: context.getString(R.string.an_unexpected_error_occurred)
                                                scope.launch {
                                                    snackBarHostState.showSnackbar(
                                                        ""
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (!Patterns.EMAIL_ADDRESS.matcher(
                                        signInViewModel.emailValue.value.text.trim()
                                    ).matches()
                                ) {
                                    signInViewModel.emailErrorMessageResource.intValue =
                                        R.string.please_enter_a_valid_email_address
                                    emailFocusRequester.requestFocus()
                                }
                                if (signInViewModel.passwordValue.value.text.length < 8) {
                                    signInViewModel.passwordErrorMessageResource.intValue =
                                        R.string.please_enter_a_minimum_of_8_characters
                                    if (signInViewModel.emailErrorMessageResource.intValue == 0) {
                                        passwordFocusRequester.requestFocus()
                                    }
                                }
                            }
                        }

                        Text(text = stringResource(R.string.forgot_your_password),
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(top = dimensionResource(id = R.dimen.spacingMd))
                                .clickable(interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null) {
                                    navController.navigate("ResetPasswordScreen")
                                },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Normal
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(id = R.dimen.spacingMd)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 0.9.dp,
                                color = MaterialTheme.colorScheme.surface
                            )

                            Text(
                                modifier = Modifier.padding(horizontal = 13.dp),
                                text = stringResource(R.string.or),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )

                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 0.9.dp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }

                        SecondaryButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = dimensionResource(id = R.dimen.spacingMd),
                                    bottom = dimensionResource(id = R.dimen.spacingXl)
                                ),
                            textResource = R.string.sign_in_with_google,
                            leadingImageResource = R.drawable.google_icon
                        ) {
                            label.intValue = R.string.signing_in
                            loaderDescription.intValue =
                                R.string.completing_your_sign_in_thank_you_for_your_patience
                            showLoaderBottomSheet.value = true
                            AuthenticationUtil.authenticateWithGoogle(context = context,
                                webClientId = webClientId,
                                setFilterByAuthorizedAccounts = true,
                                scope = scope,
                                firebaseAuth = firebaseAuth.value,
                                onAuthenticationFailure = { em, emr ->
                                    showLoaderBottomSheet.value = false
                                    if (emr != -1) {
                                        snackBarIsError.value = true
                                        snackBarMessageResource.intValue = emr
                                        snackBarMessage.value = em
                                        scope.launch { snackBarHostState.showSnackbar("") }
                                    }
                                }) {
                                showLoaderBottomSheet.value = false
                                navController.popBackStack()
                            }
                        }

                        /*SecondaryButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = dimensionResource(id = R.dimen.spacingMd),
                                    bottom = dimensionResource(id = R.dimen.spacingXl)
                                ),
                            textResource = R.string.sign_in_with_apple,
                            leadingImageResource = R.drawable.apple_icon
                        ) {

                        }*/
                    }
                }

                CustomSnackBar(
                    messageResource = snackBarMessageResource.intValue,
                    isError = snackBarIsError.value,
                    message = snackBarMessage.value,
                    snackBarHostState = snackBarHostState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

}

@Composable
@PreviewLightDark
fun SignInScreenPreview() {
    SignInScreen(navController = rememberNavController())
}