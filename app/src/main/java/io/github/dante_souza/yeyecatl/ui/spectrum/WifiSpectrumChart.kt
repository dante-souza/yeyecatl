package io.github.dante_souza.yeyecatl.ui.spectrum

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumCompleteness

@Composable
fun WifiSpectrumChart(
    observations: List<WifiScanObservation>,
    band: WifiBand,
    selectedBssid: String? = null,
    modifier: Modifier = Modifier
) {
    val viewport = remember(band) { WifiSpectrumViewports.forBand(band) }
    val visualObservations = remember(observations, band) {
        WifiSpectrumProjection.visualObservations(observations, band)
    }
    var previousRssiByKey by remember(band) {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }
    var targetRssiByKey by remember(band) {
        mutableStateOf(visualObservations.associate { it.colorKey to it.rssiDbm })
    }
    val snapshotTransition = remember(band) { Animatable(1f) }

    LaunchedEffect(visualObservations) {
        previousRssiByKey = targetRssiByKey
        targetRssiByKey = visualObservations.associate { it.colorKey to it.rssiDbm }
        snapshotTransition.snapTo(0f)
        snapshotTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = SPECTRUM_TRANSITION_MILLIS)
        )
    }

    if (visualObservations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No ${band.label()} access points observed in the latest scan.")
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

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(top = 12.dp)
            .semantics {
                contentDescription = buildString {
                    append("${band.label()} Wi-Fi spectrum chart with ")
                    append("${visualObservations.size} observed access points")
                    if (selectedBssid != null &&
                        visualObservations.any { it.bssid == selectedBssid }
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
        val smallLabelStyle = TextStyle(color = textColor, fontSize = 9.sp)

        for (rssi in -30 downTo -90 step 10) {
            val y = plotTop + WifiSpectrumProjection.rssiToY(rssi, viewport, plotHeight)
            drawLine(outlineColor, Offset(plotLeft, y), Offset(plotRight, y), strokeWidth = 1f)
            drawText(
                textMeasurer = textMeasurer,
                text = "$rssi",
                topLeft = Offset(0f, y - 8.dp.toPx()),
                style = labelStyle
            )
        }

        viewport.frequencyTicks.forEach { tick ->
            val x = plotLeft + WifiSpectrumProjection.frequencyToX(
                tick.frequencyMhz,
                viewport,
                plotWidth
            )
            drawLine(outlineColor, Offset(x, plotTop), Offset(x, plotBottom), strokeWidth = 1f)
            drawText(
                textMeasurer = textMeasurer,
                text = tick.label,
                topLeft = Offset(x - 12.dp.toPx(), plotBottom + 4.dp.toPx()),
                style = smallLabelStyle
            )
        }

        drawLine(axisColor, Offset(plotLeft, plotBottom), Offset(plotRight, plotBottom))
        drawLine(axisColor, Offset(plotLeft, plotTop), Offset(plotLeft, plotBottom))

        val orderedObservations = visualObservations.sortedBy {
            it.bssid == selectedBssid
        }

        orderedObservations.forEach { observation ->
            val baseColor = palette[colorIndex(observation.colorKey, palette.size)]
            val isSelected = selectedBssid != null && observation.bssid == selectedBssid
            val isDimmed = selectedBssid != null && !isSelected
            val color = if (isDimmed) baseColor.copy(alpha = 0.35f) else baseColor
            val strokeWidth = if (isSelected) 4.dp.toPx() else 2.dp.toPx()
            val fillAlpha = when {
                isSelected -> 0.34f
                isDimmed -> 0.08f
                else -> 0.22f
            }
            val previousRssi = previousRssiByKey[observation.colorKey]
                ?: observation.rssiDbm
            val animatedRssi = previousRssi +
                ((observation.rssiDbm - previousRssi) * snapshotTransition.value)
            val topY = plotTop + WifiSpectrumProjection.rssiToY(
                animatedRssi.toInt(),
                viewport,
                plotHeight
            )

            observation.footprint.segments.forEach { segment ->
                val leftX = plotLeft + WifiSpectrumProjection.frequencyToX(
                    segment.lowerFrequencyMhz,
                    viewport,
                    plotWidth
                )
                val rightX = plotLeft + WifiSpectrumProjection.frequencyToX(
                    segment.upperFrequencyMhz,
                    viewport,
                    plotWidth
                )
                val centerX = (leftX + rightX) / 2f

                when (observation.footprint.completeness) {
                    WifiSpectrumCompleteness.Complete -> {
                        val shoulder = (rightX - leftX) * 0.24f
                        val envelope = Path().apply {
                            moveTo(leftX, plotBottom)
                            lineTo(leftX + shoulder, topY)
                            lineTo(rightX - shoulder, topY)
                            lineTo(rightX, plotBottom)
                            close()
                        }
                        drawPath(envelope, baseColor.copy(alpha = fillAlpha))
                        drawPath(envelope, color, style = Stroke(width = strokeWidth))
                        drawText(
                            textMeasurer = textMeasurer,
                            text = observation.label,
                            topLeft = Offset(centerX - 24.dp.toPx(), topY - 18.dp.toPx()),
                            style = smallLabelStyle.copy(
                                color = if (isDimmed) {
                                    textColor.copy(alpha = 0.45f)
                                } else {
                                    textColor
                                }
                            )
                        )
                    }
                    WifiSpectrumCompleteness.Partial -> {
                        drawLine(
                            color = color,
                            start = Offset(centerX, topY),
                            end = Offset(centerX, plotBottom),
                            strokeWidth = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = observation.label,
                            topLeft = Offset(centerX + 4.dp.toPx(), topY),
                            style = smallLabelStyle.copy(
                                color = if (isDimmed) {
                                    textColor.copy(alpha = 0.45f)
                                } else {
                                    textColor
                                }
                            )
                        )
                    }
                    WifiSpectrumCompleteness.Unavailable,
                    WifiSpectrumCompleteness.Inconsistent -> Unit
                }
            }
        }
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

private const val SPECTRUM_TRANSITION_MILLIS = 450
