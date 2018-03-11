package com.app.zelgius.smars

import android.Manifest
import android.app.Fragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.adapter_rover.view.*
import kotlinx.android.synthetic.main.dialog_fragment_find_device.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [DialogFindDevice.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogFindDevice : DialogFragment() {

    private lateinit var adapter: DeviceAdapter
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var scanning: Boolean = false
    private var handler: Handler? = null

    var listener: ((address: String)-> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ScanningDialog)
        if (arguments != null) {}

        handler = Handler()

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        bluetoothAdapter = bluetoothManager!!.adapter

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 42 && grantResults.size == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            scanLeDevice(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_fragment_find_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DeviceAdapter(activity!!, mutableListOf())
        recyclerView.adapter = adapter

        scan.setOnClickListener({

            adapter.clear()
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        42)
            } else {
                scanLeDevice(true)
            }
        })

        scan.performClick()
    }

    // Device scan callback.
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            activity?.runOnUiThread({
                if(result != null && result.device != null)
                    adapter.add(result.device)
            })
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(DialogFindDevice::javaClass.name, "Scanning error: $errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.e(DialogFindDevice::javaClass.name, "onBatchScanResults $results")
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler?.postDelayed({
                scanning = false
                bluetoothScanner?.stopScan(scanCallback)
                if(progressBar != null)
                    progressBar.visibility = View.GONE
            }, 10000)

            scanning = true
            progressBar.visibility = View.VISIBLE
            bluetoothScanner?.startScan(scanCallback)
        } else {
            scanning = false
            progressBar.visibility = View.GONE
            bluetoothScanner?.stopScan(scanCallback)
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DialogFindDevice.
         */
        fun newInstance(): DialogFindDevice {
            val fragment = DialogFindDevice()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    inner class DeviceAdapter(private val ctx: Context,
                              private var devices: MutableList<BluetoothDevice>) : RecyclerView.Adapter<ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.adapter_rover, parent, false))
        }

        override fun getItemCount(): Int {
            return devices.size
        }
        
        fun add(device: BluetoothDevice){
            if(devices.find { it.address == device.address } == null) {
                devices.add(device)
                notifyItemInserted(devices.lastIndex)
            }
        }

        fun clear(){
            devices.clear()
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val d = devices[position]

            holder.itemView.address.text = d.address
            holder.itemView.name.text = when {
                d.name == null -> holder.itemView.context.getString(R.string.unknown)
                d.name.isEmpty() -> holder.itemView.context.getString(R.string.unknown)
                else -> d.name
            }

            holder.itemView.setOnClickListener({
                scanLeDevice(false)
                listener?.invoke(d.address)
                dismiss()
            })
        }


    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
