package com.app.zelgius.smars

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.app.zelgius.shared.BluetoothInfo
import com.app.zelgius.shared.BluetoothListener
import com.app.zelgius.shared.Direction
import com.app.zelgius.shared.ServerThread
import java.nio.ByteBuffer


class MainViewModel(application: Application) : AndroidViewModel(application) {
    var connected: MutableLiveData<Boolean> = MutableLiveData()
    var connecting: MutableLiveData<Boolean> = MutableLiveData()
    var discovering: MutableLiveData<Boolean> = MutableLiveData()
    var needRefresh: MutableLiveData<Boolean> = MutableLiveData()
    val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    val direction: MutableLiveData<Direction> = MutableLiveData()
    val enabled: MutableLiveData<Boolean> = MutableLiveData()
    val light: MutableLiveData<Boolean> = MutableLiveData()
    val obstacle: MutableLiveData<Boolean> = MutableLiveData()

    var thread: ServerThread? = null

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0)
                    if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                        startDiscovering()
                    else if(state == BluetoothAdapter.SCAN_MODE_CONNECTABLE){
                        startDiscovering()
                    }
                }
            }
        }
    }
    init {
        application.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
    }

    override fun onCleared() {
        super.onCleared()
        thread?.cancel()
        getApplication<Application>().unregisterReceiver(bluetoothReceiver)
    }

    fun enableLight(enabled: Boolean) {
        thread?.sendMessage(BluetoothInfo.allocate(BluetoothInfo.Message.ENABLE_LIGHT).apply{
            put((if(enabled) 1 else 0).toByte())
        })
    }

    fun setDirection(direction: Direction, power: Float){
        thread?.sendMessage(BluetoothInfo.allocate(BluetoothInfo.Message.SET_DIRECTION).apply {
            put(Direction.convert(direction))
            putFloat(power)
        })
    }


    fun setPower(power1: Float, power2: Float){
        thread?.sendMessage(BluetoothInfo.allocate(BluetoothInfo.Message.SET_POWER).apply {
            putFloat(power1)
            putFloat(power2)
        })
    }

    fun startDiscovering() {
        connecting.value = true
        thread?.cancel()
        thread = ServerThread(bluetoothAdapter, object : BluetoothListener {
            override fun onConnectionChanged(connect: Boolean) {
                connecting.postValue(false)
                connected.postValue(connect)
            }

            override fun onDataReceived(msg: BluetoothInfo.Message, data: ByteBuffer) {
                when (msg){
                    BluetoothInfo.Message.ENABLE_LIGHT -> {
                        light.postValue(data.get().toInt() == 1)
                    }

                    BluetoothInfo.Message.OBSTACLE -> {
                        obstacle.postValue(data.get().toInt() == 1)
                    }
                    else -> {}
                }
            }

        })

        thread?.start()
    }

    fun disconnect() {
        connecting.value = false
        connected.value = false
        discovering.value = false
        thread?.cancel()
        thread = null
    }


    var ledOn = false


}