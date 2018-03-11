package com.app.zelgius.smars

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.os.IBinder
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*


class MainViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = MainViewModel::class.java.simpleName!!
    //private val db = AppDatabase.get(application)
    //private val roverDao = db.roverDao()
    private var bluetoothLeService: WeakReference<BluetoothLeService?>? = null
    var connected: MutableLiveData<Boolean> = MutableLiveData()
    var connecting: MutableLiveData<Boolean> = MutableLiveData()
    var needRefresh: MutableLiveData<Boolean> = MutableLiveData()
    var device: String? = null
    var services: List<BluetoothGattService> = listOf()
    var characteristic: BluetoothGattCharacteristic? = null
    val commandQueue: MutableList<Command> = mutableListOf()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                }/*connected.value = true*/
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> connected.value = false
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> // Show all the supported services and characteristics on the user interface.
                    if (bluetoothLeService?.get()?.supportedGattServices != null) {
                        services = bluetoothLeService?.get()?.supportedGattServices!!
                        val s = services.find { it.uuid == LOCATION_SERVICE }
                        if (s != null)
                            characteristic = s.getCharacteristic(CONTROL_POINT)

                        connecting.value = false
                        connected.value = characteristic != null
                        currentDirection = 0

                        ThreadQueue().start()
                    }
                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                    //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }
            }
        }

    }
    // Code to manage Service lifecycle.
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = WeakReference((service as BluetoothLeService.LocalBinder).service)
            if (bluetoothLeService?.get()?.initialize() == false) {
                Log.e(TAG, "Unable to initialize Bluetooth")
            }
            // Automatically connects to the device upon successful start-up initialization.

            if (device != null) bluetoothLeService?.get()?.connect(device)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
        }
    }

    init {
        application.registerReceiver(receiver, makeGattUpdateIntentFilter())
        application.bindService(Intent(application, BluetoothLeService::class.java),
                serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        this.getApplication<Application>().unregisterReceiver(receiver)
        super.onCleared()
    }

    fun setDirection(direction: Command) {
        synchronized(commandQueue) {
            commandQueue.add(direction)
        }
    }

    fun enableLight(enabled: Boolean) {
        synchronized(commandQueue) {
            commandQueue.add(
                    if (enabled) Command.LIGHT_ON
                    else Command.LIGHT_OFF
            )
        }
    }

    fun connect(device: String) {
        this.device = device
        connecting.value = true
        bluetoothLeService?.get()?.connect(device)
    }

    fun disconnect() {
        connecting.value = false
        connected.value = false
        bluetoothLeService?.get()?.disconnect()
        commandQueue.clear()
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    var ledOn = false
    var currentDirection = 0.toByte()

    inner class ThreadQueue : Thread({

        while (connected.value == true) {
            if (!commandQueue.isEmpty()) {
                var cmd = Command.STOP
                synchronized(commandQueue) {
                    cmd = commandQueue.removeAt(0)
                }
                ledOn = when (cmd) {
                    Command.LIGHT_OFF -> false
                    Command.LIGHT_ON -> true
                    else -> ledOn
                }

                if (characteristic != null) {
                    characteristic!!.value = byteArrayOf(when (cmd) {
                        Command.STOP -> 0.toByte()
                        Command.FORWARD -> 1.toByte()
                        Command.RIGHT -> 2.toByte()
                        Command.BACKWARD -> 3.toByte()
                        Command.LEFT -> 4.toByte()
                        else -> currentDirection
                    }, if (ledOn) 1 else 0)
                    bluetoothLeService?.get()?.writeCharacteristic(characteristic!!)
                }

                Thread.sleep(300)
            }
        }
    })

    enum class Command {
        FORWARD, BACKWARD, RIGHT, LEFT, STOP, LIGHT_ON, LIGHT_OFF
    }

    val LOCATION_SERVICE = UUID.fromString("00001819-0000-1000-8000-00805f9b34fb")!!
    val CONTROL_POINT = UUID.fromString("00002A6B-0000-1000-8000-00805f9b34fb")!!
}