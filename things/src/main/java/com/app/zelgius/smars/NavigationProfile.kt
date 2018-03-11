/*
 * Copyright 2017, The Android Open Source Project
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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

/**
 * Implementation of the Bluetooth GATT Time Profile.
 * https://www.bluetooth.com/specifications/adopted-specifications
 */
object NavigationProfile {
    private val TAG = NavigationProfile::class.java.simpleName

    val LOCATION_SERVICE = UUID.fromString("00001819-0000-1000-8000-00805f9b34fb")!!
    val CONTROL_POINT = UUID.fromString("00002A6B-0000-1000-8000-00805f9b34fb")!!

    /**
     * Return a configured [BluetoothGattService] instance for the
     * Current Time Service.
     */
    fun createService(): BluetoothGattService {
        val service = BluetoothGattService(LOCATION_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Current Time characteristic
        val controlPoint = BluetoothGattCharacteristic(CONTROL_POINT,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_INDICATE,
                BluetoothGattCharacteristic.PERMISSION_WRITE)

        service.addCharacteristic(controlPoint)

        return service
    }
}