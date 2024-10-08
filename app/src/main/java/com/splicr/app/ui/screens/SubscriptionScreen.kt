package com.splicr.app.ui.screens

import android.app.Activity
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.splicr.app.R
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.SharedPreferenceUtil
import com.splicr.app.viewModel.SubscriptionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SubscriptionScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavHostController, subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
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

                val view = LocalView.current
                val tabs = listOf(R.string.monthly, R.string.yearly)
                val pagerState = rememberPagerState(pageCount = { tabs.size })
                val initialLaunch = remember { mutableStateOf(true) }

                val purchaseResult = subscriptionViewModel.purchaseResult.observeAsState()
                LaunchedEffect(purchaseResult.value) {
                    purchaseResult.value?.let { result ->
                        result.onSuccess {
                            goToHomeScreen(navController = navController)
                        }.onFailure { exception ->
                            snackBarIsError.value = true
                            snackBarMessageResource.intValue = 0
                            snackBarMessage.value = exception.localizedMessage ?: context.getString(
                                R.string.an_unexpected_error_occurred
                            )
                            scope.launch {
                                snackBarHostState.showSnackbar("")
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collectLatest { _ ->
                            if (!initialLaunch.value) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } else {
                                initialLaunch.value = false
                            }
                        }
                    }

                    CustomTopNavigationBar(modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl),
                            top = 72.dp
                        ),
                        centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) },
                        endStringResource = R.string.skip,
                        endOnClick = {
                            goToHomeScreen(navController = navController)
                        })

                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(
                                top = dimensionResource(id = R.dimen.spacingHuge),
                                start = dimensionResource(id = R.dimen.spacingXl),
                                end = dimensionResource(id = R.dimen.spacingXl)
                            )
                            .clip(
                                MaterialTheme.shapes.extraLarge
                            )
                            .background(MaterialTheme.colorScheme.surface),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Box(modifier = if (pagerState.currentPage == index) Modifier
                                .wrapContentSize()
                                .clip(
                                    MaterialTheme.shapes.extraLarge
                                )
                                .background(MaterialTheme.colorScheme.onBackground)
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.spacingLg),
                                    vertical = dimensionResource(id = R.dimen.spacingXs)
                                )
                                .clickable(interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null) {
                                    scope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                } else Modifier
                                .wrapContentSize()
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.spacingLg),
                                    vertical = dimensionResource(id = R.dimen.spacingXs)
                                )
                                .clickable(interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null) {
                                    scope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = stringResource(id = title),
                                    color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    HorizontalPager(
                        modifier = Modifier.weight(weight = 1f), state = pagerState
                    ) { page ->
                        when (page) {
                            0 -> {
                                PremiumOptions(
                                    prize = R.string._2_500_month, plan = R.string.monthly_premium
                                )
                            }

                            1 -> {
                                PremiumOptions(
                                    prize = R.string._27_000_year, plan = R.string.yearly_premium
                                )
                            }
                        }
                    }

                    val isChecked = rememberSaveable { mutableStateOf(false) }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            top = dimensionResource(id = R.dimen.spacingXl),
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl)
                        )
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.onSurface)
                        .clickable {
                            isChecked.value = !isChecked.value
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                        .padding(
                            vertical = dimensionResource(
                                id = R.dimen.spacingSm
                            ),
                            horizontal = dimensionResource(
                                id = R.dimen.spacingXl
                            ),
                        ), verticalAlignment = Alignment.CenterVertically) {

                        val today = LocalDate.now()
                        val dateInSevenDays = today.plusDays(7)
                        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                        val dueDate = dateInSevenDays.format(formatter)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    end = dimensionResource(
                                        id = R.dimen.spacingXl
                                    )
                                )
                        ) {
                            Text(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Start),
                                text = if (isChecked.value) stringResource(R.string.free_trial_selected) else stringResource(
                                    R.string.still_weighing_your_options
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Start
                            )

                            Text(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(
                                        top = dimensionResource(
                                            id = R.dimen.spacingXxxs
                                        )
                                    )
                                    .align(Alignment.Start),
                                text = if (isChecked.value) {
                                    stringResource(
                                        R.string.renews_on_at, dueDate, stringResource(
                                            id = if (pagerState.currentPage == 0) {
                                                R.string._2_500_mth
                                            } else {
                                                R.string._27_000_yr
                                            }
                                        )
                                    )
                                } else {
                                    stringResource(R.string.enable_7_days_free_trial)
                                },
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start
                            )
                        }

                        Box(modifier = if (isChecked.value) Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                isChecked.value = !isChecked.value
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } else Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape
                            )
                            .background(MaterialTheme.colorScheme.onSurface),
                            contentAlignment = Alignment.Center) {
                            if (isChecked.value) {
                                Image(
                                    painter = painterResource(id = R.drawable.check),
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.spacingMd),
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl)
                        ),
                        textResource = if (isChecked.value) R.string.try_free else R.string.get_premium
                    ) {
                        subscribe(
                            context = context,
                            pagerState = pagerState,
                            scope = scope,
                            snackBarHostState = snackBarHostState,
                            snackBarIsError = snackBarIsError,
                            snackBarMessage = snackBarMessage,
                            snackBarMessageResource = snackBarMessageResource,
                            subscriptionViewModel = subscriptionViewModel,
                            freeTrialEnabled = isChecked.value
                        )
                    }

                    Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacingMd)))

                    Text(modifier = Modifier
                        .wrapContentSize()
                        .padding(
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl),
                            bottom = dimensionResource(id = R.dimen.spacingXl)
                        )
                        .align(Alignment.CenterHorizontally)
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null) {
                            scope.launch {
                                subscriptionViewModel
                                    .restorePurchases(context = context)
                                    .onSuccess {
                                        goToHomeScreen(
                                            navController = navController,
                                            purchasesRestored = true
                                        )
                                    }
                                    .onFailure { exception ->
                                        snackBarIsError.value = true
                                        snackBarMessageResource.intValue = 0
                                        snackBarMessage.value =
                                            exception.localizedMessage ?: context.getString(
                                                R.string.failed_to_restore_purchases
                                            )
                                        scope.launch {
                                            snackBarHostState.showSnackbar("")
                                        }
                                    }
                            }
                        },
                        text = stringResource(R.string.restore_purchase),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }

                CustomSnackBar(
                    messageResource = snackBarMessageResource.intValue,
                    isError = snackBarIsError.value,
                    message = snackBarMessage.value,
                    snackBarHostState = snackBarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl)
                        )
                )
            }
        }
    }

}

