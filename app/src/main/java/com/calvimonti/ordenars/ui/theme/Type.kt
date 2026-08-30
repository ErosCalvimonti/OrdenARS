package com.calvimonti.ordenars.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.calvimonti.ordenars.R

@OptIn(ExperimentalTextApi::class)
private fun pesoInter(peso: Int) = Font(
    resId = R.font.inter,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso))
)

val Inter = FontFamily(
    pesoInter(400),
    pesoInter(500),
    pesoInter(600),
    pesoInter(700),
)

private fun androidx.compose.ui.text.TextStyle.conInter() = copy(fontFamily = Inter)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    displayMedium = base.displayMedium.conInter(),
    displaySmall = base.displaySmall.conInter(),
    headlineLarge = base.headlineLarge.conInter(),
    headlineMedium = base.headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    headlineSmall = base.headlineSmall.conInter(),
    titleLarge = base.titleLarge.conInter(),
    titleMedium = base.titleMedium.conInter(),
    titleSmall = base.titleSmall.conInter(),
    bodyLarge = base.bodyLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = base.bodyMedium.conInter(),
    bodySmall = base.bodySmall.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = base.labelLarge.conInter(),
    labelMedium = base.labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = base.labelSmall.conInter(),
)
