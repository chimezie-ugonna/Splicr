package com.splicr.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.splicr.app.R
import com.splicr.app.data.OnboardingData
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.components.SecondaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.SharedPreferenceUtil
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavController
) {
    SplicrTheme(darkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            val scope = rememberCoroutineScope()
            val density = LocalDensity.current
            val items: ArrayList<OnboardingData> = ArrayList()
            items.add(
                OnboardingData(
                    R.drawable.onboarding1,
                    R.string.slice_and_dice_your_footage_in_cinematic_gold_using_ai
                )
            )
            items.add(
                OnboardingData(
                    R.drawable.onboarding2,
                    R.string.trim_the_excess_keep_the_best_precision_at_your_fingertips
                )
            )
            items.add(
                OnboardingData(
                    R.drawable.onboarding3, R.string.clip_the_moments_that_matter
                )
            )

            Box(
                Modifier.fillMaxSize()
            ) {

                val pagerState = rememberPagerState(pageCount = {
                    items.size
                })

                val pagerTopPadding = remember {
                    mutableStateOf(0.dp)
                }
                val pagerTextBottomPadding = remember {
                    mutableStateOf(0.dp)
                }
                val sideSpacing = dimensionResource(
                    id = R.dimen.spacingXl
                )
                val topSpacing = 72.dp

                Pager(
                    items = items,
                    pagerState = pagerState,
                    topPadding = pagerTopPadding.value,
                    bottomPadding = pagerTextBottomPadding.value
                )

                CustomTopNavigationBar(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopCenter)
                    .padding(
                        top = topSpacing,
                        start = dimensionResource(id = R.dimen.spacingXl),
                        end = dimensionResource(id = R.dimen.spacingXl)
                    )
                    .onGloballyPositioned { coordinates ->
                        pagerTopPadding.value = with(density) {
                            coordinates.size.height.toDp() + sideSpacing + topSpacing
                        }
                    }, centerComposable = {
                    AppNameText(modifier = Modifier.align(Alignment.Center))
                }, endComposable = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterEnd)
                    ) {
                        AnimatedVisibility(
                            visible = pagerState.currentPage != pagerState.pageCount - 1,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                goToSubscriptionScreen(
                                    navController = navController
                                )
                            },
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {

                            Text(
                                text = stringResource(R.string.skip),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        if (pagerState.currentPage == pagerState.pageCount - 1) {
                            Text(
                                text = stringResource(R.string.skip),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                })

                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl),
                            bottom = 59.41.dp
                        )
                        .onGloballyPositioned { coordinates ->
                            pagerTextBottomPadding.value =
                                with(density) { coordinates.size.height.toDp() + 54.dp }
                        }) {

                    this@Box.Indicators(size = items.size, index = pagerState.currentPage)

                    if (pagerState.currentPage != pagerState.pageCount - 1) {
                        SecondaryButton(textResource = R.string.Continue) {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    } else {
                        PrimaryButton(textResource = R.string.get_started) {
                            goToSubscriptionScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

fun goToSubscriptionScreen(navController: NavController) {
    navController.navigate("SubscriptionScreen")
    SharedPreferenceUtil.onboarded(true)
}

@Composable
fun Pager(
    items: ArrayList<OnboardingData>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp
) {
    HorizontalPager(
        state = pagerState, verticalAlignment = Alignment.Top, modifier = modifier.fillMaxSize()
    ) { page ->
        val bottomSpacing = 49.41.dp
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    top = topPadding
                )
        ) {
            Image(
                painter = painterResource(items[page].imageResource),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(id = R.dimen.spacingXl),
                        end = dimensionResource(id = R.dimen.spacingXl),
                        bottom = bottomSpacing
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                            0.15f to MaterialTheme.colorScheme.background
                        )
                    )
            ) {

                Text(
                    text = stringResource(items[page].descriptionResource),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            top = if (pagerState.currentPage == 2) 146.dp else 100.dp,
                            start = dimensionResource(id = R.dimen.spacingXl),
                            end = dimensionResource(id = R.dimen.spacingXl),
                            bottom = bottomPadding + bottomSpacing
                        ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BoxScope.Indicators(size: Int, index: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = dimensionResource(id = R.dimen.spacingXl))
    ) {
        repeat(size) {
            Indicator(isSelected = it == index)
        }
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.spacingXxs))
            .width(
                animateDpAsState(
                    targetValue = if (isSelected) dimensionResource(id = R.dimen.spacingXm) else dimensionResource(
                        id = R.dimen.spacingXxs
                    ),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                    label = ""
                ).value
            )
            .clip(if (isSelected) MaterialTheme.shapes.medium else CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
    )
}

@PreviewLightDark
@Composable
fun OnboardingPreview() {
    OnboardingScreen(navController = rememberNavController())
}