fun subscribe(
    context: Context,
    pagerState: PagerState,
    freeTrialEnabled: Boolean,
    subscriptionViewModel: SubscriptionViewModel,
    snackBarIsError: MutableState<Boolean>,
    snackBarMessageResource: MutableIntState,
    snackBarMessage: MutableState<String>,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState
) {
    val productDetails = if (pagerState.currentPage == 0) {
        subscriptionViewModel.monthlyProductDetails.value
    } else {
        subscriptionViewModel.yearlyProductDetails.value
    }

    val basePlanId = if (pagerState.currentPage == 0) {
        if (freeTrialEnabled) {
            context.getString(R.string.monthly_basic_with_free_trial)
        } else {
            context.getString(R.string.monthly_basic)
        }
    } else {
        if (freeTrialEnabled) {
            context.getString(R.string.yearly_basic_with_free_trial)
        } else {
            context.getString(R.string.yearly_basic)
        }
    }

    if (productDetails != null) {
        subscriptionViewModel.startSubscriptionPurchase(
            activity = context as Activity, productDetails = productDetails, basePlanId = basePlanId
        ).onFailure { exception ->
            snackBarIsError.value = true
            snackBarMessageResource.intValue = 0
            snackBarMessage.value = exception.localizedMessage
                ?: context.getString(R.string.subscription_failed_please_try_again)
            scope.launch {
                snackBarHostState.showSnackbar("")
            }
        }
    } else {
        snackBarIsError.value = true
        snackBarMessageResource.intValue =
            R.string.subscription_options_are_unavailable_please_try_again_later
        snackBarMessage.value = ""
        scope.launch {
            snackBarHostState.showSnackbar("")
        }
    }
}

fun goToHomeScreen(navController: NavController, purchasesRestored: Boolean = false) {
    navController.navigate("HomeScreen/$purchasesRestored")
    SharedPreferenceUtil.atHomeScreen(true)
}

@Composable
fun PremiumOptions(prize: Int, plan: Int) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                top = dimensionResource(id = R.dimen.spacingXl),
                start = dimensionResource(id = R.dimen.spacingXl) + dimensionResource(id = R.dimen.spacingLg),
                end = dimensionResource(id = R.dimen.spacingXl) + dimensionResource(id = R.dimen.spacingLg)
            )
            .clip(
                MaterialTheme.shapes.medium
            )
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    1f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            )
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacingMd),
                vertical = dimensionResource(R.dimen.spacingXs)
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Image(
                modifier = Modifier
                    .width(110.dp)
                    .height(109.dp)
                    .padding(
                        top = dimensionResource(id = R.dimen.spacingXs)
                    ),
                painter = painterResource(id = R.drawable.premium_diamond),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        top = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    ),
                text = stringResource(id = plan),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        vertical = dimensionResource(
                            id = R.dimen.spacingXs
                        )
                    ),
                text = stringResource(R.string.unlock_premium_features_like),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        }

        val premiumFeatureList = listOf(
            R.string.uploading_media_files_from_urls,
            R.string.using_advanced_voice_prompts,
            R.string.exporting_in_4k_resolution,
            R.string.processing_media_longer_than_30_seconds
        )

        itemsIndexed(
            premiumFeatureList
        ) { index, _ ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        bottom = dimensionResource(
                            id = R.dimen.spacingXs
                        )
                    ),
                text = stringResource(id = premiumFeatureList[index]),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        }

        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        top = dimensionResource(
                            id = R.dimen.spacingXs
                        )
                    ),
                text = stringResource(
                    prize
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        top = dimensionResource(
                            id = R.dimen.spacingXxxs
                        ), bottom = dimensionResource(
                            id = R.dimen.spacingXs
                        )
                    ),
                text = stringResource(R.string.cancel_anytime_on_the_google_play_store_subscription_auto_renews_unless_canceled_at_least_24_hours_before_the_renewal_date),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        }

    }
}

@Composable
@PreviewLightDark
fun SubscriptionScreenPreview() {
    SubscriptionScreen(navController = rememberNavController())
}