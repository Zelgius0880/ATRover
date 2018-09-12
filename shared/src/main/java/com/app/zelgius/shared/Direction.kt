package com.app.zelgius.shared

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

    companion object {
        fun convert(b: Byte) : Direction = when(b.toInt()){
            0 -> STOP
            1 -> FORWARD
            2 -> LEFT
            3 -> BACKWARD
            4 -> RIGHT
            else -> error("Unknown direction")
        }

        fun convert(direction: Direction) : Byte = when(direction){
            STOP -> 0
            FORWARD -> 1
            LEFT -> 2
            BACKWARD -> 3
            RIGHT -> 4
        }.toByte()
    }
}