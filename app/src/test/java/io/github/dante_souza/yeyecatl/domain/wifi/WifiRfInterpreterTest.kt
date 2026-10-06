package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiRfInterpreterTest {
    @Test
    fun mapsTwoPointFourGhzChannels() {
        listOf(
            2412 to 1,
            2437 to 6,
            2462 to 11,
            2472 to 13,
            2484 to 14
        ).forEach { (frequency, channel) ->
            assertEquals(WifiBand.Ghz2_4, WifiRfInterpreter.bandForFrequency(frequency))
            assertEquals(channel, WifiRfInterpreter.channelForFrequency(frequency))
        }
    }

    @Test
    fun mapsFiveGhzChannels() {
        listOf(
            5180 to 36,
            5200 to 40,
            5220 to 44,
            5240 to 48,
            5500 to 100,
            5745 to 149,
            5765 to 153,
            5785 to 157,
            5805 to 161,
            5825 to 165
        ).forEach { (frequency, channel) ->
            assertEquals(WifiBand.Ghz5, WifiRfInterpreter.bandForFrequency(frequency))
            assertEquals(channel, WifiRfInterpreter.channelForFrequency(frequency))
        }
    }

    @Test
    fun mapsSixGhzChannels() {
        listOf(
            5935 to 2,
            5955 to 1,
            5975 to 5,
            6115 to 33,
            7115 to 233
        ).forEach { (frequency, channel) ->
            assertEquals(WifiBand.Ghz6, WifiRfInterpreter.bandForFrequency(frequency))
            assertEquals(channel, WifiRfInterpreter.channelForFrequency(frequency))
        }
    }

    @Test
    fun recognizesSixtyGhzBandWithoutChannelInterpretation() {
        assertEquals(WifiBand.Ghz60, WifiRfInterpreter.bandForFrequency(58320))
        assertNull(WifiRfInterpreter.channelForFrequency(58320))
    }

    @Test
    fun invalidFrequenciesDoNotProduceChannels() {
        listOf(
            null,
            0,
            -1,
            2413,
            2483,
            5000,
            5181,
            5940,
            5956,
            7200
        ).forEach { frequency ->
            assertNull(WifiRfInterpreter.channelForFrequency(frequency))
        }
    }

    @Test
    fun invalidFrequenciesHaveUnknownBandWhenOutsideKnownRanges() {
        listOf(null, 0, -1, 5000, 7200).forEach { frequency ->
            assertEquals(WifiBand.Unknown, WifiRfInterpreter.bandForFrequency(frequency))
        }
    }

    @Test
    fun preservesPrimaryAndCenterFrequencies() {
        val rf = WifiRfInterpreter.interpret(
            primaryFrequencyMhz = 5210,
            channelWidth = WifiChannelWidth.Mhz80Plus80,
            centerFrequency0Mhz = 5290,
            centerFrequency1Mhz = 5530,
            wifiStandard = WifiStandard.Ieee80211ax
        )

        assertEquals(5210, rf.primaryFrequencyMhz)
        assertEquals(5290, rf.centerFrequency0Mhz)
        assertEquals(5530, rf.centerFrequency1Mhz)
        assertEquals(WifiChannelWidth.Mhz80Plus80, rf.channelWidth)
        assertEquals(WifiStandard.Ieee80211ax, rf.wifiStandard)
    }
}
