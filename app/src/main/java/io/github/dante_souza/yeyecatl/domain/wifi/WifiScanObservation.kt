package io.github.dante_souza.yeyecatl.domain.wifi

data class ObservedSsid(
    val displayText: String?,
    val rawBytes: SsidBytes?,
    val isHidden: Boolean
)

data class WifiScanObservation(
    val ssid: ObservedSsid,
    val bssid: String?,
    val rssiDbm: Int?,
    val frequencyMhz: Int?,
    val capabilities: String?,
    val platformTimestampMicros: Long?
)

data class RawWifiScanObservation(
    val ssidDisplayText: String?,
    val ssidRawBytes: ByteArray?,
    val bssid: String?,
    val rssiDbm: Int?,
    val frequencyMhz: Int?,
    val capabilities: String?,
    val platformTimestampMicros: Long?
)

object WifiScanObservationMapper {
    fun fromRaw(raw: RawWifiScanObservation): WifiScanObservation {
        val displayText = raw.ssidDisplayText
            ?.takeUnless { it.isBlank() || it == UNKNOWN_SSID }

        return WifiScanObservation(
            ssid = ObservedSsid(
                displayText = displayText,
                rawBytes = raw.ssidRawBytes?.let(::SsidBytes),
                isHidden = displayText == null &&
                    (raw.ssidRawBytes == null || raw.ssidRawBytes.isEmpty())
            ),
            bssid = raw.bssid?.takeUnless { it.isBlank() },
            rssiDbm = raw.rssiDbm,
            frequencyMhz = raw.frequencyMhz?.takeIf { it > 0 },
            capabilities = raw.capabilities?.takeUnless { it.isBlank() },
            platformTimestampMicros = raw.platformTimestampMicros?.takeIf { it > 0L }
        )
    }

    private const val UNKNOWN_SSID = "<unknown ssid>"
}
