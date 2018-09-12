package com.app.zelgius.smars

import android.os.Handler
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.io.IOException


class LEDController {
    private val peripheralManager = PeripheralManager.getInstance()
    var red: Gpio
    var blue: Gpio
    var green: Gpio

    var speed = Speed.MEDIUM


    var light: Boolean = false
    set(value) {
        field = value
        enableLight(field)
    }

    val leds: MutableList<Gpio> = mutableListOf()
    val ledsEnabled = mutableListOf<LED>()

    private val pulseHandler = Handler()


    init {
        try {
            green = peripheralManager.openGpio(BoardDefaults.ledGreen)
            green.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            blue = peripheralManager.openGpio(BoardDefaults.ledBlue)
            blue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            red = peripheralManager.openGpio(BoardDefaults.ledRed)
            red.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(IOException::class)
    fun close() {
        green.close()
        blue.close()
        red.close()
    }

    fun blink(vararg leds: LED, speed: Speed = Speed.MEDIUM) {
        this.leds.clear()
        leds.forEach {
            when (it) {
                LED.GREEN -> {
                    this.leds.add(green)
                    green.value.let {
                        green.value = !it
                    }
                }
                LED.BLUE -> {
                    this.leds.add(blue)
                    blue.value.let {
                        blue.value = !it
                    }
                }
                LED.RED -> {
                    this.leds.add(red)
                    red.value.let {
                        red.value = !it
                    }
                }
            }
        }

        this.speed = speed
        pulseHandler.postDelayed(blinkRunnable, speed.ms)
    }

    fun enable(vararg leds: LED) {
        red.value = false
        green.value = false
        blue.value = false

        ledsEnabled.clear()
        ledsEnabled.addAll(leds)
        leds.forEach {
            when (it) {
                LED.GREEN -> {
                    this.leds.add(green)
                    green.value = true
                }
                LED.BLUE -> {
                    this.leds.add(blue)
                    blue.value = true
                }
                LED.RED -> {
                    this.leds.add(red)
                    red.value = true
                }
            }
        }
    }

    private val blinkRunnable = object : Runnable {
        override fun run() {
            leds.forEach {
                it.value = !it.value
            }

            pulseHandler.postDelayed(this, speed.ms)
        }
    }

    private fun enableLight(value: Boolean){
        red.value = value
        green.value = value
        blue.value = value

        if(!value){
            enable(*ledsEnabled.toTypedArray())
        }
    }
}

enum class LED {
    GREEN, BLUE, RED
}

enum class Speed(val ms: Long) {
    LOW(50L), MEDIUM(25L), FAST(10L)
}