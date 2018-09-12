package com.app.zelgius.smars

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.app.zelgius.shared.BluetoothInfo
import com.app.zelgius.shared.BluetoothListener
import com.app.zelgius.shared.ClientThread
import com.app.zelgius.shared.Direction
import com.google.android.things.bluetooth.BluetoothClassFactory
import com.google.android.things.bluetooth.BluetoothConfigManager
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.concurrent.thread


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName
    private val service = PeripheralManager.getInstance()

    var engine: EnginesController? = null
    var led: LEDController? = null
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var thread: ClientThread? = null

    var connected = false

    /*val enableIr: Gpio? by lazy {
        try {
            service.openGpio(BoardDefaults.irEnable).apply {
                setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }*/

    val obstacle = MutableLiveData<Boolean>()

    val outIr: Gpio? by lazy {
        try {
            service.openGpio(BoardDefaults.irOut).apply {
                setDirection(Gpio.DIRECTION_IN)
                setEdgeTriggerType(Gpio.EDGE_BOTH)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        val manager = BluetoothConfigManager.getInstance()

        val deviceClass = BluetoothClassFactory.build(
                BluetoothClass.Service.NETWORKING,
                BluetoothClass.Device.TOY_ROBOT)

        manager.bluetoothClass = deviceClass

        try {
            //OUTPUT
            engine = EnginesController()
            led = LEDController()

            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            //mHandler.post(mBlinkRunnable)
            //testEngine.post(testEngineRunnable)

            /*val intentFilter = IntentFilter()
            intentFilter.addAction("CONNECTION")
            intentFilter.addAction("DISCONNECTION")
            intentFilter.addAction("DATA_CHANGED")
            registerReceiver(receiver, intentFilter)*/

            led?.enable(LED.BLUE)

            //enableIr?.value = false
            //outIr?.value = true
            outIr?.registerGpioCallback {
                obstacle.postValue(!it.value)

                true //listening again
            }

            /*kotlin.concurrent.thread (start = true) {
                while (true) {
                    if (outIr?.value != true) led?.enable(LED.GREEN)
                    else led?.enable(LED.BLUE)
                }
            }*/

            /*enableIr?.registerGpioCallback {
                Log.e(TAG, "Enable IR ${it.value}")
                if(it.value)
                    led?.enable(LED.GREEN)
                else
                    led?.enable()

                true
            }*/

            obstacle.observe(this, Observer {
                if (it != null) {
                    thread?.sendMessage(BluetoothInfo.allocate(BluetoothInfo.Message.OBSTACLE).apply {
                        put((if (it) 1 else 0).toByte())
                    })

                    if (it) engine?.stopMoving()
                    engine?.obstacle = it

                    when {
                        connected && it -> led?.enable(LED.RED, LED.GREEN)
                        connected && !it -> led?.enable(LED.GREEN)
                        !connected && !it -> led?.enable(LED.BLUE)
                        !connected && it -> led?.enable(LED.RED, LED.BLUE)
                    }
                }
            })

        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
            led?.enable(LED.RED, LED.GREEN)
            Handler().postDelayed({
                if (!bluetoothAdapter.isEnabled) {
                    led?.enable(LED.RED)
                    error("Failed to activate Bluetooth")
                } else {
                    led?.enable(LED.BLUE)
                    bluetoothAdapter.startDiscovery()
                }
            }, 12000)
        }


        bluetoothAdapter.startDiscovery()

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            led?.close()
            engine?.close()

            outIr?.close()
            //enableIr?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        })
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            when (action) {

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device.name == BluetoothInfo.SERVER_NAME && !connected) {
                        thread?.cancel()
                        thread = ClientThread(device, bluetoothAdapter, object : BluetoothListener {
                            override fun onConnectionChanged(connect: Boolean) {
                                connected = connect
                                if (connect) {
                                    bluetoothAdapter.cancelDiscovery()
                                    led?.enable(LED.GREEN)
                                } else {
                                    bluetoothAdapter.startDiscovery()
                                    led?.enable(LED.BLUE)
                                }
                            }

                            override fun onDataReceived(msg: BluetoothInfo.Message, data: ByteBuffer) {
                                when (msg) {
                                    BluetoothInfo.Message.ENABLE_LIGHT -> {
                                        val enable = data.get() == 1.toByte()
                                        led?.light = enable
                                        thread?.sendMessage(BluetoothInfo.allocate(msg).apply {
                                            put(if (enable) 1.toByte() else 0.toByte())
                                        })
                                    }

                                    BluetoothInfo.Message.SET_DIRECTION -> {
                                        val direction = Direction.convert(data.get())
                                        val power = data.float
                                        engine?.turn(direction, power.toDouble())
                                    }

                                    BluetoothInfo.Message.SET_POWER -> {
                                        val power1 = data.float
                                        val power2 = data.float
                                        engine?.setPower(power1.toDouble(), power2.toDouble())
                                    }

                                    else -> {
                                    }
                                }
                            }

                        })
                        thread?.start()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (!connected) bluetoothAdapter.startDiscovery()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED, BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                    connected = false
                    bluetoothAdapter.startDiscovery()
                    thread?.cancel()
                }

            }
        }

    }


}
