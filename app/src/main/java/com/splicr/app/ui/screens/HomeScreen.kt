@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CanvasListItem
import com.splicr.app.ui.components.CanvasListLoadingItem
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.viewModel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    purchasesRestored: Boolean = false
) {
    SplicrTheme(darkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current

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
                    CustomTopNavigationBar(modifier = Modifier.fillMaxWidth(),
                        startComposable = { AppNameText(modifier = Modifier.align(Alignment.CenterStart)) },
                        endImageResource = R.drawable.settings,
                        endStringResource = R.string.settings,
                        endOnClick = {
                            navController.navigate("SettingsScreen")
                        })

                    val hasShownSnackbar = rememberSaveable { mutableStateOf(false) }
                    LaunchedEffect(purchasesRestored) {
                        if (purchasesRestored && !hasShownSnackbar.value) {
                            snackBarIsError.value = false
                            snackBarMessageResource.intValue = R.string.purchase_restored_successfully
                            snackBarMessage.value = ""
                            scope.launch {
                                snackBarHostState.showSnackbar("")
                            }
                            hasShownSnackbar.value = true
                        }
                    }

                    Text(
                        text = stringResource(R.string.my_canvas),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 28.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    LaunchedEffect(homeViewModel.showError) {
                        if (homeViewModel.showError) {
                            snackBarIsError.value = true
                            snackBarMessageResource.intValue = 0
                            snackBarMessage.value = homeViewModel.errorMessage
                            scope.launch { snackBarHostState.showSnackbar("") }
                            homeViewModel.showError(status = false)
                        }
                    }

                    val showLoaderBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }

                    showLoaderBottomSheet.value = homeViewModel.isDeleting

                    val sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
                        confirmValueChange = { false })

                    CustomBottomSheet(
                        label = R.string.deleting_item,
                        showBottomSheet = showLoaderBottomSheet,
                        isDarkTheme = isDarkTheme,
                        navController = navController,
                        sheetState = sheetState,
                        scope = scope,
                        loaderDescription = R.string.deleting_your_item_this_may_take_a_moment
                    )

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isUserLoggedIn = rememberSaveable { mutableStateOf(false) }
                        val loggedInUserId = rememberSaveable { mutableStateOf("") }

                        // Trigger the check whenever the composable enters the composition
                        DisposableEffect(Unit) {
                            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                                val newUserLoggedIn = firebaseAuth.currentUser != null
                                val newLoggedInUserId = firebaseAuth.currentUser?.uid ?: ""
                                if (newUserLoggedIn && (!isUserLoggedIn.value || newLoggedInUserId != loggedInUserId.value)) {
                                    // User just logged in
                                    isUserLoggedIn.value = true
                                    loggedInUserId.value = newLoggedInUserId
                                    homeViewModel.resetItems()
                                    homeViewModel.loadItems()
                                } else if (!newUserLoggedIn && isUserLoggedIn.value) {
                                    // User just logged out
                                    isUserLoggedIn.value = false
                                    loggedInUserId.value = ""
                                }
                            }
                            Firebase.auth.addAuthStateListener(authStateListener)

                            // Cleanup (if necessary) when the composable leaves the composition
                            onDispose {
                                Firebase.auth.removeAuthStateListener(authStateListener)
                            }
                        }

                        if (!isUserLoggedIn.value) {
                            EmptyOrNotLoggedInUi(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Center),
                                navController = navController,
                                isUserLoggedIn = isUserLoggedIn
                            )
                        } else {
                            if (homeViewModel.isLoadingMore && homeViewModel.canvasItems.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = dimensionResource(id = R.dimen.spacingXs))
                                        .verticalScroll(state = rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(
                                        space = dimensionResource(
                                            id = R.dimen.spacingSm
                                        )
                                    )
                                ) {
                                    val count = 10
                                    repeat(count) { index ->
                                        CanvasListLoadingItem(
                                            index = index,
                                            count = count,
                                            homeViewModel = homeViewModel
                                        )
                                    }
                                }
                            } else if (homeViewModel.canvasItems.isNotEmpty()) {
                                val listState = rememberLazyListState()

                                LaunchedEffect(listState) {
                                    snapshotFlow { listState.layoutInfo }.collect { layoutInfo ->
                                        val firstVisibleIndex = listState.firstVisibleItemIndex
                                        val visibleItemCount = layoutInfo.visibleItemsInfo.size
                                        val totalItemCount = layoutInfo.totalItemsCount

                                        // Check if the LazyColumn is scrollable and scrolled to the last item
                                        if (listState.isScrollInProgress && firstVisibleIndex + visibleItemCount >= totalItemCount - 1) {
                                            homeViewModel.loadItems()
                                        }
                                    }
                                }

                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = dimensionResource(id = R.dimen.spacingXs)),
                                    verticalArrangement = Arrangement.spacedBy(
                                        space = dimensionResource(
                                            id = R.dimen.spacingSm
                                        )
                                    )
                                ) {
                                    itemsIndexed(homeViewModel.canvasItems) { index, item ->
                                        CanvasListItem(
                                            index = index,
                                            item = item,
                                            homeViewModel = homeViewModel,
                                            context = context,
                                            bottomPadding = if (index == homeViewModel.canvasItems.size - 1) if (homeViewModel.isLoadingMore) 0.dp else dimensionResource(
                                                id = R.dimen.spacingMd
                                            ) else 0.dp,
                                            navController = navController
                                        )
                                    }

                                    if (homeViewModel.isLoadingMore) {
                                        item {
                                            CanvasListLoadingItem(
                                                index = 0,
                                                count = 1,
                                                homeViewModel = homeViewModel,
                                                topPadding = 0.dp
                                            )
                                        }
                                    }
                                }
                                homeViewModel.listenForRealTimeUpdates()
                            }

                            if (!homeViewModel.isLoadingMore && homeViewModel.isEmpty) {
                                EmptyOrNotLoggedInUi(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .align(Alignment.Center),
                                    navController = navController,
                                    isUserLoggedIn = isUserLoggedIn
                                )
                                homeViewModel.listenForRealTimeUpdates()
                            }
                        }
                    }
                }

                Box(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 71.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate("ChooseUploadFormatScreen")
                    }
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(all = dimensionResource(id = R.dimen.spacingSm))) {
                    Image(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(id = R.drawable.scissor),
                        contentDescription = stringResource(
                            R.string.initiate_trim
                        )
                    )
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
fun EmptyOrNotLoggedInUi(
    modifier: Modifier, navController: NavHostController, isUserLoggedIn: MutableState<Boolean>
) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_project), contentDescription = null
        )
        if (!isUserLoggedIn.value) {
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    pushStringAnnotation(tag = "signIn", annotation = "signIn")
                    append(stringResource(R.string.sign_in))
                    pop()
                }
                append(stringResource(R.string.to_view_and_manage_your_projects_projects_created_while_not_signed_in_won_t_be_saved_to_your_account))
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "signIn", start = offset, end = offset
                    ).firstOrNull()?.let {
                        navController.navigate("SignInScreen/signIn")
                    }
                },
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingSm)),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            )
        } else {
            Text(
                text = stringResource(R.string.no_project_yet),
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingSm)),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@PreviewLightDark
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}