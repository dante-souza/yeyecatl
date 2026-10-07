package io.github.dante_souza.yeyecatl.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory

@Composable
fun WifiSignalHistoryChart(
    history: WifiTemporalObservationHistory,
    observations: List<WifiScanObservation>,
    band: WifiBand,
    selectedBssid: String? = null,
    modifier: Modifier = Modifier
) {
    val series = remember(history, observations, band) {
        WifiSignalHistoryProjection.series(
            history = history,
            observations = observations,
            band = band
        )
    }
    val viewport = remember(series) {
        WifiSignalHistoryProjection.viewport(series)
    }

    if (series.isEmpty() || viewport == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No ${band.label()} signal history yet.")
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )
    val sampleCount = series.sumOf { it.points.size }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(top = 12.dp)
                .semantics {
                    contentDescription = buildString {
                        append("${band.label()} signal history chart with ")
                        append("${series.size} BSSID series and $sampleCount samples")
                        if (selectedBssid != null &&
                            series.any { it.bssid == selectedBssid }
                        ) {
                            append("; selected BSSID ")
                            append(selectedBssid)
                        }
                    }
                }
        ) {
            val plotLeft = 48.dp.toPx()
            val plotRight = size.width - 8.dp.toPx()
            val plotTop = 8.dp.toPx()
            val plotBottom = size.height - 34.dp.toPx()
            val plotWidth = plotRight - plotLeft
            val plotHeight = plotBottom - plotTop
            val labelStyle = TextStyle(color = textColor, fontSize = 10.sp)

            for (rssi in -30 downTo -90 step 10) {
                val y = plotTop + WifiSignalHistoryProjection.rssiToY(
                    rssiDbm = rssi,
                    viewport = viewport,
                    heightPx = plotHeight
                )
                drawLine(
                    color = outlineColor,
                    start = Offset(plotLeft, y),
                    end = Offset(plotRight, y),
                    strokeWidth = 1f
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$rssi",
                    topLeft = Offset(0f, y - 8.dp.toPx()),
                    style = labelStyle
                )
            }

            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val x = plotLeft + plotWidth * fraction
                val timestamp = viewport.minTimeMillis +
                    ((viewport.maxTimeMillis - viewport.minTimeMillis) * fraction).toLong()
                val delta = viewport.maxTimeMillis - timestamp
                val label = relativeTimeLabel(delta)

                drawLine(
                    color = outlineColor,
                    start = Offset(x, plotTop),
                    end = Offset(x, plotBottom),
                    strokeWidth = 1f
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(
                        x - if (fraction == 1f) 24.dp.toPx() else 12.dp.toPx(),
                        plotBottom + 4.dp.toPx()
                    ),
                    style = labelStyle
                )
            }

            drawLine(axisColor, Offset(plotLeft, plotBottom), Offset(plotRight, plotBottom))
            drawLine(axisColor, Offset(plotLeft, plotTop), Offset(plotLeft, plotBottom))

            val orderedSeries = series.sortedBy {
                it.bssid == selectedBssid
            }

            orderedSeries.forEach { visualSeries ->
                val baseColor = palette[colorIndex(visualSeries.colorKey, palette.size)]
                val isSelected = selectedBssid != null && visualSeries.bssid == selectedBssid
                val isDimmed = selectedBssid != null && !isSelected
                val color = if (isDimmed) baseColor.copy(alpha = 0.3f) else baseColor
                val strokeWidth = if (isSelected) 4.dp.toPx() else 2.dp.toPx()
                val pointRadius = if (isSelected) 5.dp.toPx() else 3.dp.toPx()
                val offsets = visualSeries.points.map { point ->
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
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )
                }

                offsets.forEach { point ->
                    drawCircle(
                        color = color,
                        radius = pointRadius,
                        center = point
                    )
                }
            }
        }

        if (series.size <= MAX_LEGEND_SERIES) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 8.dp, top = 8.dp)
                    .semantics {
                        contentDescription = "Signal history legend with ${series.size} series"
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                series.sortedBy { it.bssid == selectedBssid }.forEach { visualSeries ->
                    val baseColor = palette[colorIndex(visualSeries.colorKey, palette.size)]
                    val isSelected = selectedBssid != null && visualSeries.bssid == selectedBssid
                    val isDimmed = selectedBssid != null && !isSelected
                    val color = if (isDimmed) baseColor.copy(alpha = 0.35f) else baseColor
                    val latestRssi = visualSeries.points.last().rssiDbm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "●",
                            color = color,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = buildString {
                                if (isSelected) append("Selected · ")
                                append(visualSeries.label)
                                append(" · …")
                                append(visualSeries.bssid.takeLast(5))
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "$latestRssi dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Use Strongest 5 or Weakest 5 for a labeled signal-history view.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, end = 8.dp, top = 8.dp)
            )
        }
    }
}

private fun relativeTimeLabel(deltaMillis: Long): String {
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

private fun colorIndex(key: String, paletteSize: Int): Int =
    (key.hashCode() and Int.MAX_VALUE) % paletteSize

private fun WifiBand.label(): String =
    when (this) {
        WifiBand.Ghz2_4 -> "2.4 GHz"
        WifiBand.Ghz5 -> "5 GHz"
        WifiBand.Ghz6 -> "6 GHz"
        WifiBand.Ghz60 -> "60 GHz"
        WifiBand.Unknown -> "Unknown"
    }

private const val MAX_LEGEND_SERIES = 5
