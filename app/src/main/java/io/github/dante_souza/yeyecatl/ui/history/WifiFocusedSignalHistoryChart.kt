package io.github.dante_souza.yeyecatl.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample

@Composable
fun WifiFocusedSignalHistoryChart(
    samples: List<WifiSignalSample>,
    bssid: String,
    modifier: Modifier = Modifier
) {
    val points = remember(samples) {
        WifiFocusedSignalHistoryProjection.points(samples)
    }
    val viewport = remember(points) {
        WifiFocusedSignalHistoryProjection.viewport(points)
    }

    if (points.isEmpty() || viewport == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No retained RSSI history yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .semantics {
                contentDescription =
                    "Focused RSSI history for ${bssid} with ${points.size} displayed samples"
            }
    ) {
        val plotLeft = 42.dp.toPx()
        val plotRight = size.width - 8.dp.toPx()
        val plotTop = 8.dp.toPx()
        val plotBottom = size.height - 28.dp.toPx()
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
        val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
        val labelStyle = TextStyle(
            color = textColor,
            fontSize = 9.sp
        )

        for (rssi in -30 downTo -90 step 20) {
            val y = plotTop + WifiSignalHistoryProjection.rssiToY(
                rssiDbm = rssi,
                viewport = viewport,
                heightPx = plotHeight
            )
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "${rssi}",
                topLeft = Offset(0f, y - 7.dp.toPx()),
                style = labelStyle
            )
        }

        listOf(0f, 0.5f, 1f).forEach { fraction ->
            val x = plotLeft + plotWidth * fraction
            val timestamp = viewport.minTimeMillis +
                ((viewport.maxTimeMillis - viewport.minTimeMillis) * fraction).toLong()
            val deltaMillis = viewport.maxTimeMillis - timestamp
            val label = focusedRelativeTimeLabel(deltaMillis)

            drawLine(
                color = gridColor,
                start = Offset(x, plotTop),
                end = Offset(x, plotBottom)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(
                    x - if (fraction == 1f) 20.dp.toPx() else 10.dp.toPx(),
                    plotBottom + 3.dp.toPx()
                ),
                style = labelStyle
            )
        }

        drawLine(
            color = axisColor,
            start = Offset(plotLeft, plotBottom),
            end = Offset(plotRight, plotBottom)
        )
        drawLine(
            color = axisColor,
            start = Offset(plotLeft, plotTop),
            end = Offset(plotLeft, plotBottom)
        )

        val offsets = points.map { point ->
            Offset(
                x = plotLeft + WifiSignalHistoryProjection.timeToX(
                    observedAtMillis = point.observedAtMillis,
                    viewport = viewport,
                    widthPx = plotWidth
                ),
                y = plotTop + WifiSignalHistoryProjection.rssiToY(
                    rssiDbm = point.rssiDbm,
                    viewport = viewport,
                    heightPx = plotHeight
                )
            )
        }

        if (offsets.size > 1) {
            val path = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        offsets.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}

private fun focusedRelativeTimeLabel(deltaMillis: Long): String {
    if (deltaMillis <= 0L) {
        return "now"
    }

    val seconds = deltaMillis / 1_000L
    return if (seconds < 60L) {
        "-${seconds}s"
    } else {
        "-${seconds / 60L}m"
    }
}
