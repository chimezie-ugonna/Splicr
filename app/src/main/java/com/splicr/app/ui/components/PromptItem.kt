package com.splicr.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.splicr.app.R
import com.splicr.app.data.AspectRatioChoiceItemData
import com.splicr.app.data.CanvasItemData
import com.splicr.app.data.PromptItemData
import com.splicr.app.data.TrimRangeData
import com.splicr.app.utils.MediaConfigurationUtil.convertDimensionsToAspectRatio
import com.splicr.app.utils.MediaConfigurationUtil.getAllVideoMetadata
import com.splicr.app.utils.MediaConfigurationUtil.processVideo
import com.splicr.app.viewModel.PromptViewModel
import kotlinx.coroutines.launch

@Composable
fun PromptItem(
    modifier: Modifier,
    isAuthor: Boolean,
    videoUriString: String? = null,
    thumbnailBitmap: Bitmap? = null,
    duration: String? = null,
    message: AnnotatedString,
    showCanvasOptions: Boolean,
    isLoading: Boolean = false,
    listState: LazyListState? = null,
    trimRanges: List<TrimRangeData>? = null,
    isProcessing: MutableState<Boolean>? = null,
    viewModel: PromptViewModel? = null,
    navController: NavHostController,
    aspectRatioChoiceList: List<AspectRatioChoiceItemData>? = null,
    onClick: (Int) -> Unit
) {
    val layoutDirection = if (isAuthor) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.spacingXs))
        ) {
            if (!isAuthor) {
                Image(
                    modifier = Modifier
                        .size(size = dimensionResource(id = R.dimen.spacingXl))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .background(
                            color = Color.Transparent, shape = CircleShape
                        ),
                    painter = painterResource(id = R.drawable.splash_screen_logo),
                    contentDescription = null
                )
            }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = MaterialTheme.shapes.small)
                    .background(color = if (isAuthor) Color(color = 0XFF2C2C2C) else Color(color = 0XFF242620))
                    .padding(
                        all = if (showCanvasOptions || thumbnailBitmap == null) dimensionResource(
                            id = R.dimen.spacingSm
                        ) else 0.dp
                    ), contentAlignment = Alignment.Center
            ) {

                when {
                    showCanvasOptions && aspectRatioChoiceList != null -> {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Column {
                                Text(
                                    modifier = Modifier.wrapContentSize(),
                                    text = stringResource(R.string.select_your_aspect_ratio),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Start
                                )
                                val isTappable = isProcessing != null && !isProcessing.value
                                repeat(aspectRatioChoiceList.size) { index ->
                                    val aspectRatioWidth =
                                        aspectRatioChoiceList[index].aspectRatioWidth
                                    val aspectRatioHeight =
                                        aspectRatioChoiceList[index].aspectRatioHeight
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(top = if (index != 0) dimensionResource(id = R.dimen.spacingXs) else 10.dp)
                                        .clip(shape = MaterialTheme.shapes.medium)
                                        .alpha(if (isTappable) 1f else 0.3f)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .clickable(enabled = isTappable) {
                                            if (trimRanges != null && videoUriString != null && isProcessing != null && !isProcessing.value && viewModel != null) {
                                                viewModel.addListItem(
                                                    PromptItemData(
                                                        message = AnnotatedString(
                                                            text = context.getString(
                                                                R.string.got_it_i_m_on_it_and_will_have_it_sorted_out_for_you_soon_thanks_for_your_patience
                                                            )
                                                        )
                                                    )
                                                )
                                                isProcessing.value = true
                                                val item = PromptItemData(
                                                    isLoading = true
                                                )
                                                viewModel.addListItem(item)
                                                scope.launch {
                                                    listState?.animateScrollToItem(
                                                        viewModel.listItems.size - 1
                                                    )
                                                }
                                                processVideo(
                                                    uri = Uri.parse(videoUriString),
                                                    trimRanges = trimRanges,
                                                    context = context,
                                                    aspectRatioWidth = aspectRatioWidth,
                                                    aspectRatioHeight = aspectRatioHeight
                                                ) { uri ->
                                                    viewModel.removeListItem(item)
                                                    if (uri != null) {
                                                        val size = getAllVideoMetadata(
                                                            context = context, videoUri = uri
                                                        )?.fileSize
                                                        if (size != null) {
                                                            viewModel.addListItem(
                                                                PromptItemData(
                                                                    message = AnnotatedString(
                                                                        text = context.getString(
                                                                            R.string.done
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                            navController.navigate(
                                                                "MediaPlayerScreen/${
                                                                    Uri.encode(
                                                                        Gson().toJson(
                                                                            CanvasItemData(
                                                                                aspectRatioTypeKey = context.getString(
                                                                                    aspectRatioChoiceList[index].typeStringResource
                                                                                ),
                                                                                aspectRatioWidth = aspectRatioWidth,
                                                                                aspectRatioHeight = aspectRatioHeight,
                                                                                size = size
                                                                            )
                                                                        )
                                                                    )
                                                                }/${
                                                                    Uri.encode(
                                                                        uri.toString()
                                                                    )
                                                                }"
                                                            )
                                                        } else {
                                                            viewModel.addListItem(
                                                                PromptItemData(
                                                                    message = AnnotatedString(
                                                                        text = context.getString(
                                                                            R.string.oops_i_ran_into_an_issue_while_processing_your_request_let_s_try_that_again_how_would_you_like_me_to_trim
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    } else {
                                                        viewModel.addListItem(
                                                            PromptItemData(
                                                                message = AnnotatedString(
                                                                    text = context.getString(
                                                                        R.string.oops_i_ran_into_an_issue_while_processing_your_request_let_s_try_that_again_how_would_you_like_me_to_trim
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    }
                                                    isProcessing.value = false
                                                    scope.launch {
                                                        listState?.animateScrollToItem(
                                                            viewModel.listItems.size - 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        .background(color = Color.Transparent)
                                        .padding(
                                            all = dimensionResource(id = R.dimen.spacingMd)
                                        ), verticalAlignment = Alignment.CenterVertically

                                    ) {
                                        Column(
                                            Modifier
                                                .weight(1f)
                                                .wrapContentHeight()
                                                .padding(
                                                    end = if (aspectRatioChoiceList[index].iconResourceList != null) dimensionResource(
                                                        id = R.dimen.spacingMd
                                                    ) else 0.dp
                                                ), verticalArrangement = Arrangement.spacedBy(
                                                space = dimensionResource(
                                                    id = R.dimen.spacingXs
                                                )
                                            )
                                        ) {
                                            Text(
                                                modifier = Modifier.wrapContentSize(),
                                                text = stringResource(id = aspectRatioChoiceList[index].typeStringResource),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Start
                                            )
                                            Text(
                                                modifier = Modifier.wrapContentSize(),
                                                text = convertDimensionsToAspectRatio(
                                                    context = context,
                                                    width = aspectRatioWidth,
                                                    height = aspectRatioHeight
                                                ),
                                                color = MaterialTheme.colorScheme.tertiary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Start
                                            )
                                        }

                                        val iconResourceList =
                                            aspectRatioChoiceList[index].iconResourceList
                                        if (iconResourceList != null) {
                                            Row(
                                                modifier = Modifier.wrapContentSize(),
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    space = dimensionResource(
                                                        id = R.dimen.spacingXs
                                                    )
                                                )
                                            ) {
                                                repeat(iconResourceList.size) { index ->
                                                    Image(
                                                        modifier = Modifier.size(
                                                            size = dimensionResource(
                                                                id = R.dimen.spacingMd
                                                            )
                                                        ),
                                                        painter = painterResource(id = iconResourceList[index]),
                                                        contentDescription = null
                                                    )
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    thumbnailBitmap != null -> {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(shape = MaterialTheme.shapes.small)
                                .border(
                                    shape = MaterialTheme.shapes.small,
                                    width = 1.dp,
                                    color = if (isAuthor) Color(color = 0XFF2C2C2C) else Color(color = 0XFF242620)
                                )
                        ) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                bitmap = thumbnailBitmap.asImageBitmap(),
                                contentDescription = stringResource(R.string.video_thumbnail),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            0f to MaterialTheme.colorScheme.surface.copy(
                                                alpha = 0.3f
                                            ), 1f to MaterialTheme.colorScheme.surface.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    )
                                    .padding(all = dimensionResource(id = R.dimen.spacingSm)),
                                contentAlignment = Alignment.Center
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        modifier = Modifier.wrapContentSize(),
                                        text = duration ?: stringResource(id = R.string._0_00),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    isLoading -> {
                        val transitionState = rememberInfiniteTransition(label = "loading")
                        val currentIndex by transitionState.animateValue(
                            initialValue = 0,
                            targetValue = 3,
                            typeConverter = Int.VectorConverter,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 500, easing = LinearEasing
                                ), repeatMode = RepeatMode.Restart
                            ),
                            label = "loading"
                        )

                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(dimensionResource(id = R.dimen.spacingXxxs)),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = dimensionResource(id = R.dimen.spacingXxxs)
                            )
                        ) {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.spacingXs))
                                        .background(
                                            color = if (index == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    else -> {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            if (onClick == {}) {
                                Text(
                                    modifier = Modifier.wrapContentSize(),
                                    text = message,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Start
                                )
                            } else {
                                ClickableText(
                                    text = message,
                                    onClick = onClick,
                                    modifier = Modifier.wrapContentSize(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Start
                                    )
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}