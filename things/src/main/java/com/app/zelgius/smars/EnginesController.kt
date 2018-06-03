package com.app.zelgius.smars

import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm
import java.io.IOException


class EnginesController {
    private val service = PeripheralManager.getInstance()
    private val motor1: Gpio?
    private val motor2: Gpio?
    private val motor3: Gpio?
    private val motor4: Gpio?
    private val enable1: Pwm?
    private val enable2: Pwm?

    var obstacle = false

    private var direction: Direction = Direction.STOP

    var lastAngle = 0.0

    enum class Direction {
        FORWARD, BACKWARD, RIGHT, LEFT, STOP;

        operator fun not(): Direction {
            return when (this) {
                LEFT -> RIGHT
                RIGHT -> LEFT
                FORWARD -> BACKWARD
                BACKWARD -> FORWARD
                else -> STOP
            }
        }
    }

    init {
        try {
            enable1 = service.openPwm("PWM0")

            enable2 = service.openPwm("PWM1")

            enable1?.setPwmFrequencyHz(580.0)
            enable1?.setPwmDutyCycle(100.0)
            // Enable the PWM signal
            enable1?.setEnabled(true)

            enable2?.setPwmFrequencyHz(580.0)
            enable2?.setPwmDutyCycle(100.0)
            // Enable the PWM signal
            enable2?.setEnabled(true)


            motor1 = service.openGpio(BoardDefaults.motor1A)
            motor1?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            motor2 = service.openGpio(BoardDefaults.motor2A)
            motor2?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            motor3 = service.openGpio(BoardDefaults.motor3A)
            motor3?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            motor4 = service.openGpio(BoardDefaults.motor4A)
            motor4?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(IOException::class)
    fun close() {
        enable1?.close()
        enable2?.close()
        motor1?.close()
        motor2?.close()
        motor3?.close()
        motor4?.close()
    }

    private fun moveRightSide(direction: Direction) {
        if (direction == Direction.FORWARD) {
            motor1?.value = true
            motor2?.value = false
        } else if (direction == Direction.BACKWARD) {
            motor1?.value = false
            motor2?.value = true
        }
    }

    private fun moveLeftSide(direction: Direction) {
        if (direction == Direction.FORWARD) {
            motor3?.value = true
            motor4?.value = false
        } else if (direction == Direction.BACKWARD) {
            motor3?.value = false
            motor4?.value = true
        }
    }

    fun stopMoving() {
        direction = Direction.STOP
        motor1?.value = false
        motor2?.value = false
        motor3?.value = false
        motor4?.value = false
    }

    fun moveForward() {
        direction = Direction.FORWARD

        moveRightSide(direction)
        moveLeftSide(direction)
    }

    fun moveBackward() {
        direction = Direction.BACKWARD

        moveRightSide(direction)
        moveLeftSide(direction)
    }

    fun turnLeft() {
        direction = Direction.LEFT
        moveRightSide(Direction.FORWARD)
        moveLeftSide(Direction.BACKWARD)
    }

    fun turnRight() {
        direction = Direction.RIGHT
        moveRightSide(Direction.BACKWARD)
        moveLeftSide(Direction.FORWARD)
    }

    fun moveRight(direction: Direction, power: Double) {
        val finalDirection = if (power < 0) !direction else direction
        Log.e(EnginesController::javaClass.name, "Moving ${Direction.RIGHT} $finalDirection $direction $power")

        enable1?.setPwmDutyCycle(Math.abs(power))
        moveRightSide(finalDirection)
    }

    fun moveLeft(direction: Direction, power: Double) {
        val finalDirection = if (power < 0) !direction else direction
        Log.e(EnginesController::javaClass.name, "Moving ${Direction.LEFT} $finalDirection $direction $power")

        enable2?.setPwmDutyCycle(Math.abs(power))
        moveLeftSide(finalDirection)
    }

    fun turn(angle: Double, power: Double = 100.0) {

        Log.e(EnginesController::javaClass.name, "Moving Request $angle°, $power°")

        val mainSide = if (Math.abs(angle) > 90) Direction.LEFT else Direction.RIGHT
        val direction = if (angle < 0) Direction.BACKWARD else Direction.FORWARD

        if (direction != Direction.FORWARD || !obstacle) {
            /*val cos = -Math.abs(Math.cos(Math.toRadians(Math.abs(angle)+90)))
            val pct = power * cos*/
            var final = if (Math.abs(angle) <= 90) Math.abs(angle) else 180 - Math.abs(angle)
            final -= 45
            final /= 45
            final *= power

            if (mainSide == Direction.LEFT) {
                moveRight(direction, power)
                moveLeft(direction, final)
            } else {
                moveLeft(direction, power)
                moveRight(direction, final)
            }
        }
    }
}