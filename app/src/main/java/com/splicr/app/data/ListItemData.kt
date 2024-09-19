package com.splicr.app.data

data class ListItemData(
    val titleResource: Int? = null,
    val titleString: String = "",
    val isHighlightedText: Boolean = false,
    val isSignOutText: Boolean = false,
    val subText: String? = null,
    val showArrow: Boolean = true,
    val leadingIconResource: Int? = null
)