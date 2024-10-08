package com.splicr.app.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomTextField
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.MediaConfigurationUtil.convertDimensionsToAspectRatio
import com.splicr.app.viewModel.NameYourProjectViewModel

@SuppressLint("DiscouragedApi")
@Composable
fun NameYourProjectScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    canvasItemData: CanvasItemData = CanvasItemData(),
    videoUriString: String = "",
    currentPosition: Long = 0,
    isPlaying: Boolean = false,
    nameYourProjectViewModel: NameYourProjectViewModel = viewModel()
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 72.dp, start = dimensionResource(
                            id = R.dimen.spacingXl
                        ), end = dimensionResource(
                            id = R.dimen.spacingXl
                        ), bottom = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    )
                    .imePadding()
            ) {
                CustomTopNavigationBar(modifier = Modifier.fillMaxWidth(),
                    startImageResource = R.drawable.back,
                    startStringResource = R.string.go_back,
                    startOnClick = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "currentPosition", currentPosition
                        )
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "isPlaying", isPlaying
                        )
                        navController.popBackStack()
                    },
                    centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) },
                    endStringResource = R.string.skip,
                    endOnClick = {
                        canvasItemData.title = context.getString(R.string.unnamed)
                        navController.navigate(
                            route = "MediaSplicedScreen/${
                                Uri.encode(
                                    Gson().toJson(
                                        canvasItemData
                                    )
                                )
                            }/${
                                Uri.encode(
                                    videoUriString
                                )
                            }/NameYourProjectScreen/${
                                currentPosition
                            }/${
                                isPlaying
                            }"
                        )
                    })

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f)
                        .verticalScroll(state = rememberScrollState())
                ) {
                    val topPadding = 28.dp

                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = topPadding),
                        text = stringResource(R.string.name_your_project),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Start)
                            .padding(top = topPadding),
                        text = stringResource(R.string.selected_aspect_ratio),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = dimensionResource(id = R.dimen.spacingXs))
                            .clip(shape = MaterialTheme.shapes.medium)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium
                            )
                            .background(color = Color.Transparent)
                            .padding(
                                all = dimensionResource(id = R.dimen.spacingMd)
                            ), verticalAlignment = Alignment.CenterVertically

                    ) {
                        val aspectRatioTypeResource = context.resources.getIdentifier(
                            canvasItemData.aspectRatioTypeKey.lowercase(),
                            "string",
                            context.packageName
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight()
                                .padding(
                                    end = if (aspectRatioTypeResource != R.string.original) dimensionResource(
                                        id = R.dimen.spacingMd
                                    ) else 0.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.spacingXs))
                        ) {
                            Text(
                                modifier = Modifier.wrapContentSize(),
                                text = stringResource(id = aspectRatioTypeResource),
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
                                    width = canvasItemData.aspectRatioWidth,
                                    height = canvasItemData.aspectRatioHeight
                                ),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start
                            )
                        }

                        if (aspectRatioTypeResource != R.string.original) {
                            Row(
                                modifier = Modifier.wrapContentSize(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = dimensionResource(
                                        id = R.dimen.spacingXs
                                    )
                                )
                            ) {
                                val listItems = when (aspectRatioTypeResource) {
                                    R.string.square -> {
                                        listOf(
                                            R.drawable.tiktok,
                                            R.drawable.facebook,
                                            R.drawable.instagram_2
                                        )
                                    }

                                    R.string.vertical -> {
                                        listOf(
                                            R.drawable.tiktok,
                                            R.drawable.facebook,
                                            R.drawable.instagram_2,
                                            R.drawable.youtube,
                                            R.drawable.snapchat
                                        )
                                    }

                                    else -> {
                                        listOf(R.drawable.youtube)
                                    }
                                }

                                repeat(listItems.size) { index ->
                                    Image(
                                        modifier = Modifier.size(
                                            size = dimensionResource(
                                                id = R.dimen.spacingMd
                                            )
                                        ),
                                        painter = painterResource(id = listItems[index]),
                                        contentDescription = null
                                    )
                                }

                            }
                        }
                    }
                }

                CustomTextField(
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.spacingMd)
                    ),
                    placeHolderResource = R.string.enter_project_name_here,
                    value = nameYourProjectViewModel.projectNameValue,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
                )

                PrimaryButton(
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.spacingMd)
                    ),
                    textResource = R.string.Continue,
                    enabled = nameYourProjectViewModel.projectNameValue.value.text.isNotEmpty()
                ) {
                    canvasItemData.title =
                        nameYourProjectViewModel.projectNameValue.value.text.trim()
                    navController.navigate(
                        route = "MediaSplicedScreen/${
                            Uri.encode(
                                Gson().toJson(
                                    canvasItemData
                                )
                            )
                        }/${
                            Uri.encode(
                                videoUriString
                            )
                        }/NameYourProjectScreen/${
                            currentPosition
                        }/${
                            isPlaying
                        }"
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun NameYourProjectScreenPreview() {
    NameYourProjectScreen(navController = rememberNavController())
}