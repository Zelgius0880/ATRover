package com.app.zelgius.smars

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.util.*


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
class MainActivity : Activity() {
    private val mHandler = Handler()
    private val testEngine = Handler()

    var engine: EnginesController? = null
    var led: LEDController? = null

    private lateinit var gattServer: GattServer

    var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        try {
            //OUTPUT


            engine = EnginesController()
            led = LEDController()

            Log.i(TAG, "Start blinking LED GPIO pin")
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            //mHandler.post(mBlinkRunnable)
            //testEngine.post(testEngineRunnable)

            /*val intentFilter = IntentFilter()
            intentFilter.addAction("CONNECTION")
            intentFilter.addAction("DISCONNECTION")
            intentFilter.addAction("DATA_CHANGED")
            registerReceiver(receiver, intentFilter)*/

        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }



        gattServer = GattServer(this, callback)

        if (!gattServer.start()) finish()
    }

    private val callback = object : GattServer.Callback {
        override fun onConnectionChanged(connected: Boolean, device: BluetoothDevice) {
            if (connected) {
                this@MainActivity.connected = true
                led?.red?.value = false
                led?.green?.value = false
                led?.blue?.value = false
                led?.pulse(Speed.MEDIUM, LED.BLUE)
            } else {
                this@MainActivity.connected = false
                led?.red?.value = false
                led?.green?.value = false
                led?.blue?.value = false
                led?.pulse(Speed.LOW, LED.RED)
                engine?.stopMoving()
            }
        }

        override fun onCharacteristicChanged(uid: UUID, byteArray: ByteArray) {
            when (byteArray[0]) {
                0.toByte() -> engine?.stopMoving()
                1.toByte() -> engine?.moveForward()
                2.toByte() -> engine?.turnRight()
                3.toByte() -> engine?.moveBackward()
                4.toByte() -> engine?.turnLeft()
            }

            if (byteArray[1] == 0.toByte()) {
                if (led?.light != false) {
                    led?.light = false
                }
                led?.pulse(Speed.MEDIUM, LED.BLUE)
            } else {
                if (led?.light != true)
                    led?.light = true
            }
        }
    }

    /*private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "DATA_CHANGED" -> {
                    when (intent.getByteExtra("DIRECTION", 0)) {
                        0.toByte() -> engine?.stopMoving()
                        1.toByte() -> engine?.moveForward()
                        2.toByte() -> engine?.turnRight()
                        3.toByte() -> engine?.moveBackward()
                        4.toByte() -> engine?.turnLeft()
                    }

                    if (intent.getByteExtra("CMD", 0) == 0.toByte()) {
                        if (led?.light != false) {
                            led?.light = false
                        }
                        led?.pulse(Speed.MEDIUM, LED.BLUE)
                    } else {
                        if (led?.light != true)
                            led?.light = true
                    }
                }
                "CONNECTED" -> {
                    connected = true
                    led?.red?.value = false
                    led?.green?.value = false
                    led?.blue?.value = false
                    led?.pulse(Speed.MEDIUM, LED.BLUE)
                }
                "DISCONNECTED" -> {
                    connected = false
                    led?.red?.value = false
                    led?.green?.value = false
                    led?.blue?.value = false
                    led?.pulse(Speed.LOW, LED.RED)
                    engine?.stopMoving()
                }
            }
        }

    }*/

    override fun onResume() {
        super.onResume()
        if (connected)
            led?.pulse(Speed.MEDIUM, LED.BLUE)
        else
            led?.pulse(Speed.LOW, LED.RED)
    }

    override fun onPause() {
        super.onPause()
        led?.stopPulse()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mBlinkRunnable)
        testEngine.removeCallbacks(testEngineRunnable)
        try {
            led?.close()
            engine?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //unregisterReceiver(receiver)
        gattServer.stop()

    }

    private val testEngineRunnable = object : Runnable {
        override fun run() {

            /* when(engine?.direction){
                 EnginesController.Direction.FORWARD -> engine?.moveBackward()
                 EnginesController.Direction.BACKWARD -> engine?.turnLeft()
                 EnginesController.Direction.LEFT -> engine?.turnRight()
                 else -> engine?.moveForward()
             }*/
            //engine?.moveForward()
            engine?.stopMoving()

            testEngine.postDelayed(this, 500)
        }

    }


    private val mBlinkRunnable = object : Runnable {
        override fun run() {
            try {
                // Toggle the GPIO state
                led?.blink(LED.RED)

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(this, 1000)
            } catch (e: IOException) {
                Log.e(TAG, "Error on PeripheralIO API", e)
            }

        }
    }
}
