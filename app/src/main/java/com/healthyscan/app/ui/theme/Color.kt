package com.healthyscan.app.ui.theme

import androidx.compose.ui.graphics.Color

// The two brand colors. They swap roles between light and dark mode:
// Light mode -> cream background, deep green as the primary/accent color.
// Dark mode  -> deep green background, cream as the primary/accent color.
val BrandDeepGreen = Color(0xFF064E3B)
val BrandCream = Color(0xFFF8E7C9)

val GreenVariantLight = Color(0xFF0B6B52)
val GreenVariantDark = Color(0xFF043A2C)
val CreamVariantLight = Color(0xFFFBF1DD)
val CreamVariantDark = Color(0xFFEFD9AC)

// Health score status colors (constant across themes so a red score always reads as red)
val ScoreExcellent = Color(0xFF1E8E5A)
val ScoreGood = Color(0xFF4CAF50)
val ScoreModerate = Color(0xFFF5A623)
val ScoreLow = Color(0xFFE8791A)
val ScoreVeryLow = Color(0xFFD9483B)
