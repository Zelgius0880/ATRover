package com.app.zelgius.shared

import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.thread

class BluetoothInfo {
    enum class Message(val protocol: Int, val length: Int) {
        ENABLE_LIGHT(0, 1),
        SET_DIRECTION(1, 8),
        OBSTACLE(2, 1);
        //0-3 : angle
        //4-7 : power pct

    }

    companion object {

        fun getMessage(protocol: Int): Message {
            return when (protocol) {
                0 -> Message.ENABLE_LIGHT
                1 -> Message.SET_DIRECTION
                2 -> Message.OBSTACLE
                else -> throw IllegalStateException("Unknown protocol $protocol")
            }
        }

        val SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val SERVER_NAME = UUID.fromString("75c7e990-86d8-435f-a597-3d7b76f2ba7a").toString()

        fun allocate(msg: Message): ByteBuffer {
            return ByteBuffer.allocate(msg.length + 1).apply {
                put(msg.protocol.toByte())
            }
        }
    }
}

interface BluetoothListener {
    fun onConnectionChanged(connect: Boolean)
    fun onDataReceived(msg: BluetoothInfo.Message, data: ByteBuffer)
}

open class BluetoothThread(val listener: BluetoothListener) : Thread() {
    private val msg = mutableListOf<ByteBuffer>()
    protected var stop = false

    protected fun startListening(socket: BluetoothSocket) {
        val output = socket.outputStream
        val input = socket.inputStream
        thread(start = true) {
            while (!stop) {
                try {
                    val protocol = input?.read()
                    if (protocol != null && protocol != -1) {
                        val msg = BluetoothInfo.getMessage(protocol)
                        val array = ByteArray(msg.length)
                        input.read(array)
                        listener.onDataReceived(msg, ByteBuffer.wrap(array))
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                    stop = true
                }
            }
        }

        while (!stop) {
            try {
                if (!msg.isEmpty()) {
                    synchronized(msg) {
                        val m = msg.removeAt(0)
                        if (m != null) {
                            val array = m.array()
                            output?.write(array)
                            output?.flush()
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                stop = true
            }

        }
    }

    fun sendMessage(buffer: ByteBuffer) {
        msg.add(buffer)
    }

}