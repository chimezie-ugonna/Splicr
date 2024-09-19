@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.splicr.app.R
import com.splicr.app.data.ListItemData
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomListItem
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.customModifier
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.viewModel.HomeViewModel
import com.splicr.app.viewModel.SettingsViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingsScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
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
                        centerStringResource = R.string.settings
                    )

                    val showFaqsBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val showShareYourFeedbackBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val showContactSupportBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }

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

                    val showLoader = rememberSaveable {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(showLoader.value) {
                        if (showLoader.value) {
                            label.intValue = R.string.sending_feedback
                            loaderDescription.intValue =
                                R.string.please_wait_a_moment_as_we_send_your_feedback
                        }
                        showLoaderBottomSheet.value = showLoader.value
                    }

                    CustomBottomSheet(
                        label = R.string.faqs,
                        showBottomSheet = showFaqsBottomSheet,
                        isDarkTheme = isDarkTheme,
                        hasPerformedHapticFeedback = settingsViewModel.hasPerformedFaqsBottomSheetHapticFeedback,
                        navController = navController,
                        viewModel = settingsViewModel
                    )
                    CustomBottomSheet(
                        label = R.string.share_your_feedback,
                        showBottomSheet = showShareYourFeedbackBottomSheet,
                        isDarkTheme = isDarkTheme,
                        commentValue = settingsViewModel.commentValue,
                        snackBarMessageResource = snackBarMessageResource,
                        snackBarMessage = snackBarMessage,
                        snackBarHostState = snackBarHostState,
                        hasPerformedHapticFeedback = settingsViewModel.hasPerformedShareYourFeedbackBottomSheetHapticFeedback,
                        navController = navController,
                        showLoader = showLoader
                    )
                    CustomBottomSheet(
                        label = R.string.contact_support,
                        showBottomSheet = showContactSupportBottomSheet,
                        isDarkTheme = isDarkTheme,
                        hasPerformedHapticFeedback = settingsViewModel.hasPerformedContactSupportBottomSheetHapticFeedback,
                        snackBarMessageResource = snackBarMessageResource,
                        snackBarHostState = snackBarHostState,
                        navController = navController
                    )

                    CustomBottomSheet(
                        label = label.intValue,
                        showBottomSheet = showLoaderBottomSheet,
                        isDarkTheme = isDarkTheme,
                        navController = navController,
                        sheetState = sheetState,
                        scope = scope,
                        loaderDescription = loaderDescription.intValue
                    )

                    val auth = FirebaseAuth.getInstance()
                    val user = remember { mutableStateOf(auth.currentUser) }
                    val emailVerified =
                        remember { mutableStateOf(user.value?.isEmailVerified == true) }
                    val profileListItem = remember { mutableStateOf(listOf<ListItemData>()) }

                    DisposableEffect(Unit) {
                        val authStateListener = FirebaseAuth.AuthStateListener {
                            user.value = it.currentUser
                            emailVerified.value = user.value?.isEmailVerified == true
                            updateProfileListItems(
                                user = user.value, profileListItems = profileListItem
                            )
                        }

                        auth.addAuthStateListener(authStateListener)
                        onDispose {
                            auth.removeAuthStateListener(authStateListener)
                        }
                    }

                    LaunchedEffect(user.value?.uid, emailVerified.value) {
                        if (user.value != null && !emailVerified.value) {
                            while (user.value != null && !emailVerified.value) {
                                delay(2000)
                                user.value?.reload()
                                emailVerified.value = user.value?.isEmailVerified == true
                                if (emailVerified.value) {
                                    updateProfileListItems(
                                        user = user.value, profileListItems = profileListItem
                                    )
                                    break
                                }
                            }
                        } else {
                            this.cancel()
                        }
                    }

                    val today = LocalDate.now()
                    val dateInSevenDays = today.plusDays(7)
                    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                    val dueDate = dateInSevenDays.format(formatter)

                    val subscriptionRenewalText = stringResource(
                        R.string.auto_renewal, dueDate
                    )
                    val subscriptionListItem = remember {
                        mutableStateOf(
                            listOf(
                                ListItemData(
                                    titleResource = R.string.free_trial,
                                    subText = subscriptionRenewalText,
                                    showArrow = false
                                ), ListItemData(
                                    titleResource = R.string.manage_subscription,
                                    isHighlightedText = true
                                )
                            )
                        )
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                modifier = Modifier.padding(top = 28.dp),
                                text = stringResource(R.string.profile),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        itemsIndexed(profileListItem.value) { index, item ->
                            CustomListItem(modifier = if (item.showArrow || item.titleResource == R.string.verify_email || item.titleResource == R.string.sign_out) Modifier
                                .customModifier(
                                    index = index, listSize = profileListItem.value.size
                                )
                                .clickable {
                                    when (item.titleResource) {
                                        R.string.create_an_account_or_sign_in -> {
                                            navController.navigate("SignInScreen")
                                        }

                                        R.string.verify_email -> {
                                            if (user.value != null) {
                                                label.intValue = R.string.sending_link
                                                loaderDescription.intValue =
                                                    R.string.please_wait_a_moment_as_we_send_the_verification_link_to_your_email
                                                showLoaderBottomSheet.value = true
                                                user.value!!
                                                    .sendEmailVerification()
                                                    .addOnCompleteListener { task2 ->
                                                        showLoaderBottomSheet.value = false
                                                        if (task2.isSuccessful) {
                                                            snackBarIsError.value = false
                                                            snackBarMessageResource.intValue =
                                                                R.string.email_verification_link_sent_successfully
                                                            snackBarMessage.value = ""
                                                        } else {
                                                            snackBarIsError.value = true
                                                            snackBarMessageResource.intValue = 0
                                                            snackBarMessage.value =
                                                                task2.exception?.localizedMessage
                                                                    ?: context.getString(R.string.an_unknown_error_occurred)
                                                        }
                                                        scope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                ""
                                                            )
                                                        }
                                                    }
                                            } else {
                                                snackBarIsError.value = true
                                                snackBarMessageResource.intValue =
                                                    R.string.an_unknown_error_occurred
                                                snackBarMessage.value = ""
                                                scope.launch { snackBarHostState.showSnackbar("") }
                                            }

                                        }

                                        else -> {
                                            if (user.value != null) {
                                                auth.signOut()
                                                homeViewModel.stopListening()
                                                scope.launch {
                                                    CredentialManager
                                                        .create(context = context)
                                                        .clearCredentialState(
                                                            ClearCredentialStateRequest()
                                                        )
                                                }
                                            }
                                        }
                                    }
                                } else Modifier.customModifier(
                                index = index, listSize = profileListItem.value.size
                            ),
                                showDivider = profileListItem.value.size > 1 && index != profileListItem.value.size - 1,
                                textStringResource = item.titleResource,
                                textString = item.titleString,
                                subTextString = item.subText,
                                showArrow = item.showArrow,
                                textColor = if (item.isHighlightedText) MaterialTheme.colorScheme.primary else if (item.isSignOutText) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
                        }

                        item {
                            Text(
                                modifier = Modifier.padding(top = 33.dp),
                                text = stringResource(R.string.subscription),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        itemsIndexed(subscriptionListItem.value) { index, item ->
                            CustomListItem(modifier = if (item.showArrow) Modifier
                                .customModifier(
                                    index = index, listSize = subscriptionListItem.value.size
                                )
                                .clickable {
                                    if (item.titleResource == R.string.manage_subscription) {
                                        navController.navigate("ManageSubscriptionScreen")
                                    }
                                } else Modifier.customModifier(
                                index = index, listSize = subscriptionListItem.value.size
                            ),
                                showDivider = subscriptionListItem.value.size > 1 && index != subscriptionListItem.value.size - 1,
                                textStringResource = item.titleResource,
                                textString = item.titleString,
                                subTextString = item.subText,
                                showArrow = item.showArrow,
                                textColor = if (item.isHighlightedText) MaterialTheme.colorScheme.primary else if (item.isSignOutText) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
                        }

                        item {
                            Text(
                                modifier = Modifier.padding(top = 33.dp),
                                text = stringResource(R.string.help_center),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        val listItems = listOf(
                            ListItemData(
                                titleResource = R.string.faqs
                            ), ListItemData(
                                titleResource = R.string.share_your_feedback
                            ), ListItemData(
                                titleResource = R.string.contact_support
                            )
                        )

                        itemsIndexed(listItems) { index, item ->
                            CustomListItem(modifier = if (item.showArrow) Modifier
                                .customModifier(
                                    index = index,
                                    listSize = listItems.size,
                                    bottomPadding = if (index == listItems.size - 1) dimensionResource(
                                        id = R.dimen.spacingXl
                                    ) else 0.dp
                                )
                                .clickable {
                                    when (item.titleResource) {
                                        R.string.faqs -> {
                                            settingsViewModel.resetFAQs()
                                            settingsViewModel.loadFAQs()
                                            showFaqsBottomSheet.value = true
                                        }

                                        R.string.share_your_feedback -> {
                                            settingsViewModel.commentValue.value =
                                                TextFieldValue("")
                                            showShareYourFeedbackBottomSheet.value = true
                                        }

                                        R.string.contact_support -> {
                                            showContactSupportBottomSheet.value = true
                                        }
                                    }
                                } else Modifier.customModifier(
                                index = index,
                                listSize = listItems.size,
                                bottomPadding = if (index == listItems.size - 1) dimensionResource(
                                    id = R.dimen.spacingXl
                                ) else 0.dp
                            ),
                                showDivider = index != listItems.size - 1,
                                textStringResource = item.titleResource,
                                textString = item.titleString,
                                subTextString = item.subText,
                                showArrow = item.showArrow)
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

private fun updateProfileListItems(
    user: FirebaseUser?, profileListItems: MutableState<List<ListItemData>>
) {
    when {
        user == null -> {
            profileListItems.value = listOf(
                ListItemData(
                    titleResource = R.string.create_an_account_or_sign_in, isHighlightedText = true
                )
            )
        }

        !user.isEmailVerified -> {
            profileListItems.value = listOf(
                ListItemData(
                    titleString = user.email ?: "",
                    subText = getSignInProvider(user),
                    showArrow = false
                ), ListItemData(
                    titleResource = R.string.verify_email,
                    isHighlightedText = true,
                    showArrow = false
                ), ListItemData(
                    titleResource = R.string.sign_out, isSignOutText = true, showArrow = false
                )
            )
        }

        else -> {
            profileListItems.value = listOf(
                ListItemData(
                    titleString = user.email ?: "",
                    subText = getSignInProvider(user),
                    showArrow = false
                ), ListItemData(
                    titleResource = R.string.sign_out, isSignOutText = true, showArrow = false
                )
            )
        }
    }
}

fun getSignInProvider(user: FirebaseUser?): String {
    val providerId = user?.providerData?.get(1)?.providerId ?: return "Unknown"
    return when (providerId) {
        "google.com" -> "Google"
        "apple.com" -> "Apple"
        else -> "Email & Password"
    }
}

@Composable
@PreviewLightDark
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}