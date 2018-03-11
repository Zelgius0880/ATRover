package com.app.zelgius.smars

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.io.IOException


class EnginesController {
    private val service = PeripheralManager.getInstance()
    private val motor1: Gpio?
    private val motor2: Gpio?
    private val motor3: Gpio?
    private val motor4: Gpio?
    private val enable1: Gpio?
    private val enable2: Gpio?

    private var direction: Direction = Direction.STOP

    enum class Direction {
        FORWARD, BACKWARD, RIGHT, LEFT, STOP
    }

    init {
        try {
            enable1 = service.openGpio(BoardDefaults.enable1)
            enable1?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)

            enable2 = service.openGpio(BoardDefaults.enable2)
            enable2?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)

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

    private fun moveLeftSide(direction: Direction) {
        if (direction == Direction.FORWARD) {
            motor1?.value = false
            motor2?.value = true
        } else if (direction == Direction.BACKWARD) {
            motor1?.value = true
            motor2?.value = false
        }
    }

    private fun moveRightSide(direction: Direction) {
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

}