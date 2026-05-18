package com.example.orthodoxapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleDonutChart(
    data: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    centerSubText: String = ""
) {
    val total = data.sum()
    val proportions = data.map { it / total }
    val sweepAngles = proportions.map { it * 360f }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            for (i in sweepAngles.indices) {
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[i] - 2f, // -2f for a small gap between segments
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
                startAngle += sweepAngles[i]
            }
        }
        if (centerText.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerText, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                if (centerSubText.isNotEmpty()) {
                    Text(centerSubText, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Blue,
    barColors: List<Color>? = null
) {
    val maxData = if (data.isEmpty() || data.maxOrNull() == 0f) 1f else data.maxOrNull()!!

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            val colorToUse = barColors?.getOrNull(index) ?: barColor
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(value / maxData * 0.85f)
                        .background(colorToUse, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
