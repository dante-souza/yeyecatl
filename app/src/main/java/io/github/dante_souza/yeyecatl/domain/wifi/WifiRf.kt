package io.github.dante_souza.yeyecatl.domain.wifi

enum class WifiBand {
    Ghz2_4,
    Ghz5,
    Ghz6,
    Ghz60,
    Unknown
}

enum class WifiChannelWidth {
    Mhz20,
    Mhz40,
    Mhz80,
    Mhz160,
    Mhz80Plus80,
    Mhz320,
    Unknown
}

enum class WifiStandard {
    Legacy,
    Ieee80211n,
    Ieee80211ac,
    Ieee80211ax,
    Ieee80211ad,
    Ieee80211be,
    Unknown
}

data class WifiRfCharacteristics(
    val band: WifiBand,
    val primaryChannel: Int?,
    val primaryFrequencyMhz: Int?,
    val channelWidth: WifiChannelWidth,
    val centerFrequency0Mhz: Int?,
    val centerFrequency1Mhz: Int?,
    val wifiStandard: WifiStandard
)

object WifiRfInterpreter {
    fun interpret(observation: WifiScanObservation): WifiRfCharacteristics =
        interpret(
            primaryFrequencyMhz = observation.frequencyMhz,
            channelWidth = observation.channelWidth,
            centerFrequency0Mhz = observation.centerFrequency0Mhz,
            centerFrequency1Mhz = observation.centerFrequency1Mhz,
            wifiStandard = observation.wifiStandard
        )

    fun interpret(
        primaryFrequencyMhz: Int?,
        channelWidth: WifiChannelWidth,
        centerFrequency0Mhz: Int?,
        centerFrequency1Mhz: Int?,
        wifiStandard: WifiStandard
    ): WifiRfCharacteristics =
        WifiRfCharacteristics(
            band = bandForFrequency(primaryFrequencyMhz),
            primaryChannel = channelForFrequency(primaryFrequencyMhz),
            primaryFrequencyMhz = primaryFrequencyMhz,
            channelWidth = channelWidth,
            centerFrequency0Mhz = centerFrequency0Mhz,
            centerFrequency1Mhz = centerFrequency1Mhz,
            wifiStandard = wifiStandard
        )

    fun bandForFrequency(frequencyMhz: Int?): WifiBand =
        when {
            frequencyMhz == null -> WifiBand.Unknown
            frequencyMhz in 2412..2484 -> WifiBand.Ghz2_4
            frequencyMhz in 5160..5885 -> WifiBand.Ghz5
            frequencyMhz == 5935 -> WifiBand.Ghz6
            frequencyMhz in 5955..7115 -> WifiBand.Ghz6
            frequencyMhz in 58320..70200 -> WifiBand.Ghz60
            else -> WifiBand.Unknown
        }

    fun channelForFrequency(frequencyMhz: Int?): Int? =
        when {
            frequencyMhz == null -> null
            frequencyMhz == 2484 -> 14
            frequencyMhz in 2412..2472 && (frequencyMhz - 2412) % 5 == 0 ->
                ((frequencyMhz - 2412) / 5) + 1
            frequencyMhz in 5160..5885 && (frequencyMhz - 5160) % 5 == 0 ->
                ((frequencyMhz - 5160) / 5) + 32
            frequencyMhz == 5935 -> 2
            frequencyMhz in 5955..7115 && (frequencyMhz - 5955) % 5 == 0 ->
                ((frequencyMhz - 5955) / 5) + 1
            else -> null
        }
}
