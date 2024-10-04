@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.theme.SplicrTheme
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordRequestSentScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavHostController, email: String = ""
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
                        .wrapContentSize()
                        .align(
                            Alignment.Center
                        ), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val firebaseAuth = remember { mutableStateOf(Firebase.auth) }

                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,
                        confirmValueChange = { false })
                    val showLoaderBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }

                    CustomBottomSheet(
                        label = R.string.resending_password_reset_link,
                        showBottomSheet = showLoaderBottomSheet,
                        isDarkTheme = isDarkTheme,
                        navController = navController,
                        sheetState = sheetState,
                        scope = scope,
                        loaderDescription = R.string.please_wait_a_moment_as_we_resend_the_password_reset_link_to_your_email
                    )

                    Image(
                        modifier = Modifier
                            .width(150.dp)
                            .height(93.67.dp),
                        painter = painterResource(id = R.drawable.sent_mail),
                        contentDescription = null
                    )

                    Text(
                        text = stringResource(R.string.please_check_your_inbox_and_make_sure_its_you_then_reset_your_password),
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )

                    val annotatedText = buildAnnotatedString {
                        append(stringResource(R.string.didn_t_receive_the_mail))

                        pushStringAnnotation(
                            tag = "resend", annotation = stringResource(R.string.resend_mail)
                        )
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(" ${stringResource(R.string.resend_mail)}")
                        }
                        pop()
                    }

                    ClickableText(text = annotatedText,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingSm)),
                        style = TextStyle(
                            fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "resend", start = offset, end = offset
                            ).firstOrNull()?.let { _ ->
                                showLoaderBottomSheet.value = true
                                firebaseAuth.value.sendPasswordResetEmail(email)
                                    .addOnCompleteListener { task ->
                                        showLoaderBottomSheet.value = false
                                        if (task.isSuccessful) {
                                            snackBarIsError.value = false
                                            snackBarMessageResource.intValue =
                                                R.string.password_reset_link_sent_successfully
                                        } else {
                                            snackBarIsError.value = true
                                            snackBarMessageResource.intValue = 0
                                            snackBarMessage.value = task.exception?.localizedMessage
                                                ?: context.getString(
                                                    R.string.an_unexpected_error_occurred
                                                )
                                        }
                                        scope.launch { snackBarHostState.showSnackbar("") }
                                    }
                            }
                        })
                }

                CustomTopNavigationBar(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(
                        vertical = 72.dp
                    ),
                    startImageResource = R.drawable.back,
                    startStringResource = R.string.go_back,
                    startOnClick = {
                        navController.popBackStack(
                            route = "SignInScreen", inclusive = false
                        )
                    })

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
fun ResetPasswordRequestSentScreenPreview() {
    ResetPasswordRequestSentScreen(navController = rememberNavController())
}