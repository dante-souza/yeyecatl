package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiScanObservationMapperTest {
    @Test
    fun mapsRawAndroidFieldsWithoutAnalysis() {
        val observation = WifiScanObservationMapper.fromRaw(
            RawWifiScanObservation(
                ssidDisplayText = "whanganui",
                ssidRawBytes = byteArrayOf(0x77, 0x69),
                bssid = "00:11:22:33:44:55",
                rssiDbm = -42,
                frequencyMhz = 2412,
                capabilities = "[WPA2-PSK-CCMP][ESS]",
                platformTimestampMicros = 123456L
            )
        )

        assertEquals("whanganui", observation.ssid.displayText)
        assertArrayEquals(byteArrayOf(0x77, 0x69), observation.ssid.rawBytes?.bytes)
        assertFalse(observation.ssid.isHidden)
        assertEquals("00:11:22:33:44:55", observation.bssid)
        assertEquals(-42, observation.rssiDbm)
        assertEquals(2412, observation.frequencyMhz)
        assertEquals("[WPA2-PSK-CCMP][ESS]", observation.capabilities)
        assertEquals(123456L, observation.platformTimestampMicros)
    }

    @Test
    fun treatsBlankSsidAsHiddenWithoutFailing() {
        val observation = WifiScanObservationMapper.fromRaw(
            RawWifiScanObservation(
                ssidDisplayText = "",
                ssidRawBytes = null,
                bssid = null,
                rssiDbm = null,
                frequencyMhz = null,
                capabilities = null,
                platformTimestampMicros = null
            )
        )

        assertNull(observation.ssid.displayText)
        assertTrue(observation.ssid.isHidden)
    }

    @Test
    fun treatsUnknownSsidMarkerAsUnavailable() {
        val observation = WifiScanObservationMapper.fromRaw(
            RawWifiScanObservation(
                ssidDisplayText = "<unknown ssid>",
                ssidRawBytes = null,
                bssid = "00:11:22:33:44:55",
                rssiDbm = -60,
                frequencyMhz = 5180,
                capabilities = "",
                platformTimestampMicros = 0L
            )
        )

        assertNull(observation.ssid.displayText)
        assertTrue(observation.ssid.isHidden)
        assertNull(observation.capabilities)
        assertNull(observation.platformTimestampMicros)
    }
}
