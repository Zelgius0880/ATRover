package com.app.zelgius.smars

import android.os.Handler
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm
import java.io.IOException


class LEDController {
    private val service = PeripheralManager.getInstance()
    var red: PwmLed
    var blue: PwmLed
    val green: Gpio

    private val pwm0: Pwm //Blue
    private val pwm1: Pwm //Red

    var light: Boolean = false
    set(value) {
        field = value
        enableLight(field)
    }

    var pwmDuty = 100.0
    var increment = 1
    val pulseLeds: MutableList<Pwm> = mutableListOf()

    private val pulseHandler = Handler()

    var pulseSpeed = Speed.FAST

    init {
        try {
            green = service.openGpio(BoardDefaults.ledGreen)
            green?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            pwm0 = service.openPwm("PWM0")

            pwm1 = service.openPwm("PWM1")

            pwm0?.setPwmFrequencyHz(580.0)
            pwm0?.setPwmDutyCycle(0.0)
            // Enable the PWM signal
            pwm0?.setEnabled(true)

            pwm1.setPwmFrequencyHz(580.0)
            pwm1.setPwmDutyCycle(0.0)
            // Enable the PWM signal
            pwm1.setEnabled(true)

            blue = PwmLed(pwm0)
            red = PwmLed(pwm1)
        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(IOException::class)
    fun close() {
        green.close()
        pwm0.close()
        pwm1.close()
    }

    fun blink(vararg leds: LED) {
        leds.forEach {
            when (it) {
                LED.GREEN -> green.value.let {
                    green.value = !it
                }
                LED.BLUE -> blue.value.let {
                    blue.value = !it
                }
                LED.RED -> red.value.let {
                    red.value = !it
                }
            }

        }
    }

    fun pulse(speed: Speed, vararg leds: LED) {
        pulseHandler.removeCallbacks(pulseRunnable)
        pwmDuty = 100.0

        pulseSpeed = speed

        pulseLeds.clear()
        leds.forEach {
            val pwm = when (it) {
                LED.BLUE -> pwm0
                LED.RED -> pwm1
                else -> throw IllegalStateException("Unknown Pwm LED $it")
            }
            if (!pulseLeds.contains(pwm)) pulseLeds.add(pwm)
        }
        pulseHandler.postDelayed(pulseRunnable, speed.ms)
    }

    private val pulseRunnable = object : Runnable {
        override fun run() {
            pulseLeds.forEach {
                it.setPwmDutyCycle(pwmDuty)
            }

            pwmDuty += increment
            if (pwmDuty <= 0) {
                increment = 1
                pwmDuty = 0.0
            } else if (pwmDuty >= 100) {
                increment = -1
                pwmDuty = 100.0
            }
            pulseHandler.postDelayed(this, pulseSpeed.ms)

        }
    }

    private fun enableLight(value: Boolean){
        if(value){
            stopPulse()
        }
        red.value = value
        green.value = value
        blue.value = value

    }

    fun stopPulse() {
        pulseLeds.forEach {
            it.setPwmDutyCycle(0.0)
        }
        pulseLeds.clear()
        pulseHandler.removeCallbacks(pulseRunnable)
    }

    class PwmLed(private val pwm: Pwm) {
        var value: Boolean = false
            set(value) {
                field = value
                pwm.setPwmDutyCycle(if (value) 100.0 else 0.0)
            }
    }
}

enum class LED {
    GREEN, BLUE, RED
}


enum class Speed(val ms: Long) {
    LOW(50L), MEDIUM(25L), FAST(10L)
}
