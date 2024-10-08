package com.splicr.app.ui.screens

import android.app.Activity
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.splicr.app.R
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.viewModel.SubscriptionStatus
import com.splicr.app.viewModel.SubscriptionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ManageSubscriptionScreen(
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

                val context = LocalContext.current
                val view = LocalView.current
                val tabs = listOf(R.string.monthly, R.string.yearly)
                val pagerState = rememberPagerState(pageCount = { tabs.size })
                val initialLaunch = remember { mutableStateOf(true) }
                val subscriptionStatus =
                    subscriptionViewModel.subscriptionStatus.observeAsState(initial = SubscriptionStatus.NONE)
                val subscriptionRenewalDate =
                    subscriptionViewModel.renewalDate.observeAsState(initial = null)
                val subscriptionExpiryDate =
                    subscriptionViewModel.expiryDate.observeAsState(initial = null)
                val currentPlan = rememberSaveable {
                    mutableStateOf(false)
                }
                LaunchedEffect(subscriptionStatus.value) {
                    if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                        currentPlan.value =
                            pagerState.currentPage == 0 && subscriptionStatus.value == SubscriptionStatus.MONTHLY_SUBSCRIPTION || pagerState.currentPage == 0 && subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL || pagerState.currentPage == 1 && subscriptionStatus.value == SubscriptionStatus.YEARLY_SUBSCRIPTION || pagerState.currentPage == 1 && subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL
                    }
                }
                LaunchedEffect(subscriptionRenewalDate.value) {

                }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collectLatest { _ ->
                        if (!initialLaunch.value) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        } else {
                            initialLaunch.value = false
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 72.dp
                        )
                ) {
                    CustomTopNavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimensionResource(
                                    id = R.dimen.spacingXl
                                ), end = dimensionResource(
                                    id = R.dimen.spacingXl
                                )
                            ),
                        startImageResource = R.drawable.back,
                        startStringResource = R.string.go_back,
                        startOnClick = { navController.popBackStack() },
                        centerStringResource = R.string.manage_subscription
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Start)
                            .padding(
                                top = 28.dp, start = dimensionResource(
                                    id = R.dimen.spacingXl
                                ), end = dimensionResource(
                                    id = R.dimen.spacingXl
                                ), bottom = dimensionResource(id = R.dimen.spacingXs)
                            ),
                        text = stringResource(R.string.your_plan),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )

                    HorizontalPager(
                        modifier = Modifier.weight(1f), state = pagerState
                    ) { page ->
                        when (page) {
                            0 -> {
                                Options(
                                    prize = R.string._2_500_month, plan = R.string.monthly_premium
                                )
                            }

                            1 -> {
                                Options(
                                    prize = R.string._27_000_year, plan = R.string.yearly_premium
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(
                                all = dimensionResource(id = R.dimen.spacingXl)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(
                                    MaterialTheme.shapes.small
                                )
                                .background(MaterialTheme.colorScheme.surface),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Box(modifier = if (pagerState.currentPage == index) Modifier
                                    .weight(
                                        1f
                                    )
                                    .wrapContentHeight()
                                    .clip(
                                        MaterialTheme.shapes.small
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
                                    .weight(1f)
                                    .wrapContentHeight()
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

                        PrimaryButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(id = R.dimen.spacingSm)),
                            textResource = if (subscriptionStatus.value != SubscriptionStatus.NONE) if (currentPlan.value) R.string.cancel_plan else R.string.change_plan else R.string.get_plan,
                            uiEnabled = !currentPlan.value
                        ) {
                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                if (currentPlan.value) {
                                    subscriptionViewModel.cancelSubscription(activity = context as Activity)
                                } else {
                                    subscribe(
                                        context = context,
                                        pagerState = pagerState,
                                        scope = scope,
                                        snackBarHostState = snackBarHostState,
                                        snackBarIsError = snackBarIsError,
                                        snackBarMessage = snackBarMessage,
                                        snackBarMessageResource = snackBarMessageResource,
                                        subscriptionViewModel = subscriptionViewModel,
                                        freeTrialEnabled = true
                                    )
                                }
                            } else {
                                subscribe(
                                    context = context,
                                    pagerState = pagerState,
                                    scope = scope,
                                    snackBarHostState = snackBarHostState,
                                    snackBarIsError = snackBarIsError,
                                    snackBarMessage = snackBarMessage,
                                    snackBarMessageResource = snackBarMessageResource,
                                    subscriptionViewModel = subscriptionViewModel,
                                    freeTrialEnabled = true
                                )
                            }
                        }

                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally)
                                .padding(
                                    top = dimensionResource(id = R.dimen.spacingSm)
                                ),
                            text = if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL || subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                    if (subscriptionExpiryDate.value != null) {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_trial_expires_on,
                                                    subscriptionExpiryDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_trial_expires_on,
                                                    subscriptionExpiryDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    } else if (subscriptionRenewalDate.value != null) {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_for_7_days_then_starting,
                                                    stringResource(id = R.string._2_500_mth),
                                                    subscriptionRenewalDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_for_7_days_then_starting,
                                                    stringResource(id = R.string._27_000_yr),
                                                    subscriptionRenewalDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    } else {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_for_7_days_then,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.free_for_7_days_then,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    if (subscriptionExpiryDate.value != null) {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.premium_expires_on,
                                                    subscriptionExpiryDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.premium_expires_on,
                                                    subscriptionExpiryDate.value!!
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    } else if (subscriptionRenewalDate.value != null) {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.renews_on_at,
                                                    subscriptionRenewalDate.value!!,
                                                    stringResource(
                                                        id = R.string._2_500_mth
                                                    )
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.renews_on_at,
                                                    subscriptionRenewalDate.value!!,
                                                    stringResource(
                                                        id = R.string._27_000_yr
                                                    )
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    } else {
                                        if (pagerState.currentPage == 0) {
                                            if (subscriptionStatus.value == SubscriptionStatus.MONTHLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.renews_at,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._2_500_mth)
                                                )
                                            }
                                        } else {
                                            if (subscriptionStatus.value == SubscriptionStatus.YEARLY_FREE_TRIAL) {
                                                stringResource(
                                                    R.string.renews_at,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.cancel_anytime,
                                                    stringResource(id = R.string._27_000_yr)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (pagerState.currentPage == 0) stringResource(
                                    R.string.cancel_anytime,
                                    stringResource(id = R.string._2_500_mth)
                                ) else stringResource(
                                    R.string.cancel_anytime,
                                    stringResource(id = R.string._27_000_yr)
                                )
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
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

@Composable
fun Options(prize: Int, plan: Int) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.spacingXl)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        top = dimensionResource(id = R.dimen.spacingXs)
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
                        horizontal = dimensionResource(id = R.dimen.spacingXl),
                        vertical = dimensionResource(id = R.dimen.spacingMd)
                    ), verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(
                            end = dimensionResource(
                                id = R.dimen.spacingMd
                            )
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Start),
                        text = stringResource(id = plan),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Start
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(
                                top = dimensionResource(
                                    id = R.dimen.spacingXs
                                )
                            )
                            .align(Alignment.Start),
                        text = stringResource(R.string.unlock_all_premium_features),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )

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
                }

                Image(
                    modifier = Modifier
                        .width(80.dp)
                        .height(78.dp),
                    painter = painterResource(id = R.drawable.premium_diamond),
                    contentDescription = null
                )

            }
        }

        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp),
                text = stringResource(R.string.what_you_get),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )
        }

        val list = listOf(
            R.string.seamlessly_upload_media_files_directly_from_urls_ensuring_a_streamlined_and_efficient_workflow,
            R.string.utilize_advanced_voice_prompts_to_enhance_your_user_interactions_and_improve_overall_engagement,
            R.string.export_your_media_in_stunning_4k_resolution_delivering_high_quality_content_with_exceptional_clarity_and_detail,
            R.string.unlock_the_ability_to_process_media_longer_than_30_seconds_enabling_extended_content_creation_and_editing_capabilities
        )

        itemsIndexed(list) { _, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = dimensionResource(id = R.dimen.spacingMd))
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                            .padding(end = dimensionResource(id = R.dimen.spacingXl)),
                        text = stringResource(item),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )

                    Image(
                        painter = painterResource(id = R.drawable.check2), contentDescription = null
                    )

                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingXs)),
                    thickness = 0.9.dp,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
fun ManageSubscriptionScreenPreview() {
    ManageSubscriptionScreen(navController = rememberNavController())
}