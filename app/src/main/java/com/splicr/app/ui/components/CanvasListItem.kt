package com.splicr.app.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.google.gson.Gson
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.data.CanvasOptionsItemData
import com.splicr.app.utils.MediaConfigurationUtil.formatTimestamp
import com.splicr.app.viewModel.HomeViewModel

@Composable
fun CanvasListItem(
    index: Int,
    item: CanvasItemData,
    homeViewModel: HomeViewModel,
    context: Context,
    bottomPadding: Dp,
    navController: NavController
) {
    val expanded = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                top = if (index == 0) dimensionResource(id = R.dimen.spacingXs) else 0.dp,
                bottom = bottomPadding
            )
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.medium),
            model = item.thumbnailUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(
                    shape = RoundedCornerShape(
                        bottomStart = 10.dp, bottomEnd = 10.dp
                    )
                )
                .background(
                    brush = Brush.linearGradient(
                        0f to MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.3f
                        ), 1f to MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.7f
                        )
                    )
                )
                .padding(
                    vertical = dimensionResource(id = R.dimen.spacingSm),
                    horizontal = dimensionResource(
                        id = R.dimen.spacingXl
                    )
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(
                        end = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    ), verticalArrangement = Arrangement.spacedBy(
                    space = dimensionResource(
                        id = R.dimen.spacingXxxs
                    )
                )
            ) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(item.createdAt),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.wrapContentSize()) {
                Image(modifier = Modifier
                    .wrapContentSize()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null) {
                        expanded.value = !expanded.value
                    },
                    painter = painterResource(id = R.drawable.more),
                    contentDescription = null
                )
                if (expanded.value) {
                    val density = LocalDensity.current.density
                    val offsetYInPx =
                        (dimensionResource(id = R.dimen.spacingXl).value * density).toInt()

                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(x = 0, y = offsetYInPx),
                        onDismissRequest = {
                            if (expanded.value) {
                                expanded.value = false
                            }
                        },
                        properties = PopupProperties(focusable = true)
                    ) {
                        AnimatedVisibility(
                            visible = expanded.value, enter = scaleIn(), exit = scaleOut()
                        ) {

                            LazyColumn(
                                modifier = Modifier
                                    .width(112.dp)
                                    .wrapContentHeight()
                            ) {
                                val optionsItems = listOf(
                                    CanvasOptionsItemData(
                                        titleStringResource = R.string.export,
                                        iconResource = R.drawable.export
                                    ), CanvasOptionsItemData(
                                        titleStringResource = R.string.delete,
                                        iconResource = R.drawable.delete
                                    )
                                )
                                itemsIndexed(optionsItems) { index, item2 ->
                                    Column(modifier = Modifier
                                        .wrapContentSize()
                                        .clip(
                                            shape = if (index == 0) RoundedCornerShape(
                                                topStart = dimensionResource(
                                                    id = R.dimen.spacingXxxs
                                                ), topEnd = dimensionResource(
                                                    id = R.dimen.spacingXxxs
                                                )
                                            ) else RoundedCornerShape(
                                                bottomStart = dimensionResource(
                                                    id = R.dimen.spacingXxxs
                                                ), bottomEnd = dimensionResource(
                                                    id = R.dimen.spacingXxxs
                                                )
                                            )
                                        )
                                        .clickable {
                                            if (item2.titleStringResource == R.string.delete) {
                                                homeViewModel.deleteItem(
                                                    item = item, context = context
                                                )
                                            } else {
                                                navController.navigate(
                                                    route = "MediaSplicedScreen/${
                                                        Uri.encode(
                                                            Gson().toJson(
                                                                item
                                                            )
                                                        )
                                                    }/${
                                                        context.getString(R.string.empty)
                                                    }/HomeScreen/${
                                                        0
                                                    }/${
                                                        false
                                                    }"
                                                )
                                            }
                                            expanded.value = false
                                        }
                                        .background(color = MaterialTheme.colorScheme.surface)) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .padding(
                                                    top = if (index == 0) dimensionResource(
                                                        id = R.dimen.spacingXs
                                                    ) else dimensionResource(
                                                        id = R.dimen.spacingXxxs
                                                    ), start = dimensionResource(
                                                        id = R.dimen.spacingSm
                                                    ), end = dimensionResource(
                                                        id = R.dimen.spacingSm
                                                    ), bottom = if (index == 0) dimensionResource(
                                                        id = R.dimen.spacingXxxs
                                                    ) else dimensionResource(
                                                        id = R.dimen.spacingXs
                                                    )
                                                ), verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Text(
                                                modifier = Modifier
                                                    .wrapContentHeight()
                                                    .weight(1f)
                                                    .padding(
                                                        end = dimensionResource(
                                                            id = R.dimen.spacingSm
                                                        )
                                                    ),
                                                text = stringResource(
                                                    item2.titleStringResource
                                                ),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Image(
                                                painter = painterResource(
                                                    item2.iconResource
                                                ), contentDescription = null
                                            )

                                        }

                                        if (index == 0) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CanvasListLoadingItem(
    index: Int,
    count: Int,
    homeViewModel: HomeViewModel,
    topPadding: Dp = if (index == 0) dimensionResource(id = R.dimen.spacingXs) else 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(
                top = topPadding, bottom = if (index == count - 1) dimensionResource(
                    id = R.dimen.spacingMd
                ) else 0.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(
                        alpha = 0.1f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.3f
                            ), 1f to MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.7f
                            )
                        )
                    )
                    .padding(
                        vertical = dimensionResource(id = R.dimen.spacingSm),
                        horizontal = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(
                            end = dimensionResource(
                                id = R.dimen.spacingXl
                            )
                        ), verticalArrangement = Arrangement.spacedBy(
                        space = dimensionResource(
                            id = R.dimen.spacingXxxs
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(
                                dimensionResource(id = R.dimen.spacingSm)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.tertiary.copy(
                                    alpha = 0.15f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(
                                dimensionResource(id = R.dimen.spacingSm)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.tertiary.copy(
                                    alpha = 0.15f
                                )
                            )
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.more), contentDescription = null
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .placeholder(
                    visible = homeViewModel.isLoadingMore, highlight = PlaceholderHighlight.shimmer(
                        highlightColor = MaterialTheme.colorScheme.onBackground
                    ), color = Color.Transparent, shape = MaterialTheme.shapes.medium
                )
        )
    }
}