package com.app.zelgius.smars

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_remote -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, RemoteFragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_gyro -> {
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

                    if (viewModel.characteristic == null)
                        Snackbar.make(container, R.string.failed_to_connect, Snackbar.LENGTH_LONG).show()
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

        connect.setOnClickListener({
            if (viewModel.connected.value != true) {
                DialogFindDevice.newInstance().let {
                    it.listener = {
                        viewModel.connect(it)
                    }
                    it.show(supportFragmentManager, "dialog_device")
                }
            } else viewModel.disconnect()
        })

        viewModel.needRefresh.observe(this, Observer {
            if (it == true) {
                viewModel.needRefresh.value = false
            }
        })
        viewModel.needRefresh.value = true

        light.setOnClickListener({
            if(viewModel.connected.value == true) {
                if (!viewModel.ledOn) {
                    light.drawable.let {
                        (it as? AnimatedVectorDrawable)?.start()
                    }
                    light.backgroundTintList = ColorStateList.valueOf(getColor(R.color.md_yellow_200))
                    viewModel.enableLight(true)
                } else {
                    light.drawable.let {
                        (it as? AnimatedVectorDrawable)?.reset()
                    }
                    light.backgroundTintList = ColorStateList.valueOf(getColor(R.color.white))
                    viewModel.enableLight(false)
                }

                viewModel.ledOn = !viewModel.ledOn
            }
        })
    }

}
