package io.github.dante_souza.yeyecatl.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalHistory
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalState
import kotlinx.coroutines.delay

@Composable
fun WifiConnectedSignalCard(
    state: WifiConnectedSignalState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Connected AP RSSI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Independent 1 s Android connected-link reads; no nearby Wi-Fi scan is requested.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when (state) {
                WifiConnectedSignalState.Idle -> {
                    Text(
                        text = "Connected-link sampling is starting.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is WifiConnectedSignalState.Disconnected -> {
                    Text(
                        text = "No active Wi-Fi connection.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is WifiConnectedSignalState.Unavailable -> {
                    Text(
                        text = state.reason,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is WifiConnectedSignalState.Connected -> {
                    val sample = state.sample
                    val ssid = sample.ssid.displayText ?: "<hidden/unavailable>"
                    ConnectedValueRow("SSID", ssid)
                    ConnectedValueRow("BSSID", sample.bssid)
                    ConnectedValueRow("Current link RSSI", "${sample.rssiDbm} dBm")
                    sample.frequencyMhz?.let {
                        ConnectedValueRow("Frequency", "$it MHz")
                    }
                    ConnectedValueRow("Retained link reads", state.history.samples.size.toString())
                    WifiConnectedSignalHistoryChart(
                        history = state.history,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Each point is a WifiInfo RSSI read. Repeated values may reflect Android's own link-update cadence rather than a new radio measurement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
private fun WifiConnectedSignalHistoryChart(
    history: WifiConnectedSignalHistory,
    modifier: Modifier = Modifier
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(CONNECTED_CHART_CLOCK_TICK_MILLIS)
            nowMillis = System.currentTimeMillis()
        }
    }

    val viewport = remember(nowMillis) {
        WifiSignalHistoryProjection.rollingViewport(nowMillis)
    }
    val points = remember(history, viewport) {
        WifiSignalHistoryProjection.visiblePoints(
            points = history.samples.map {
                WifiSignalHistoryPoint(
                    observedAtMillis = it.observedAtMillis,
                    rssiDbm = it.rssiDbm
                )
            },
            viewport = viewport
        )
    }

    if (points.isEmpty()) {
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
            .height(180.dp)
    ) {
        val plotLeft = 44.dp.toPx()
        val plotRight = size.width - 6.dp.toPx()
        val plotTop = 8.dp.toPx()
        val plotBottom = size.height - 28.dp.toPx()
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
        val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
        val labelStyle = TextStyle(color = textColor, fontSize = 9.sp)

        for (rssi in -30 downTo -90 step 20) {
            val y = plotTop + WifiSignalHistoryProjection.rssiToY(
                rssiDbm = rssi,
                viewport = viewport,
                heightPx = plotHeight
            )
            drawLine(gridColor, Offset(plotLeft, y), Offset(plotRight, y))
            drawText(
                textMeasurer = textMeasurer,
                text = "$rssi",
                topLeft = Offset(0f, y - 7.dp.toPx()),
                style = labelStyle
            )
        }

        listOf(0f, 0.5f, 1f).forEach { fraction ->
            val x = plotLeft + plotWidth * fraction
            drawLine(gridColor, Offset(x, plotTop), Offset(x, plotBottom))
            val label = when (fraction) {
                0f -> "-2m"
                0.5f -> "-1m"
                else -> "now"
            }
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(
                    x - if (fraction == 1f) 22.dp.toPx() else 10.dp.toPx(),
                    plotBottom + 3.dp.toPx()
                ),
                style = labelStyle
            )
        }

        drawLine(axisColor, Offset(plotLeft, plotBottom), Offset(plotRight, plotBottom))
        drawLine(axisColor, Offset(plotLeft, plotTop), Offset(plotLeft, plotBottom))

        WifiSignalHistoryProjection.contiguousSegments(
            points = points,
            maxGapMillis = CONNECTED_LINE_GAP_MILLIS
        ).forEach { segment ->
            val offsets = segment.map { point ->
                Offset(
                    x = plotLeft + WifiSignalHistoryProjection.timeToX(
                        point.observedAtMillis,
                        viewport,
                        plotWidth
                    ),
                    y = plotTop + WifiSignalHistoryProjection.rssiToY(
                        point.rssiDbm,
                        viewport,
                        plotHeight
                    )
                )
            }

            if (offsets.size > 1) {
                val path = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
            }
            offsets.forEach {
                drawCircle(lineColor, radius = 2.5.dp.toPx(), center = it)
            }
        }
    }
}

private const val CONNECTED_CHART_CLOCK_TICK_MILLIS = 1_000L
private const val CONNECTED_LINE_GAP_MILLIS = 3_000L
