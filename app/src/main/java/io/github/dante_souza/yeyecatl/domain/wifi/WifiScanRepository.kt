package io.github.dante_souza.yeyecatl.domain.wifi

import kotlinx.coroutines.flow.StateFlow

interface WifiScanRepository {
    fun observeScanState(): StateFlow<WifiScanState>
    fun start()
    fun stop()
    fun requestScan()
}
