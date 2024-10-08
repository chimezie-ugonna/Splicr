@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTextField
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.viewModel.ResetPasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    action: String = "",
    resetPasswordViewModel: ResetPasswordViewModel = viewModel()
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
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
                        centerStringResource = R.string.reset_password
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val firebaseAuth = remember { mutableStateOf(Firebase.auth) }
                        val emailFocusRequester = remember {
                            FocusRequester()
                        }

                        val sheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true,
                            confirmValueChange = { false })
                        val showLoaderBottomSheet = rememberSaveable {
                            mutableStateOf(false)
                        }

                        CustomBottomSheet(
                            label = R.string.sending_password_reset_link,
                            showBottomSheet = showLoaderBottomSheet,
                            isDarkTheme = isDarkTheme,
                            navController = navController,
                            sheetState = sheetState,
                            scope = scope,
                            loaderDescription = R.string.please_wait_a_moment_as_we_send_a_password_reset_link_to_your_email
                        )

                        Text(
                            modifier = Modifier.padding(top = 44.dp),
                            text = stringResource(R.string.enter_your_email_address_to_reset_your_password),
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
                            value = resetPasswordViewModel.emailValue,
                            leadingImageResource = R.drawable.mail,
                            leadingErrorImageResource = R.drawable.error_mail,
                            errorMessageResource = resetPasswordViewModel.emailErrorMessageResource,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                capitalization = KeyboardCapitalization.None,
                                imeAction = ImeAction.Done
                            )
                        )

                        PrimaryButton(
                            modifier = Modifier.padding(
                                top = dimensionResource(id = R.dimen.spacingMd),
                                bottom = dimensionResource(
                                    id = R.dimen.spacingXl
                                )
                            ), textResource = R.string.Continue
                        ) {
                            if (Patterns.EMAIL_ADDRESS.matcher(
                                    resetPasswordViewModel.emailValue.value.text.trim()
                                ).matches()
                            ) {
                                showLoaderBottomSheet.value = true
                                firebaseAuth.value.sendPasswordResetEmail(resetPasswordViewModel.emailValue.value.text.trim())
                                    .addOnCompleteListener { task ->
                                        showLoaderBottomSheet.value = false
                                        if (task.isSuccessful) {
                                            navController.navigate(route = "ResetPasswordRequestSentScreen/${resetPasswordViewModel.emailValue.value.text.trim()}/$action")
                                        } else {
                                            snackBarIsError.value = true
                                            snackBarMessageResource.intValue = 0
                                            snackBarMessage.value = task.exception?.localizedMessage
                                                ?: context.getString(
                                                    R.string.an_unexpected_error_occurred
                                                )
                                            scope.launch { snackBarHostState.showSnackbar("") }
                                        }
                                    }
                            } else {
                                resetPasswordViewModel.emailErrorMessageResource.intValue =
                                    R.string.please_enter_a_valid_email_address
                                emailFocusRequester.requestFocus()
                            }
                        }
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
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(navController = rememberNavController())
}