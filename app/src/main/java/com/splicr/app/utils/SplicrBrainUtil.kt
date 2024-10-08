package com.splicr.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.Content
import com.google.firebase.vertexai.type.TextPart
import com.google.firebase.vertexai.vertexAI
import com.splicr.app.R
import com.splicr.app.data.AspectRatioChoiceItemData
import com.splicr.app.data.PromptItemData
import com.splicr.app.data.TrimRangeData
import com.splicr.app.utils.MediaConfigurationUtil.checkIfVideoUrl
import com.splicr.app.utils.MediaConfigurationUtil.convertDimensionsToAspectRatio
import com.splicr.app.utils.MediaConfigurationUtil.downloadVideoToLocal
import com.splicr.app.utils.MediaConfigurationUtil.formatDuration
import com.splicr.app.utils.MediaConfigurationUtil.getAllVideoMetadata
import com.splicr.app.viewModel.PromptViewModel
import com.splicr.app.viewModel.SubscriptionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

object SplicrBrainUtil {

    fun converse(
        context: Context,
        promptValue: String,
        uploadFormatStringResource: Int,
        isProcessing: MutableState<Boolean>,
        scope: CoroutineScope,
        subscriptionStatus: State<SubscriptionStatus?>,
        navController: NavController,
        primaryColor: Color,
        promptViewModel: PromptViewModel,
        listState: LazyListState
    ) {
        isProcessing.value = true
        val item = PromptItemData(
            isLoading = true
        )
        promptViewModel.addListItem(item)

        scope.launch {
            listState.animateScrollToItem(
                promptViewModel.listItems.size - 1
            )
        }

        if (uploadFormatStringResource == R.string.url_upload) {
            if (promptViewModel.mutableVideoUriString == context.getString(R.string.empty) || promptViewModel.mutableVideoUriString == "") {
                scope.launch {
                    val result = checkIfVideoUrl(
                        videoUrl = promptValue
                    )
                    result.onSuccess {
                        val result2 = downloadVideoToLocal(
                            context = context, videoUrl = promptValue
                        )
                        result2.onSuccess {
                            promptViewModel.removeListItem(item)

                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                val thumbnail = getAllVideoMetadata(
                                    context = context, videoUri = Uri.parse(it.toString())
                                )?.thumbnail
                                val formattedDuration = getAllVideoMetadata(
                                    context = context, videoUri = Uri.parse(it.toString())
                                )?.duration?.let { it2 ->
                                    formatDuration(
                                        durationMillis = it2, shouldBeInFullFormat = false
                                    )
                                } ?: context.getString(R.string._0_00)

                                if (thumbnail != null) {
                                    promptViewModel.updateMutableVideoUriString(it.toString())
                                    promptViewModel.addListItem(
                                        PromptItemData(
                                            videoUriString = promptViewModel.mutableVideoUriString,
                                            thumbnailBitmap = thumbnail,
                                            duration = formattedDuration
                                        )
                                    )
                                    promptViewModel.addListItem(
                                        PromptItemData(
                                            message = AnnotatedString(text = context.getString(R.string.how_would_you_like_to_trim))
                                        )
                                    )
                                } else {
                                    promptViewModel.addListItem(
                                        PromptItemData(
                                            message = AnnotatedString(text = context.getString(R.string.oops_we_were_able_to_retrieve_the_media_from_the_url_but_something_seems_to_be_wrong_with_the_file_it_might_be_corrupted_or_in_an_unsupported_format_please_try_again_with_a_different_url_or_check_if_the_file_is_accessible_and_valid))
                                        )
                                    )
                                }
                            } else {
                                val tagAndAnnotation = "get_premium"
                                val annotatedString = buildAnnotatedString {
                                    append(context.getString(R.string.thanks_for_sharing_a_valid_media_url_however_it_seems_like_you_currently_are_not_on_any_of_our_premium_plans_get_one_of_our))
                                    withStyle(style = SpanStyle(color = primaryColor)) {
                                        pushStringAnnotation(
                                            tag = tagAndAnnotation, annotation = tagAndAnnotation
                                        )
                                        append(context.getString(R.string.premium_plans))
                                        pop()
                                    }
                                    append(context.getString(R.string.and_let_s_give_it_another_go))
                                }
                                promptViewModel.addListItem(PromptItemData(
                                    message = annotatedString
                                ) { offset ->
                                    annotatedString.getStringAnnotations(
                                        tag = tagAndAnnotation, start = offset, end = offset
                                    ).firstOrNull()?.let {
                                        navController.navigate("ManageSubscriptionScreen")
                                    }
                                })
                            }
                            val file = File(it?.path ?: "")
                            if (file.exists()) {
                                file.delete()
                            }
                        }.onFailure {
                            promptViewModel.removeListItem(item)

                            promptViewModel.addListItem(
                                PromptItemData(
                                    message = AnnotatedString(
                                        text = context.getString(
                                            R.string.i_m_sorry_but_i_couldn_t_retrieve_any_media_from_the_text_you_provided_please_try_again_with_a_valid_media_url
                                        )
                                    )
                                )
                            )
                        }
                    }.onFailure {
                        promptViewModel.removeListItem(item)

                        promptViewModel.addListItem(
                            PromptItemData(
                                message = AnnotatedString(
                                    text = context.getString(
                                        R.string.i_m_sorry_but_i_couldn_t_retrieve_any_media_from_the_text_you_provided_please_try_again_with_a_valid_media_url
                                    )
                                )
                            )
                        )
                    }
                    isProcessing.value = false
                    scope.launch {
                        listState.animateScrollToItem(
                            promptViewModel.listItems.size - 1
                        )
                    }
                }
            } else {
                askAi(
                    context = context,
                    promptValue = promptValue,
                    promptViewModel = promptViewModel,
                    item = item,
                    isProcessing = isProcessing,
                    scope = scope,
                    navController = navController,
                    primaryColor = primaryColor,
                    subscriptionStatus = subscriptionStatus,
                    listState = listState
                )
            }
        } else {
            askAi(
                context = context,
                promptValue = promptValue,
                promptViewModel = promptViewModel,
                item = item,
                isProcessing = isProcessing,
                scope = scope,
                navController = navController,
                primaryColor = primaryColor,
                subscriptionStatus = subscriptionStatus,
                listState = listState
            )
        }
    }

    private fun askAi(
        context: Context,
        promptValue: String,
        promptViewModel: PromptViewModel,
        item: PromptItemData,
        isProcessing: MutableState<Boolean>,
        scope: CoroutineScope,
        navController: NavController,
        primaryColor: Color,
        subscriptionStatus: State<SubscriptionStatus?>,
        listState: LazyListState
    ) {
        scope.launch {
            val duration =
                if (promptViewModel.mutableVideoUriString != context.getString(R.string.empty) && promptViewModel.mutableVideoUriString != "") getAllVideoMetadata(
                    context = context, videoUri = Uri.parse(promptViewModel.mutableVideoUriString)
                )?.duration else null

            when {
                duration == null -> {
                    promptViewModel.removeListItem(item)
                    promptViewModel.addListItem(
                        PromptItemData(
                            message = AnnotatedString(text = context.getString(R.string.oops_something_seems_to_be_wrong_with_the_file_it_might_be_corrupted_or_in_an_unsupported_format_please_try_again_with_a_different_url_or_check_if_the_file_is_accessible_and_valid))
                        )
                    )
                }

                subscriptionStatus.value == SubscriptionStatus.NONE && duration > 30000 -> {
                    promptViewModel.removeListItem(item)
                    val tagAndAnnotation = "get_premium"
                    val annotatedString = buildAnnotatedString {
                        append(context.getString(R.string.it_looks_like_you_re_trying_to_process_a_video_longer_than_30_seconds_this_feature_is_only_available_on_our_premium_plans_to_unlock_the_full_power_of_extended_video_editing_consider_getting_one_of_our))
                        withStyle(style = SpanStyle(color = primaryColor)) {
                            pushStringAnnotation(
                                tag = tagAndAnnotation, annotation = tagAndAnnotation
                            )
                            append(context.getString(R.string.premium_plans))
                            pop()
                        }
                        append(context.getString(R.string.we_d_love_to_help_you_create_amazing_content))
                    }
                    promptViewModel.addListItem(PromptItemData(
                        message = annotatedString
                    ) {
                        annotatedString.getStringAnnotations(
                            tag = tagAndAnnotation, start = it, end = it
                        ).firstOrNull()?.let {
                            navController.navigate("ManageSubscriptionScreen")
                        }
                    })
                }

                else -> {
                    val responseText = if (isInternetAvailable(context)) {
                        try {
                            Firebase.vertexAI.generativeModel("gemini-1.5-flash").generateContent(
                                Content(
                                    parts = listOf(
                                        TextPart(context.getString(R.string.ai_identity_instruction)),
                                        TextPart(context.getString(R.string.ai_format_instruction)),
                                        TextPart(
                                            context.getString(
                                                R.string.ai_duration_instruction, formatDuration(
                                                    durationMillis = duration
                                                )
                                            )
                                        ),
                                        TextPart(context.getString(R.string.ai_invalid_response_instruction)),
                                        TextPart(
                                            context.getString(
                                                R.string.ai_prompt_instruction, promptValue
                                            )
                                        )
                                    )
                                )
                            ).text
                        } catch (e: Exception) {
                            context.getString(R.string.i_m_having_a_bit_of_trouble_responding_to_your_request_right_now_i_ll_need_you_to_try_again_later)
                        }
                    } else {
                        context.getString(R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go)
                    }

                    promptViewModel.removeListItem(item)

                    if (responseText == null) {
                        promptViewModel.addListItem(
                            PromptItemData(
                                message = AnnotatedString(text = context.getString(R.string.oops_i_ran_into_an_issue_while_processing_your_request_let_s_try_that_again_how_would_you_like_me_to_trim))
                            )
                        )
                    } else {
                        val trimRanges = extractTrimRanges(responseText.trim(), duration)
                        if (trimRanges != null) {
                            promptViewModel.addListItem(
                                PromptItemData(
                                    message = AnnotatedString(text = responseText.trim())
                                )
                            )
                            if (trimRanges.isNotEmpty()) {
                                var width = getAllVideoMetadata(
                                    context = context,
                                    videoUri = Uri.parse(promptViewModel.mutableVideoUriString)
                                )?.width
                                var height = getAllVideoMetadata(
                                    context = context,
                                    videoUri = Uri.parse(promptViewModel.mutableVideoUriString)
                                )?.height
                                if (width == null || height == null || height == 0 || convertDimensionsToAspectRatio(
                                        context = context, width = width, height = height
                                    ) == context.getString(R.string.unknown_aspect_ratio)
                                ) {
                                    width = 1920
                                    height = 1080
                                }
                                val canvasChoiceList = listOf(
                                    AspectRatioChoiceItemData(
                                        typeStringResource = R.string.square,
                                        aspectRatioWidth = 1080,
                                        aspectRatioHeight = 1080,
                                        iconResourceList = listOf(
                                            R.drawable.tiktok,
                                            R.drawable.facebook,
                                            R.drawable.instagram_2
                                        )
                                    ), AspectRatioChoiceItemData(
                                        typeStringResource = R.string.vertical,
                                        aspectRatioWidth = 1080,
                                        aspectRatioHeight = 1920,
                                        iconResourceList = listOf(
                                            R.drawable.tiktok,
                                            R.drawable.facebook,
                                            R.drawable.instagram_2,
                                            R.drawable.youtube,
                                            R.drawable.snapchat
                                        )
                                    ), AspectRatioChoiceItemData(
                                        typeStringResource = R.string.horizontal,
                                        aspectRatioWidth = 1920,
                                        aspectRatioHeight = 1080,
                                        iconResourceList = listOf(R.drawable.youtube)
                                    ), AspectRatioChoiceItemData(
                                        typeStringResource = R.string.original,
                                        aspectRatioWidth = width,
                                        aspectRatioHeight = height,
                                        iconResourceList = null
                                    )
                                )

                                promptViewModel.addListItem(
                                    PromptItemData(
                                        videoUriString = promptViewModel.mutableVideoUriString,
                                        showCanvasOptions = true,
                                        trimRanges = trimRanges,
                                        isProcessing = isProcessing,
                                        viewModel = promptViewModel,
                                        canvasChoiceList = canvasChoiceList
                                    )
                                )
                            }
                        } else {
                            promptViewModel.addListItem(
                                PromptItemData(
                                    message = AnnotatedString(text = context.getString(R.string.your_request_is_invalid_because_it_exceeds_the_video_s_duration_please_adjust_the_times_and_try_again))
                                )
                            )
                        }
                    }
                }
            }
            isProcessing.value = false
            scope.launch {
                listState.animateScrollToItem(
                    promptViewModel.listItems.size - 1
                )
            }
        }
    }

    private fun extractTrimRanges(text: String, videoDurationMillis: Long): List<TrimRangeData>? {
        // Remove extra carriage returns and trim the text
        val cleanText = text.trim().replace("\r", "")

        // Split lines and filter out any empty lines
        val lines = cleanText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        // Validate the number of lines and format
        if (lines.size % 2 != 0 || lines.any { !it.startsWith("Start time: ") && !it.startsWith("End time: ") }) {
            return emptyList()
        }

        val trimRanges = mutableListOf<TrimRangeData>()

        for (i in lines.indices step 2) {
            val startTimeLine = lines[i]
            val endTimeLine = lines[i + 1]

            // Check if lines are properly formatted
            if (!startTimeLine.startsWith("Start time: ") || !endTimeLine.startsWith("End time: ")) {
                return emptyList()
            }

            val startTime = startTimeLine.removePrefix("Start time: ").trim()
            val endTime = endTimeLine.removePrefix("End time: ").trim()

            // Validate the time format (HH:MM:SS)
            if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime)) {
                return emptyList()
            }

            // Convert start and end times to milliseconds
            val startTimeMillis = convertTimeToMillis(startTime)
            val endTimeMillis = convertTimeToMillis(endTime)

            // Check if times exceed the video duration
            if (startTimeMillis > videoDurationMillis || endTimeMillis > videoDurationMillis) {
                return null
            }

            trimRanges.add(TrimRangeData(startTime = startTime, endTime = endTime))
        }

        return trimRanges
    }

    // Helper function to convert HH:MM:SS to milliseconds
    private fun convertTimeToMillis(time: String): Long {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        if (parts.size != 3) return 0L
        val hours = parts[0] * 60 * 60 * 1000L
        val minutes = parts[1] * 60 * 1000L
        val seconds = parts[2] * 1000L
        return hours + minutes + seconds
    }

    // Example validation function for HH:MM:SS format
    private fun isValidTimeFormat(time: String): Boolean {
        val regex = "^\\d{2}:\\d{2}:\\d{2}$".toRegex()
        return regex.matches(time)
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}