package io.github.dante_souza.yeyecatl.domain.wifi

import kotlinx.coroutines.flow.StateFlow

interface WifiScanRepository {
    fun observeScanState(): StateFlow<WifiScanState>
    fun observeTemporalHistory(): StateFlow<WifiTemporalObservationHistory>
    fun start()
    fun stop()
    fun requestScan()
}
