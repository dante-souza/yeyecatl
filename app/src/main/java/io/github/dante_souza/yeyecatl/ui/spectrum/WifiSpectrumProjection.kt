package io.github.dante_souza.yeyecatl.ui.spectrum

import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumCompleteness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumFootprint
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumGeometry

data class WifiSpectrumTick(
    val frequencyMhz: Int,
    val label: String
)

data class WifiSpectrumViewport(
    val band: WifiBand,
    val minFrequencyMhz: Int,
    val maxFrequencyMhz: Int,
    val minRssiDbm: Int = -90,
    val maxRssiDbm: Int = -30,
    val frequencyTicks: List<WifiSpectrumTick>
)

data class WifiSpectrumVisualObservation(
    val label: String,
    val bssid: String?,
    val rssiDbm: Int,
    val footprint: WifiSpectrumFootprint,
    val colorKey: String
)

object WifiSpectrumViewports {
    fun forBand(band: WifiBand): WifiSpectrumViewport =
        when (band) {
            WifiBand.Ghz2_4 -> WifiSpectrumViewport(
                band = band,
                minFrequencyMhz = 2400,
                maxFrequencyMhz = 2495,
                frequencyTicks = listOf(
                    WifiSpectrumTick(2412, "ch 1"),
                    WifiSpectrumTick(2437, "ch 6"),
                    WifiSpectrumTick(2462, "ch 11"),
                    WifiSpectrumTick(2472, "ch 13"),
                    WifiSpectrumTick(2484, "ch 14")
                )
            )
            WifiBand.Ghz5 -> WifiSpectrumViewport(
                band = band,
                minFrequencyMhz = 5160,
                maxFrequencyMhz = 5885,
                frequencyTicks = listOf(
                    WifiSpectrumTick(5180, "ch 36"),
                    WifiSpectrumTick(5500, "ch 100"),
                    WifiSpectrumTick(5745, "ch 149"),
                    WifiSpectrumTick(5825, "ch 165")
                )
            )
            WifiBand.Ghz6 -> WifiSpectrumViewport(
                band = band,
                minFrequencyMhz = 5925,
                maxFrequencyMhz = 7135,
                frequencyTicks = listOf(
                    WifiSpectrumTick(5935, "ch 2"),
                    WifiSpectrumTick(5955, "ch 1"),
                    WifiSpectrumTick(6115, "ch 33"),
                    WifiSpectrumTick(6435, "ch 97"),
                    WifiSpectrumTick(6755, "ch 161"),
                    WifiSpectrumTick(7115, "ch 233")
                )
            )
            WifiBand.Ghz60,
            WifiBand.Unknown -> WifiSpectrumViewport(
                band = band,
                minFrequencyMhz = 0,
                maxFrequencyMhz = 1,
                frequencyTicks = emptyList()
            )
        }
}

object WifiSpectrumProjection {
    fun frequencyToX(
        frequencyMhz: Int,
        viewport: WifiSpectrumViewport,
        widthPx: Float
    ): Float {
        val clamped = frequencyMhz.coerceIn(
            viewport.minFrequencyMhz,
            viewport.maxFrequencyMhz
        )
        val span = viewport.maxFrequencyMhz - viewport.minFrequencyMhz
        return ((clamped - viewport.minFrequencyMhz).toFloat() / span.toFloat()) * widthPx
    }

    fun rssiToY(
        rssiDbm: Int,
        viewport: WifiSpectrumViewport,
        heightPx: Float
    ): Float {
        val clamped = rssiDbm.coerceIn(viewport.minRssiDbm, viewport.maxRssiDbm)
        val span = viewport.maxRssiDbm - viewport.minRssiDbm
        val normalized = (clamped - viewport.minRssiDbm).toFloat() / span.toFloat()
        return heightPx * (1f - normalized)
    }

    fun visualObservations(
        observations: List<WifiScanObservation>,
        band: WifiBand
    ): List<WifiSpectrumVisualObservation> =
        observations.mapNotNull { observation ->
            val rf = WifiRfInterpreter.interpret(observation)
            val rssi = observation.rssiDbm ?: return@mapNotNull null
            if (rf.band != band) {
                return@mapNotNull null
            }

            val label = observation.ssid.displayText ?: if (observation.ssid.isHidden) {
                "<hidden>"
            } else {
                "<unavailable>"
            }
            WifiSpectrumVisualObservation(
                label = label,
                bssid = observation.bssid,
                rssiDbm = rssi,
                footprint = WifiSpectrumGeometry.footprint(rf),
                colorKey = observation.bssid ?: "$label:${rf.primaryFrequencyMhz}:$rssi"
            )
        }.sortedWith(
            compareBy<WifiSpectrumVisualObservation> { it.rssiDbm }
                .thenBy { it.colorKey }
        )

    fun drawableSegments(observation: WifiSpectrumVisualObservation): Int =
        when (observation.footprint.completeness) {
            WifiSpectrumCompleteness.Complete -> observation.footprint.segments.size
            WifiSpectrumCompleteness.Partial -> observation.footprint.segments.size
            WifiSpectrumCompleteness.Unavailable,
            WifiSpectrumCompleteness.Inconsistent -> 0
        }
}
