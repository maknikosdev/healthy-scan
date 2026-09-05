package com.healthyscan.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthyscan.app.R
import com.healthyscan.app.data.model.ScoreBand
import com.healthyscan.app.ui.theme.ScoreExcellent
import com.healthyscan.app.ui.theme.ScoreGood
import com.healthyscan.app.ui.theme.ScoreLow
import com.healthyscan.app.ui.theme.ScoreModerate
import com.healthyscan.app.ui.theme.ScoreVeryLow

fun colorForBand(band: ScoreBand): Color = when (band) {
    ScoreBand.EXCELLENT -> ScoreExcellent
    ScoreBand.GOOD -> ScoreGood
    ScoreBand.MODERATE -> ScoreModerate
    ScoreBand.LOW -> ScoreLow
    ScoreBand.VERY_LOW -> ScoreVeryLow
}

@Composable
fun labelForBand(band: ScoreBand): String = when (band) {
    ScoreBand.EXCELLENT -> stringResource(R.string.rating_excellent)
    ScoreBand.GOOD -> stringResource(R.string.rating_good)
    ScoreBand.MODERATE -> stringResource(R.string.rating_moderate)
    ScoreBand.LOW -> stringResource(R.string.rating_low)
    ScoreBand.VERY_LOW -> stringResource(R.string.rating_very_low)
}

/** Circular ring showing score/100, colored by band. Used on the product result & score detail screens. */
@Composable
fun ScoreRing(
    score: Int,
    band: ScoreBand,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    val color = colorForBand(band)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.09f
            drawArc(
                color = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }
        Text(
            text = "$score",
            fontSize = (size.value * 0.28f).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** Small pill badge, e.g. "72/100" or a colored dot + label for a nutrient level. */
@Composable
fun ScorePill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
