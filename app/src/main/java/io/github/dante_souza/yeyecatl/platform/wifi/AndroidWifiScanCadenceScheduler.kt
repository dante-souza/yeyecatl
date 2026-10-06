package io.github.dante_souza.yeyecatl.platform.wifi

import android.os.Handler
import android.os.Looper
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanCadenceScheduler
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanCadenceTask

class AndroidWifiScanCadenceScheduler(
    private val handler: Handler = Handler(Looper.getMainLooper())
) : WifiScanCadenceScheduler {
    override fun schedule(
        delayMillis: Long,
        action: () -> Unit
    ): WifiScanCadenceTask {
        val runnable = Runnable(action)
        handler.postDelayed(runnable, delayMillis)

        return WifiScanCadenceTask {
            handler.removeCallbacks(runnable)
        }
    }
}
