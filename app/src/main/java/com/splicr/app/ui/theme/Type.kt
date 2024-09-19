package com.splicr.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.splicr.app.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val Typography = Typography(
    labelSmall = TextStyle(
        fontFamily = FontFamily(
            Font(
                googleFont = GoogleFont("Montserrat"), fontProvider = provider
            )
        ), fontStyle = FontStyle.Normal, fontSize = 13.sp
    ), labelMedium = TextStyle(
        fontFamily = FontFamily(
            Font(
                googleFont = GoogleFont("Lato"), fontProvider = provider
            )
        ), fontStyle = FontStyle.Normal, fontSize = 16.sp
    ), labelLarge = TextStyle(
        fontFamily = FontFamily(
            Font(
                googleFont = GoogleFont("Montserrat"), fontProvider = provider
            )
        ), fontStyle = FontStyle.Normal, fontSize = 16.sp
    ), titleSmall = TextStyle(
        fontFamily = FontFamily(
            Font(
                googleFont = GoogleFont("Montserrat"), fontProvider = provider
            )
        ), fontStyle = FontStyle.Normal, fontSize = 22.sp
    ), bodyLarge = TextStyle(
        fontFamily = FontFamily(
            Font(
                googleFont = GoogleFont("Montserrat"), fontProvider = provider
            )
        ), fontStyle = FontStyle.Normal, fontSize = 28.sp
    )
)