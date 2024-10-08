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
import androidx.compose.runtime.livedata.observeAsState
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
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.data.ListItemData
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomListItem
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.customModifier
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.FirestoreQueryUtil.deleteUserDataAndAccount
import com.splicr.app.viewModel.HomeViewModel
import com.splicr.app.viewModel.SettingsViewModel
import com.splicr.app.viewModel.SubscriptionStatus
import com.splicr.app.viewModel.SubscriptionViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
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
                    val showReAuthenticateAccountBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }

                    val sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
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

                    val subscriptionStatus =
                        subscriptionViewModel.subscriptionStatus.observeAsState(initial = SubscriptionStatus.NONE)
                    val subscriptionRenewalDate =
                        subscriptionViewModel.renewalDate.observeAsState(initial = null)
                    val subscriptionExpiryDate =
                        subscriptionViewModel.expiryDate.observeAsState(initial = null)

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
                        label = R.string.re_authenticate_account,
                        showBottomSheet = showReAuthenticateAccountBottomSheet,
                        isDarkTheme = isDarkTheme,
                        hasPerformedHapticFeedback = settingsViewModel.hasPerformedReAuthenticateAccountBottomSheetHapticFeedback,
                        navController = navController
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

                    val auth = Firebase.auth
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

                    val accountIsReadyToBeDeleted = rememberSaveable { mutableStateOf(false) }

                    val reAuthenticated =
                        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                            "reAuthenticated"
                        )?.observeAsState()

                    reAuthenticated?.value?.let {
                        accountIsReadyToBeDeleted.value = it
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "reAuthenticated", false
                        )
                    }

                    LaunchedEffect(accountIsReadyToBeDeleted.value) {
                        if (accountIsReadyToBeDeleted.value) {
                            label.intValue = R.string.deleting_account
                            if (reAuthenticated?.value == true) {
                                loaderDescription.intValue =
                                    R.string.completing_your_account_deletion_process_this_may_take_a_moment
                            } else {
                                loaderDescription.intValue =
                                    R.string.deleting_your_account_this_may_take_a_moment
                            }
                            showLoaderBottomSheet.value = true
                            scope.launch {
                                deleteUserDataAndAccount(context = context).onSuccess {
                                    CredentialManager.create(context = context)
                                        .clearCredentialState(
                                            ClearCredentialStateRequest()
                                        )
                                    auth.signOut()
                                    homeViewModel.stopListening()
                                    showLoaderBottomSheet.value = false
                                    snackBarIsError.value = false
                                    snackBarMessageResource.intValue =
                                        R.string.account_deleted_successfully
                                    snackBarMessage.value = ""
                                    snackBarHostState.showSnackbar(
                                        ""
                                    )
                                    accountIsReadyToBeDeleted.value = false
                                }.onFailure {
                                    showLoaderBottomSheet.value = false
                                    if (it is FirebaseAuthRecentLoginRequiredException) {
                                        showReAuthenticateAccountBottomSheet.value = true
                                    } else {
                                        snackBarIsError.value = true
                                        snackBarMessageResource.intValue = 0
                                        snackBarMessage.value =
                                            it.localizedMessage ?: context.getString(
                                                R.string.an_unexpected_error_occurred
                                            )
                                        snackBarHostState.showSnackbar(
                                            message = ""
                                        )
                                    }
                                    accountIsReadyToBeDeleted.value = false
                                }
                            }
                        }
                    }

                    val subscriptionStatusDisplayStringResource =
                        rememberSaveable { mutableIntStateOf(0) }


                    subscriptionStatusDisplayStringResource.intValue =
                        when (subscriptionStatus.value) {
                            SubscriptionStatus.NONE -> {
                                R.string.no_active_subscription
                            }

                            SubscriptionStatus.MONTHLY_SUBSCRIPTION -> {
                                R.string.monthly_premium
                            }

                            SubscriptionStatus.YEARLY_SUBSCRIPTION -> {
                                R.string.yearly_premium
                            }

                            else -> {
                                R.string.free_trial
                            }
                        }

                    val subscriptionListItem = remember {
                        mutableStateOf(
                            listOf(
                                ListItemData(
                                    titleResource = subscriptionStatusDisplayStringResource.intValue,
                                    subText = if (subscriptionRenewalDate.value != null) {
                                        context.getString(
                                            R.string.auto_renewal, subscriptionRenewalDate.value
                                        )
                                    } else if (subscriptionExpiryDate.value != null) {
                                        context.getString(
                                            R.string.expiring, subscriptionExpiryDate.value
                                        )
                                    } else {
                                        null
                                    },
                                    showArrow = false
                                ), ListItemData(
                                    titleResource = R.string.manage_subscription,
                                    isPrimaryColoredText = true
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
                            CustomListItem(modifier = if (item.showArrow || item.titleResource == R.string.verify_email || item.titleResource == R.string.sign_out || item.titleResource == R.string.delete_account) Modifier
                                .customModifier(
                                    index = index, listSize = profileListItem.value.size
                                )
                                .clickable {
                                    when (item.titleResource) {
                                        R.string.create_an_account_or_sign_in -> {
                                            navController.navigate("SignInScreen/signIn")
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
                                                                    ?: context.getString(R.string.an_unexpected_error_occurred)
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
                                                    R.string.no_account_signed_in
                                                snackBarMessage.value = ""
                                                scope.launch { snackBarHostState.showSnackbar("") }
                                            }

                                        }

                                        R.string.sign_out -> {
                                            if (user.value != null) {
                                                scope.launch {
                                                    CredentialManager
                                                        .create(context = context)
                                                        .clearCredentialState(
                                                            ClearCredentialStateRequest()
                                                        )
                                                }
                                                auth.signOut()
                                                homeViewModel.stopListening()
                                            } else {
                                                snackBarIsError.value = true
                                                snackBarMessageResource.intValue =
                                                    R.string.no_account_signed_in
                                                snackBarMessage.value = ""
                                                scope.launch { snackBarHostState.showSnackbar("") }
                                            }
                                        }

                                        else -> {
                                            if (user.value != null) {
                                                accountIsReadyToBeDeleted.value = true
                                            } else {
                                                snackBarIsError.value = true
                                                snackBarMessageResource.intValue =
                                                    R.string.no_account_signed_in
                                                snackBarMessage.value = ""
                                                scope.launch { snackBarHostState.showSnackbar("") }
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
                                textColor = if (item.isPrimaryColoredText) MaterialTheme.colorScheme.primary else if (item.isErrorColoredText) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
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
                                textColor = if (item.isPrimaryColoredText) MaterialTheme.colorScheme.primary else if (item.isErrorColoredText) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground)
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
                    titleResource = R.string.create_an_account_or_sign_in,
                    isPrimaryColoredText = true
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
                    isPrimaryColoredText = true,
                    showArrow = false
                ), ListItemData(
                    titleResource = R.string.sign_out, isErrorColoredText = true, showArrow = false
                ), ListItemData(
                    titleResource = R.string.delete_account,
                    isErrorColoredText = true,
                    showArrow = false
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
                    titleResource = R.string.sign_out, isErrorColoredText = true, showArrow = false
                ), ListItemData(
                    titleResource = R.string.delete_account,
                    isErrorColoredText = true,
                    showArrow = false
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