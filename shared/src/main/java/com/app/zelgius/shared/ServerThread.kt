package com.app.zelgius.shared

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.app.zelgius.shared.BluetoothInfo.Companion.SERVER_NAME
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class ServerThread(bluetoothAdapter: BluetoothAdapter,listener: BluetoothListener) : BluetoothThread(listener) {
    private var mmServerSocket: BluetoothServerSocket? = null

    companion object {
        val TAG = ClientThread::class.java.simpleName

    }
    init {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        bluetoothAdapter.name = SERVER_NAME
        var tmp: BluetoothServerSocket? = null
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothInfo.SERVER_NAME, BluetoothInfo.SERVER_UUID)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Socket's listen() method failed", e)
        }

        mmServerSocket = tmp
    }

    override fun run() {
        var socket: BluetoothSocket?
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            stop = false
            try {
                socket = mmServerSocket?.accept()
                Log.d(TAG, "Client connected")
                listener.onConnectionChanged(true)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Socket's accept() method failed", e)
                stop = true
                break
            }

           if(socket != null) startListening(socket)

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                listener.onConnectionChanged(false)
                socket.outputStream.close()
                socket.close()
            }
        }
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            stop = true
            mmServerSocket?.close()
            listener.onConnectionChanged(false)
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }
}