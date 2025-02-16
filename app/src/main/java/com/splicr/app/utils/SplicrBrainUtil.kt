package com.splicr.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import com.google.firebase.vertexai.type.Part
import com.google.firebase.vertexai.type.TextPart
import com.google.firebase.vertexai.vertexAI
import com.splicr.app.R
import com.splicr.app.data.PromptItemData
import com.splicr.app.data.TrimRangeData
import com.splicr.app.utils.MediaConfigurationUtil.checkIfVideoUrl
import com.splicr.app.utils.MediaConfigurationUtil.downloadMediaToLocal
import com.splicr.app.utils.MediaConfigurationUtil.formatDuration
import com.splicr.app.utils.MediaConfigurationUtil.getAllVideoMetadata
import com.splicr.app.viewModel.PromptViewModel
import com.splicr.app.viewModel.SubscriptionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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
        promptViewModel: PromptViewModel
    ) {
        isProcessing.value = true
        promptViewModel.addListItem(
            PromptItemData(
                isLoading = true
            )
        )

        generateWaitingMessage(
            context = context, promptViewModel = promptViewModel, scope = scope
        )

        if (uploadFormatStringResource == R.string.url_upload) {
            if (promptViewModel.mutableVideoUriString == context.getString(R.string.empty) || promptViewModel.mutableVideoUriString == "") {
                scope.launch {
                    val result = checkIfVideoUrl(
                        videoUrl = promptValue
                    )
                    result.onSuccess {
                        val result2 = downloadMediaToLocal(
                            context = context, mediaUrl = promptValue
                        )
                        result2.onSuccess {
                            promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
                            if (promptViewModel.waitingMessageJob?.isActive == true) {
                                promptViewModel.waitingMessageJob?.cancel()
                                promptViewModel.previousWaitingMessageResponse = ""
                            }

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
                                            message = AnnotatedString(text = context.getString(R.string.how_would_you_like_to_trim)),
                                        )
                                    )
                                } else {
                                    promptViewModel.addListItem(
                                        PromptItemData(
                                            message = AnnotatedString(text = context.getString(R.string.oops_we_were_able_to_retrieve_the_media_from_the_url_but_something_seems_to_be_wrong_with_the_file_it_might_be_corrupted_or_in_an_unsupported_format_please_try_again_with_a_different_url_or_check_if_the_file_is_accessible_and_valid)),
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
                        }.onFailure {
                            promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
                            if (promptViewModel.waitingMessageJob?.isActive == true) {
                                promptViewModel.waitingMessageJob?.cancel()
                                promptViewModel.previousWaitingMessageResponse = ""
                            }

                            promptViewModel.addListItem(
                                PromptItemData(
                                    message = AnnotatedString(
                                        text = if (isInternetAvailable(context = context)) {
                                            context.getString(
                                                R.string.i_m_sorry_but_i_couldn_t_retrieve_any_media_from_the_text_you_provided_please_try_again_with_a_valid_media_url
                                            )
                                        } else {
                                            context.getString(R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go)
                                        }
                                    )
                                )
                            )
                        }
                    }.onFailure {
                        promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
                        if (promptViewModel.waitingMessageJob?.isActive == true) {
                            promptViewModel.waitingMessageJob?.cancel()
                            promptViewModel.previousWaitingMessageResponse = ""
                        }

                        promptViewModel.addListItem(
                            PromptItemData(
                                message = AnnotatedString(
                                    text = if (isInternetAvailable(context = context)) {
                                        context.getString(
                                            R.string.i_m_sorry_but_i_couldn_t_retrieve_any_media_from_the_text_you_provided_please_try_again_with_a_valid_media_url
                                        )
                                    } else {
                                        context.getString(R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go)
                                    }
                                )
                            )
                        )
                    }
                    isProcessing.value = false
                }.invokeOnCompletion {
                    if (it is CancellationException && promptViewModel.listItems.lastOrNull()?.isLoading == true) {
                        promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
                        if (promptViewModel.waitingMessageJob?.isActive == true) {
                            promptViewModel.waitingMessageJob?.cancel()
                            promptViewModel.previousWaitingMessageResponse = ""
                        }
                        promptViewModel.addListItem(
                            PromptItemData(
                                message = AnnotatedString(
                                    text = context.getString(R.string.the_operation_was_interrupted_if_this_wasn_t_intentional_you_can_try_again)
                                )
                            )
                        )
                        promptViewModel.addListItem(
                            PromptItemData(
                                message = AnnotatedString(
                                    text = context.getString(R.string.please_input_the_url_of_the_video_you_would_like_to_trim_again)
                                )
                            )
                        )
                    }
                }
            } else {
                validateDuration(
                    context = context,
                    promptValue = promptValue,
                    promptViewModel = promptViewModel,
                    isProcessing = isProcessing,
                    scope = scope
                )
            }
        } else {
            validateDuration(
                context = context,
                promptValue = promptValue,
                promptViewModel = promptViewModel,
                isProcessing = isProcessing,
                scope = scope
            )
        }
    }

    fun generateWaitingMessage(
        context: Context, promptViewModel: PromptViewModel, scope: CoroutineScope
    ) {
        if (promptViewModel.waitingMessageJob?.isActive == true) return

        promptViewModel.waitingMessageJob = scope.launch(Dispatchers.IO) {
            delay(30_000)

            askAi(
                context = context, parts = listOf(
                    TextPart(context.getString(R.string.waiting_message_prompt)), TextPart(
                        context.getString(
                            R.string.these_are_your_previous_responses,
                            promptViewModel.previousWaitingMessageResponse
                        )
                    )
                )
            ).onSuccess {
                if (!it.isNullOrEmpty() && it != promptViewModel.previousWaitingMessageResponse && it !in listOf(
                        context.getString(R.string.i_m_having_a_bit_of_trouble_responding_to_your_request_right_now_i_ll_need_you_to_try_again_later),
                        context.getString(R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go)
                    )
                ) {
                    if (promptViewModel.previousWaitingMessageResponse.isNotEmpty()) {
                        promptViewModel.previousWaitingMessageResponse += "+$it"
                    } else {
                        promptViewModel.previousWaitingMessageResponse = it
                    }
                    withContext(Dispatchers.Main) {
                        if (promptViewModel.listItems.lastOrNull()?.isLoading == true) {
                            promptViewModel.updateMessageForItem(
                                position = promptViewModel.listItems.size - 1,
                                newMessage = it.trim()
                            )
                        }
                    }
                } else {
                    if (promptViewModel.listItems.lastOrNull()?.isLoading == true) {
                        generateWaitingMessage(
                            context = context, promptViewModel = promptViewModel, scope = scope
                        )
                    }
                }
            }.onFailure {
                if (promptViewModel.listItems.lastOrNull()?.isLoading == true) {
                    generateWaitingMessage(
                        context = context, promptViewModel = promptViewModel, scope = scope
                    )
                }
            }
        }
    }

    private fun validateDuration(
        context: Context,
        promptValue: String,
        promptViewModel: PromptViewModel,
        isProcessing: MutableState<Boolean>,
        scope: CoroutineScope
    ) {
        val duration =
            if (promptViewModel.mutableVideoUriString != context.getString(R.string.empty) && promptViewModel.mutableVideoUriString != "") getAllVideoMetadata(
                context = context, videoUri = Uri.parse(promptViewModel.mutableVideoUriString)
            )?.duration else null

        if (duration == null) {
            promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
            if (promptViewModel.waitingMessageJob?.isActive == true) {
                promptViewModel.waitingMessageJob?.cancel()
                promptViewModel.previousWaitingMessageResponse = ""
            }
            promptViewModel.addListItem(
                PromptItemData(
                    message = AnnotatedString(text = context.getString(R.string.oops_something_seems_to_be_wrong_with_the_file_it_might_be_corrupted_or_in_an_unsupported_format_please_try_again_with_a_different_url_or_check_if_the_file_is_accessible_and_valid)),
                )
            )
        } else {
            scope.launch {
                askAi(
                    context = context, parts = listOf(
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
                ).onSuccess {
                    displayAspectRatioOptions(
                        context = context,
                        promptViewModel = promptViewModel,
                        duration = duration,
                        isProcessing = isProcessing,
                        responseText = it
                    )
                }.onFailure {
                    displayAspectRatioOptions(
                        context = context,
                        promptViewModel = promptViewModel,
                        duration = duration,
                        isProcessing = isProcessing,
                        responseText = it.localizedMessage
                    )
                }
            }.invokeOnCompletion {
                if (it is CancellationException && promptViewModel.listItems.lastOrNull()?.isLoading == true) {
                    displayAspectRatioOptions(
                        context = context,
                        promptViewModel = promptViewModel,
                        duration = duration,
                        isProcessing = isProcessing,
                        responseText = context.getString(R.string.the_operation_was_interrupted_if_this_wasn_t_intentional_you_can_try_again)
                    )
                    displayAspectRatioOptions(
                        context = context,
                        promptViewModel = promptViewModel,
                        duration = duration,
                        isProcessing = isProcessing,
                        responseText = context.getString(R.string.how_would_you_like_to_trim)
                    )
                }
            }
        }
    }

    private suspend fun askAi(
        context: Context, parts: List<Part>
    ): Result<String?> {
        return if (isInternetAvailable(context)) {
            try {
                val responseText = Firebase.vertexAI.generativeModel("gemini-1.5-flash")
                    .generateContent(Content(parts = parts)).text
                Result.success(responseText)
            } catch (_: Exception) {
                Result.failure(Exception(context.getString(R.string.i_m_having_a_bit_of_trouble_responding_to_your_request_right_now_i_ll_need_you_to_try_again_later)))
            }
        } else {
            Result.failure(Exception(context.getString(R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go)))
        }
    }

    private fun displayAspectRatioOptions(
        context: Context,
        promptViewModel: PromptViewModel,
        duration: Long,
        isProcessing: MutableState<Boolean>,
        responseText: String?
    ) {

        promptViewModel.removeListItem(promptViewModel.listItems[promptViewModel.listItems.size - 1])
        if (promptViewModel.waitingMessageJob?.isActive == true) {
            promptViewModel.waitingMessageJob?.cancel()
            promptViewModel.previousWaitingMessageResponse = ""
        }

        if (responseText.isNullOrEmpty() || responseText == context.getString(R.string.i_m_having_a_bit_of_trouble_responding_to_your_request_right_now_i_ll_need_you_to_try_again_later) || responseText == context.getString(
                R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go
            )
        ) {
            promptViewModel.addListItem(
                PromptItemData(
                    message = AnnotatedString(
                        text = when (responseText) {
                            context.getString(R.string.i_m_having_a_bit_of_trouble_responding_to_your_request_right_now_i_ll_need_you_to_try_again_later), context.getString(
                                R.string.it_seems_i_can_t_reach_the_internet_at_the_moment_please_check_your_connection_and_give_it_another_go
                            ) -> responseText

                            else -> context.getString(
                                R.string.oops_i_ran_into_an_issue_while_processing_your_request_let_s_try_that_again_how_would_you_like_me_to_trim
                            )
                        }
                    ),
                )
            )
        } else {
            val trimRanges = extractTrimRanges(responseText.trim(), duration)
            if (trimRanges != null) {
                promptViewModel.addListItem(
                    PromptItemData(
                        message = AnnotatedString(text = responseText.trim()),
                        trimRanges = trimRanges
                    )
                )
            } else {
                promptViewModel.addListItem(
                    PromptItemData(
                        message = AnnotatedString(text = context.getString(R.string.your_request_is_invalid_because_it_exceeds_the_video_s_duration_please_adjust_the_times_and_try_again)),
                    )
                )
            }
        }

        isProcessing.value = false
    }

    private fun extractTrimRanges(text: String, videoDurationMillis: Long): List<TrimRangeData>? {
        val cleanText = text.trim().replace("\r", "")

        val lines = cleanText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        if (lines.size % 2 != 0 || lines.any { !it.startsWith("Start time: ") && !it.startsWith("End time: ") }) {
            return emptyList()
        }

        val trimRanges = mutableListOf<TrimRangeData>()

        for (i in lines.indices step 2) {
            val startTimeLine = lines[i]
            val endTimeLine = lines[i + 1]

            if (!startTimeLine.startsWith("Start time: ") || !endTimeLine.startsWith("End time: ")) {
                return emptyList()
            }

            val startTime = startTimeLine.removePrefix("Start time: ").trim()
            val endTime = endTimeLine.removePrefix("End time: ").trim()

            if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime)) {
                return emptyList()
            }

            val startTimeMillis = convertTimeToMillis(startTime)
            val endTimeMillis = convertTimeToMillis(endTime)

            if (startTimeMillis > videoDurationMillis || endTimeMillis > videoDurationMillis) {
                return null
            }

            trimRanges.add(TrimRangeData(startTime = startTime, endTime = endTime))
        }

        return trimRanges
    }

    private fun convertTimeToMillis(time: String): Long {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        if (parts.size != 3) return 0L
        val hours = parts[0] * 60 * 60 * 1000L
        val minutes = parts[1] * 60 * 1000L
        val seconds = parts[2] * 1000L
        return hours + minutes + seconds
    }

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