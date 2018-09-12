package com.app.zelgius.smars

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 15
        const val DISCOVERABLE_TIME = 300
    }



    private lateinit var viewModel: MainViewModel
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_remote -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, RemoteFragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_remote_2-> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, Remote2Fragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_alone -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, RemoteFragment.newInstance())
                .commit()

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.connecting.observe(this, Observer {
            if (it != null) {
                if (it) {
                    try {
                        fabProgressCircle.show()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        fabProgressCircle.hide()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        })

        viewModel.connected.observe(this, Observer {
            if (it != null) {
                if (it) {
                    connect.backgroundTintList = ColorStateList.valueOf(getColor(R.color.md_green_400))
                    try {
                        fabProgressCircle.hide()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                } else {
                    connect.backgroundTintList = ColorStateList.valueOf(getColor(R.color.md_red_400))
                    try {
                        fabProgressCircle.hide()
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        })

        viewModel.needRefresh.observe(this, Observer {
            if (it == true) {
                viewModel.needRefresh.value = false
            }
        })
        viewModel.needRefresh.value = true

        light.setOnClickListener {
            if (viewModel.connected.value == true) {
                if (!viewModel.ledOn) {
                    viewModel.enableLight(true)
                } else {
                    viewModel.enableLight(false)
                }

                viewModel.ledOn = !viewModel.ledOn
            }
        }

        viewModel.light.observe(this, Observer {
            if(it == true){
                light.drawable.apply {
                    (this as? AnimatedVectorDrawable)?.start()
                }
                light.backgroundTintList = ColorStateList.valueOf(getColor(R.color.md_yellow_200))
            } else {
                light.drawable.let {
                    (it as? AnimatedVectorDrawable)?.reset()
                }
                light.backgroundTintList = ColorStateList.valueOf(getColor(R.color.white))
            }
        })



        connect.setOnClickListener {
            if(viewModel.connected.value == false)startDiscovery()
            else {
                viewModel.connected.value = false
                viewModel.disconnect()
            }
        }

        viewModel.enabled.value = viewModel.bluetoothAdapter.isEnabled
        if (viewModel.enabled.value != true) {
            val enableBtIntent = Intent(ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    42)
        } else startDiscovery()

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 42 && grantResults.size == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startDiscovery()
        }
    }

    private fun startDiscovery(){
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME)
        startActivity(discoverableIntent)
    }
}
