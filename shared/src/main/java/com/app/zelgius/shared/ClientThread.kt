package com.app.zelgius.shared

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class ClientThread(mmDevice: BluetoothDevice, private val mBluetoothAdapter: BluetoothAdapter, listener: BluetoothListener) : BluetoothThread(listener)  {
    companion object {
        val TAG = ClientThread::class.java.simpleName
    }

    private val mmSocket: BluetoothSocket?

    init {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        var tmp: BluetoothSocket? = null

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(BluetoothInfo.SERVER_UUID)
        } catch (e: IOException) {
            Log.e(TAG, "Socket's create() method failed", e)
        }

        mmSocket = tmp
    }

    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery()

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket!!.connect()
        } catch (connectException: IOException) {
            // Unable to connect; close the socket and return.
            listener.onConnectionChanged(false)
            connectException.printStackTrace()
            try {
                mmSocket!!.close()
            } catch (closeException: IOException) {
                Log.e(TAG, "Could not close the client socket", closeException)
            }

            return
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.

        listener.onConnectionChanged(true)

        startListening(mmSocket)

        listener.onConnectionChanged(false)

    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            stop = true
            mmSocket!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }

    }
}