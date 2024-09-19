package com.splicr.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splicr.app.R

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    placeHolderResource: Int,
    hasFocus: MutableState<Boolean> = rememberSaveable {
        mutableStateOf(false)
    },
    focusRequester: FocusRequester = remember {
        FocusRequester()
    },
    value: MutableState<TextFieldValue> = rememberSaveable { mutableStateOf(TextFieldValue("")) },
    maxCharacter: Int? = null,
    leadingImageResource: Int? = null,
    leadingErrorImageResource: Int? = null,
    leadingImageContentDescriptionResource: Int? = null,
    trailingImageResource: Int? = null,
    trailingErrorImageResource: Int? = null,
    trailingImageContentDescriptionResource: Int? = null,
    isPasswordField: Boolean = false,
    errorMessageResource: MutableState<Int> = rememberSaveable { mutableIntStateOf(0) },
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Done
    ),
    height: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
    initialShape: Shape = MaterialTheme.shapes.small,
    readOnly: MutableState<Boolean> = rememberSaveable {
        mutableStateOf(false)
    },
    singleLine: Boolean = true,
    placeHolderMaxLines: Int = 1
) {
    Column(modifier = modifier) {
        val showPassword = remember {
            mutableStateOf(false)
        }
        val customTextSelectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary
        )
        val textHeight = remember { mutableIntStateOf(0) }
        val singleLineHeight = remember { mutableIntStateOf(0) }

        val shape = if (textHeight.intValue > singleLineHeight.intValue) {
            MaterialTheme.shapes.large
        } else {
            initialShape
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .clip(
                shape = shape
            )
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .border(
                width = 1.dp,
                color = if (errorMessageResource.value != 0) MaterialTheme.colorScheme.error else if (hasFocus.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
                focusRequester.requestFocus()
            }
            .padding(
                all = dimensionResource(id = R.dimen.spacingMd)
            )) {
            if (leadingImageResource != null) {
                Image(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.spacingXl))
                        .height(dimensionResource(id = R.dimen.spacingXl))
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = if (errorMessageResource.value != 0 && leadingErrorImageResource != null) leadingErrorImageResource else leadingImageResource),
                    contentDescription = if (leadingImageContentDescriptionResource == null) null else stringResource(
                        id = leadingImageContentDescriptionResource
                    )
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
            }

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                BasicTextField(value = value.value,
                    onValueChange = {
                        if (maxCharacter != null && it.text.length <= maxCharacter) {
                            value.value = it
                            errorMessageResource.value = 0
                        } else if (maxCharacter == null) {
                            value.value = it
                            errorMessageResource.value = 0
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (height != Dp.Unspecified) {
                                Modifier.height(height)
                            } else {
                                Modifier.heightIn(min = minHeight, max = maxHeight)
                            }
                        )
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .onFocusChanged { hasFocus.value = it.hasFocus }
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { coordinates ->
                            textHeight.intValue = coordinates.size.height
                            if (singleLineHeight.intValue == 0) {
                                singleLineHeight.intValue = textHeight.intValue
                            }
                        },
                    textStyle = TextStyle(
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = singleLine,
                    visualTransformation = if (isPasswordField) if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (value.value.text.isEmpty()) {
                            Text(
                                text = stringResource(id = placeHolderResource),
                                color = MaterialTheme.colorScheme.surface,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                maxLines = placeHolderMaxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    },
                    readOnly = readOnly.value
                )
            }

            if (trailingImageResource != null || isPasswordField) {
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
                Image(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.spacingXl))
                        .height(dimensionResource(id = R.dimen.spacingXl))
                        .align(Alignment.CenterVertically)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showPassword.value = !showPassword.value
                        },
                    painter = painterResource(
                        id = if (trailingImageResource != null) {
                            if (errorMessageResource.value != 0 && trailingErrorImageResource != null) {
                                trailingErrorImageResource
                            } else {
                                trailingImageResource
                            }
                        } else {
                            if (showPassword.value) {
                                if (errorMessageResource.value != 0) {
                                    R.drawable.error_show_password
                                } else {
                                    R.drawable.show_password
                                }
                            } else {
                                if (errorMessageResource.value != 0) {
                                    R.drawable.error_hide_password
                                } else {
                                    R.drawable.hide_password
                                }
                            }
                        }
                    ),
                    contentDescription = if (trailingImageContentDescriptionResource == null) null else stringResource(
                        id = trailingImageContentDescriptionResource
                    )
                )
            }
        }

        if (errorMessageResource.value != 0) {
            Text(
                text = stringResource(errorMessageResource.value),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = dimensionResource(id = R.dimen.spacingXxs)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}