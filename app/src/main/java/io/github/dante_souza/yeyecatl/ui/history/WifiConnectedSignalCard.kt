package io.github.dante_souza.yeyecatl.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
    var scaleModeName by rememberSaveable {
        mutableStateOf(WifiConnectedSignalScaleMode.Fixed.name)
    }
    val scaleMode = WifiConnectedSignalScaleMode.valueOf(scaleModeName)

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

            ConnectedScaleSelector(
                selected = scaleMode,
                onSelected = { scaleModeName = it.name }
            )

            val active = state is WifiConnectedSignalState.Connected
            val inactiveMessage = when (state) {
                WifiConnectedSignalState.Idle -> "Waiting for connected signal"
                is WifiConnectedSignalState.Disconnected ->
                    "Wi-Fi disconnected · waiting for signal"
                is WifiConnectedSignalState.Unavailable -> "Waiting for measurable signal"
                is WifiConnectedSignalState.Connected -> null
            }

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
                }
            }

            WifiConnectedSignalHistoryChart(
                history = state.history,
                active = active,
                inactiveMessage = inactiveMessage,
                scaleMode = scaleMode,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = when (scaleMode) {
                    WifiConnectedSignalScaleMode.Fixed ->
                        "Fixed scale keeps room-to-room comparisons stable (-100 to -20 dBm)."
                    WifiConnectedSignalScaleMode.Auto ->
                        "Auto scale follows the recent signal range in 10 dB steps with hysteresis."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Each point is a WifiInfo RSSI read. Repeated values may reflect Android's own link-update cadence rather than a new radio measurement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectedScaleSelector(
    selected: WifiConnectedSignalScaleMode,
    onSelected: (WifiConnectedSignalScaleMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WifiConnectedSignalScaleMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = { Text(mode.name) }
            )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    active: Boolean,
    inactiveMessage: String?,
    scaleMode: WifiConnectedSignalScaleMode,
    modifier: Modifier = Modifier
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var autoRange by remember { mutableStateOf<WifiConnectedRssiRange?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(CONNECTED_CHART_CLOCK_TICK_MILLIS)
            nowMillis = System.currentTimeMillis()
        }
    }

    val rollingViewport = remember(nowMillis) {
        WifiSignalHistoryProjection.rollingViewport(nowMillis)
    }
    val visibleSamples = remember(history, rollingViewport) {
        history.samples.filter {
            it.observedAtMillis in rollingViewport.minTimeMillis..rollingViewport.maxTimeMillis
        }
    }
    val autoTarget = remember(history, nowMillis) {
        WifiConnectedSignalProjection.autoTargetRange(
            samples = history.samples,
            nowMillis = nowMillis
        )
    }

    LaunchedEffect(scaleMode, autoTarget, active) {
        when {
            scaleMode == WifiConnectedSignalScaleMode.Fixed -> autoRange = null
            active -> {
                autoRange = WifiConnectedSignalProjection.stabilizeAutoRange(
                    current = autoRange,
                    target = autoTarget
                )
            }
        }
    }

    val rssiRange = when (scaleMode) {
        WifiConnectedSignalScaleMode.Fixed -> WifiConnectedSignalProjection.fixedRange
        WifiConnectedSignalScaleMode.Auto -> autoRange ?: autoTarget
    }
    val viewport = WifiSignalHistoryViewport(
        minTimeMillis = rollingViewport.minTimeMillis,
        maxTimeMillis = rollingViewport.maxTimeMillis,
        minRssiDbm = rssiRange.minRssiDbm,
        maxRssiDbm = rssiRange.maxRssiDbm
    )

    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val oldSessionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        val plotLeft = 44.dp.toPx()
        val plotRight = size.width - 6.dp.toPx()
        val plotTop = 8.dp.toPx()
        val plotBottom = size.height - 28.dp.toPx()
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
        val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
        val labelStyle = TextStyle(color = textColor, fontSize = 9.sp)

        WifiConnectedSignalProjection.ticks(rssiRange).forEach { rssi ->
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

        if (active) {
            val latestSessionId = visibleSamples.lastOrNull()?.connectionSessionId
            WifiConnectedSignalProjection.sessionSegments(
                samples = visibleSamples,
                maxGapMillis = CONNECTED_LINE_GAP_MILLIS
            ).forEach { segment ->
                val offsets = segment.map { sample ->
                    Offset(
                        x = plotLeft + WifiSignalHistoryProjection.timeToX(
                            sample.observedAtMillis,
                            viewport,
                            plotWidth
                        ),
                        y = plotTop + WifiSignalHistoryProjection.rssiToY(
                            sample.rssiDbm,
                            viewport,
                            plotHeight
                        )
                    )
                }

                val segmentColor = if (
                    segment.lastOrNull()?.connectionSessionId == latestSessionId
                ) {
                    lineColor
                } else {
                    oldSessionColor
                }

                if (offsets.size > 1) {
                    val path = Path().apply {
                        moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, segmentColor, style = Stroke(width = 2.dp.toPx()))
                }
                offsets.forEach {
                    drawCircle(segmentColor, radius = 2.5.dp.toPx(), center = it)
                }
            }
        } else if (!inactiveMessage.isNullOrBlank()) {
            val messageLayout = textMeasurer.measure(
                text = inactiveMessage,
                style = TextStyle(color = textColor, fontSize = 10.sp)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = inactiveMessage,
                topLeft = Offset(
                    plotLeft + (plotWidth - messageLayout.size.width) / 2f,
                    plotTop + (plotHeight - messageLayout.size.height) / 2f
                ),
                style = TextStyle(color = textColor, fontSize = 10.sp)
            )
        }
    }
}

private const val CONNECTED_CHART_CLOCK_TICK_MILLIS = 1_000L
private const val CONNECTED_LINE_GAP_MILLIS = 3_000L
