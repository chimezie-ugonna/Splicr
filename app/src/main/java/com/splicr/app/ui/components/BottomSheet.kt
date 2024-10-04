@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.data.ListItemData
import com.splicr.app.utils.MediaConfigurationUtil.shareVideo
import com.splicr.app.viewModel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CustomBottomSheet(
    navController: NavHostController,
    label: Int,
    showBottomSheet: MutableState<Boolean>,
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    commentValue: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue("")) },
    hasPerformedHapticFeedback: MutableState<Boolean>? = null,
    snackBarMessageResource: MutableState<Int> = remember {
        mutableIntStateOf(0)
    },
    snackBarMessage: MutableState<String> = remember {
        mutableStateOf("")
    },
    scope: CoroutineScope = rememberCoroutineScope(),
    snackBarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    },
    snackBarIsError: MutableState<Boolean> = remember {
        mutableStateOf(true)
    },
    videoUriString: String? = null,
    filePath: String? = null,
    canvasItemData: CanvasItemData = CanvasItemData(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    viewModel: SettingsViewModel = viewModel(),
    loaderDescription: Int? = null,
    showLoader: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val loaderList = listOf(
        R.string.exporting_medium,
        R.string.signing_in,
        R.string.creating_account,
        R.string.sending_link,
        R.string.deleting_item,
        R.string.deleting_account,
        R.string.sending_feedback,
        R.string.sending_password_reset_link,
        R.string.resending_password_reset_link
    )
    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                if (loaderList.contains(label).not()) {
                    showBottomSheet.value = false
                    if (hasPerformedHapticFeedback != null) {
                        hasPerformedHapticFeedback.value = false
                    }
                } else {
                    scope.launch { sheetState.show() }
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp
            ),
            modifier = Modifier.systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(all = dimensionResource(id = R.dimen.spacingXl))
            ) {
                if (loaderList.contains(label).not()) {
                    Text(text = stringResource(R.string.close),
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Start)
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null) {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showBottomSheet.value = false
                                            if (hasPerformedHapticFeedback != null) {
                                                hasPerformedHapticFeedback.value = false
                                            }
                                        }
                                    }
                            },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                }

                if (label == R.string.saved_to_your_device || label == R.string.saved_to_your_device_and_your_account) {
                    Image(
                        modifier = Modifier
                            .size(size = 58.dp)
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(
                                top = dimensionResource(
                                    id = R.dimen.spacingSm
                                )
                            ),
                        painter = painterResource(id = R.drawable.confetti),
                        contentDescription = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            top = if (loaderList
                                    .contains(label)
                                    .not()
                            ) dimensionResource(
                                id = R.dimen.spacingSm
                            ) else 0.dp
                        ), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .padding(
                                end = if (label != R.string.voice_prompt) 0.dp else dimensionResource(
                                    id = R.dimen.spacingSm
                                )
                            ),
                        text = stringResource(id = label),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = if (label != R.string.voice_prompt) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
                        fontWeight = if (label != R.string.voice_prompt) FontWeight.SemiBold else FontWeight.Bold,
                        textAlign = if (label != R.string.voice_prompt) TextAlign.Center else TextAlign.Start
                    )

                    if (label == R.string.voice_prompt) {
                        PremiumText(modifier = Modifier)
                    }
                }

                when {
                    label == R.string.faqs -> {
                        if (viewModel.isEmpty) {
                            Text(
                                text = stringResource(R.string.no_faqs_available),
                                modifier = Modifier
                                    .padding(top = dimensionResource(id = R.dimen.spacingSm))
                                    .align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal
                            )
                        } else {
                            val listItems = viewModel.faqs
                            val listState = rememberLazyListState()

                            LaunchedEffect(listState) {
                                snapshotFlow { listState.layoutInfo }.collect { layoutInfo ->
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    val totalItemCount = layoutInfo.totalItemsCount
                                    if (totalItemCount > 0) {
                                        if (visibleItems.lastOrNull()?.index == totalItemCount - 1) {
                                            viewModel.loadFAQs()
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(
                                        top = dimensionResource(
                                            id = R.dimen.spacingSm
                                        )
                                    )
                                    .clip(shape = MaterialTheme.shapes.small)
                                    .background(color = MaterialTheme.colorScheme.surface)
                            ) {
                                itemsIndexed(listItems) { index, item ->
                                    CustomDropDownListItem(
                                        showDivider = index != listItems.size - 1,
                                        textString = item.question,
                                        subTextString = item.answer
                                    )
                                }

                                if (viewModel.isLoadingMore) {
                                    item {
                                        Column {
                                            if (listItems.isNotEmpty()) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(
                                                        start = dimensionResource(id = R.dimen.spacingMd)
                                                    ),
                                                    thickness = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .padding(all = dimensionResource(id = R.dimen.spacingMd)),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                Text(
                                                    text = "",
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Start,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(
                                                            end = dimensionResource(id = R.dimen.spacingMd)
                                                        )
                                                        .placeholder(
                                                            visible = viewModel.isLoadingMore,
                                                            highlight = PlaceholderHighlight.shimmer(
                                                                highlightColor = MaterialTheme.colorScheme.onBackground
                                                            ),
                                                            color = MaterialTheme.colorScheme.tertiary.copy(
                                                                alpha = 0.15f
                                                            ),
                                                            shape = MaterialTheme.shapes.medium
                                                        )
                                                )

                                                Image(
                                                    painter = painterResource(id = R.drawable.arrow_right),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    label == R.string.contact_support -> {
                        val listItems = listOf(
                            ListItemData(
                                leadingIconResource = R.drawable.mail,
                                titleResource = R.string.email,
                                subText = stringResource(
                                    R.string.legal_options_gmail_com
                                )
                            ), ListItemData(
                                leadingIconResource = R.drawable.x,
                                titleResource = R.string.x,
                                subText = stringResource(
                                    R.string.legal_options_gmail_com
                                )
                            ), ListItemData(
                                leadingIconResource = R.drawable.instagram,
                                titleResource = R.string.instagram,
                                subText = stringResource(
                                    R.string.legal_options_gmail_com
                                )
                            )
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            itemsIndexed(listItems) { index, item ->
                                CustomListItem(modifier = if (item.showArrow) Modifier
                                    .customModifier(
                                        index = index,
                                        listSize = listItems.size,
                                        topPadding = dimensionResource(
                                            id = R.dimen.spacingSm
                                        )
                                    )
                                    .clickable {
                                        scope
                                            .launch { sheetState.hide() }
                                            .invokeOnCompletion {
                                                if (!sheetState.isVisible) {
                                                    showBottomSheet.value = false
                                                    if (hasPerformedHapticFeedback != null) {
                                                        hasPerformedHapticFeedback.value = false
                                                    }
                                                }
                                                when (item.titleResource) {
                                                    R.string.email -> {
                                                        val emailIntent =
                                                            Intent(Intent.ACTION_SENDTO).apply {
                                                                data = Uri.parse("mailto:")
                                                                putExtra(
                                                                    Intent.EXTRA_EMAIL,
                                                                    arrayOf(item.subText)
                                                                )
                                                            }
                                                        if (emailIntent.resolveActivity(context.packageManager) != null) {
                                                            context.startActivity(emailIntent)
                                                        } else {
                                                            snackBarIsError.value = true
                                                            snackBarMessageResource.value =
                                                                R.string.we_could_not_find_any_application_to_handle_that_operation
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    ""
                                                                )
                                                            }
                                                        }
                                                    }

                                                    R.string.x -> {
                                                        try {
                                                            context.startActivity(
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(
                                                                        "twitter://user?screen_name=${
                                                                            item.subText
                                                                        }"
                                                                    )
                                                                )
                                                            )
                                                        } catch (e: ActivityNotFoundException) {
                                                            context.startActivity(
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(
                                                                        "https://twitter.com/${
                                                                            item.subText
                                                                        }"
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    }

                                                    R.string.instagram -> {
                                                        try {
                                                            context.startActivity(
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(
                                                                        "instagram://user?username=${
                                                                            item.subText
                                                                        }"
                                                                    )
                                                                )
                                                            )
                                                        } catch (e: ActivityNotFoundException) {
                                                            context.startActivity(
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(
                                                                        "https://instagram.com/${
                                                                            item.subText
                                                                        }"
                                                                    )
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                    } else Modifier.customModifier(
                                    index = index, listSize = listItems.size
                                ),
                                    showDivider = index != listItems.size - 1,
                                    textStringResource = item.titleResource,
                                    textString = item.titleString,
                                    subTextString = item.subText,
                                    showArrow = item.showArrow,
                                    leadingIcon = item.leadingIconResource,
                                    leadingIconHeight = dimensionResource(
                                        id = R.dimen.spacingMd
                                    ),
                                    leadingIconWidth = dimensionResource(
                                        id = R.dimen.spacingMd
                                    ))
                            }
                        }
                    }

                    loaderList.contains(label) -> {
                        Text(
                            text = stringResource(loaderDescription!!),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .align(Alignment.CenterHorizontally)
                                .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )

                        var boxWidth by remember { mutableIntStateOf(0) }
                        val itemWidthPx =
                            with(LocalDensity.current) { dimensionResource(id = R.dimen.spacingHuge).toPx() }
                        val infiniteTransition =
                            rememberInfiniteTransition(label = "horizontal loader")
                        val slideAnim by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = (boxWidth - itemWidthPx).coerceAtLeast(0f),
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 2000, easing = LinearEasing
                                ), repeatMode = RepeatMode.Restart
                            ),
                            label = "horizontal loader"
                        )

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = dimensionResource(id = R.dimen.spacingXl))
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .onGloballyPositioned { coordinates ->
                                boxWidth = coordinates.size.width // Capture the width
                            }) {
                            Box(
                                modifier = Modifier
                                    .width(width = dimensionResource(id = R.dimen.spacingHuge))
                                    .height(height = dimensionResource(id = R.dimen.spacingXxs))
                                    .offset(x = with(LocalDensity.current) { slideAnim.toDp() })
                                    .background(
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                                    .align(Alignment.CenterStart)
                            )
                        }
                    }

                    label == R.string.voice_prompt -> {
                        Text(
                            text = stringResource(R.string.the_voice_prompt_feature_is_available_exclusively_for_premium_members_please_get_a_premium_membership_to_access_this_feature_thank_you),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start
                        )

                        PrimaryButton(
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingXl)),
                            textResource = R.string.get_premium
                        ) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet.value = false
                                    if (hasPerformedHapticFeedback != null) {
                                        hasPerformedHapticFeedback.value = false
                                    }
                                }
                                navController.navigate("ManageSubscriptionScreen")
                            }
                        }
                    }

                    label == R.string.saved_to_your_device || label == R.string.saved_to_your_device_and_your_account -> {

                        ThumbnailImage(
                            canvasItemData = canvasItemData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(top = dimensionResource(id = R.dimen.spacingSm))
                                .clip(shape = MaterialTheme.shapes.extraSmall)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                        )

                        Text(
                            text = stringResource(R.string.share),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .align(Alignment.CenterHorizontally)
                                .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(alignment = Alignment.CenterHorizontally)
                                .padding(
                                    top = dimensionResource(
                                        id = R.dimen.spacingMd
                                    )
                                ), horizontalArrangement = Arrangement.spacedBy(
                                space = dimensionResource(
                                    id = R.dimen.spacingMd
                                )
                            )
                        ) {
                            val listItems = listOf(
                                listOf(R.drawable.tiktok, "com.zhiliaoapp.musically"),
                                listOf(R.drawable.facebook, "com.facebook.katana"),
                                listOf(R.drawable.snapchat, "com.snapchat.android"),
                                listOf(R.drawable.whatsapp, "com.whatsapp"),
                                listOf(R.drawable.instagram_2, "com.instagram.android")
                            )
                            repeat(listItems.size) { index ->
                                Image(modifier = Modifier
                                    .size(size = dimensionResource(id = R.dimen.spacingXl))
                                    .clickable(interactionSource = remember {
                                        MutableInteractionSource()
                                    }, indication = null) {
                                        if (filePath != null) {
                                            shareVideo(
                                                context = context,
                                                videoPath = filePath,
                                                packageName = listItems[index][1] as String
                                            ) { result, errorMessageResource ->
                                                if (!result && errorMessageResource != null) {
                                                    snackBarIsError.value = true
                                                    snackBarMessageResource.value =
                                                        errorMessageResource
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            ""
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    painter = painterResource(id = listItems[index][0] as Int),
                                    contentDescription = null
                                )
                            }

                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    top = dimensionResource(
                                        id = R.dimen.spacingSm
                                    )
                                )
                                .verticalScroll(state = rememberScrollState())
                                .imePadding()
                        ) {
                            Text(
                                text = stringResource(R.string.rate_your_experience),
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Start),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start
                            )

                            val emojiList = listOf(
                                R.drawable.pouting_face,
                                R.drawable.frowning_face,
                                R.drawable.neutral_face,
                                R.drawable.slightly_smiling_face,
                                R.drawable.smiling_face_with_heart_eyes
                            )
                            val tappedItemIndex = rememberSaveable {
                                mutableIntStateOf(-1)
                            }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(
                                        top = dimensionResource(
                                            id = R.dimen.spacingXs
                                        )
                                    ), horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                items(emojiList.size) { index: Int ->
                                    Box(modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            tappedItemIndex.intValue = index
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                        .background(
                                            color = if (tappedItemIndex.intValue == index) MaterialTheme.colorScheme.primary else Color(
                                                0xFF2A2D22
                                            )
                                        )
                                        .padding(all = dimensionResource(id = R.dimen.spacingMd))) {
                                        Image(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.Center),
                                            painter = painterResource(id = emojiList[index]),
                                            contentDescription = stringResource(
                                                R.string.initiate_trim
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = stringResource(R.string.your_comment_optional),
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Start)
                                    .padding(top = dimensionResource(id = R.dimen.spacingMd)),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start
                            )

                            CustomTextField(
                                modifier = Modifier.padding(
                                    top = dimensionResource(
                                        id = R.dimen.spacingXs
                                    )
                                ),
                                placeHolderResource = R.string.describe_your_experience,
                                height = 120.dp,
                                value = commentValue,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                placeHolderMaxLines = Int.MAX_VALUE,
                                singleLine = false
                            )

                            PrimaryButton(
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingMd)),
                                textResource = R.string.send,
                                enabled = tappedItemIndex.intValue != -1
                            ) {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet.value = false
                                        if (hasPerformedHapticFeedback != null) {
                                            hasPerformedHapticFeedback.value = false
                                        }
                                    }
                                    showLoader.value = true
                                    Firebase.firestore.collection("feedbacks").add(
                                        hashMapOf(
                                            "comment" to commentValue.value.text.trim(),
                                            "rating" to tappedItemIndex.intValue + 1
                                        )
                                    ).addOnCompleteListener { task ->
                                        showLoader.value = false
                                        if (task.isSuccessful) {
                                            navController.navigate(route = "FeedbackSentScreen")
                                        } else {
                                            snackBarIsError.value = true
                                            snackBarMessageResource.value = 0
                                            snackBarMessage.value =
                                                task.exception?.localizedMessage?.toString() ?: ""
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    ""
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
        if (hasPerformedHapticFeedback != null && !hasPerformedHapticFeedback.value) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            hasPerformedHapticFeedback.value = true
        }
    }
}