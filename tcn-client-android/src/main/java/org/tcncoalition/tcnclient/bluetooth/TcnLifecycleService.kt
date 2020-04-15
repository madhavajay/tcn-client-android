package org.tcncoalition.tcnclient.bluetooth

import android.app.Notification
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService

class TcnLifecycleService : LifecycleService() {

    inner class LocalBinder : Binder() {
        val service = this@TcnLifecycleService
    }

    companion object {
        const val NOTIFICATION_ID = 0
    }

    private var tcnBluetoothService: TcnBluetoothService? = null
    private val binder: IBinder = LocalBinder()
    private lateinit var tcnCallback: TcnBluetoothServiceCallback

    private var isStarted = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        tcnBluetoothService?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    private fun BluetoothAdapter.supportsAdvertising() =
        isMultipleAdvertisementSupported && bluetoothLeAdvertiser != null

    fun setForegroundNotification(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)
    }

    fun setTcnCallback(tcnCallback: TcnBluetoothServiceCallback) {
        this.tcnCallback = tcnCallback
    }

    fun startTcnBluetoothService() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.supportsAdvertising()

        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return

        if (isStarted) return
        isStarted = true

        tcnBluetoothService = TcnBluetoothService(
            this@TcnLifecycleService,
            scanner,
            advertiser,
            tcnCallback
        )

        tcnBluetoothService?.start()
    }

    fun stopTcnBluetoothService() {
        if (!isStarted) return
        isStarted = false

        tcnBluetoothService?.stop()
    }

}
