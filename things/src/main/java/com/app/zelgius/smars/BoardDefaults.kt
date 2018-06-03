/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.zelgius.smars

import android.os.Build

object BoardDefaults {
    private val DEVICE_RPI3 = "rpi3"
    private val DEVICE_IMX6UL_PICO = "imx6ul_pico"
    private val DEVICE_IMX7D_PICO = "imx7d_pico"

    val irOut = when(Build.DEVICE){
    //DEVICE_RPI3 -> "BCM27"
        //DEVICE_RPI3 -> "BCM17"
        DEVICE_RPI3 -> "BCM12"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val irEnable = when(Build.DEVICE){
        //DEVICE_RPI3 ->  "BCM27"
        DEVICE_RPI3 ->  "BCM3"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val ledRed = when(Build.DEVICE){
        //DEVICE_RPI3 -> "BCM27"
        DEVICE_RPI3 -> "BCM19"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val ledGreen = when(Build.DEVICE){
        DEVICE_RPI3 ->  "BCM22"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }
    val ledBlue = when(Build.DEVICE){
        //DEVICE_RPI3 ->  "BCM4"
        DEVICE_RPI3 ->  "BCM23"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val motor1A = when(Build.DEVICE){
        DEVICE_RPI3 ->  "BCM6"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val motor2A = when(Build.DEVICE){
        DEVICE_RPI3 ->  "BCM5"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val motor3A = when(Build.DEVICE){
        DEVICE_RPI3 ->  "BCM20"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }

    val motor4A = when(Build.DEVICE){
        DEVICE_RPI3 ->  "BCM16"
        else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
    }
}