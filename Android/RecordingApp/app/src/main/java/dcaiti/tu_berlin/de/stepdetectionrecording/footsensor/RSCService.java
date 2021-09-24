/*
 * Copyright (C) 2013 Lann Martin
 * Modifcations Copyright (C) 2017 Joris Clement
 *
 * Licensed under the Apache License, Version 2.0 as below
 *
 * This file incorporates work covered by the following notice:
 *
 *   Copyright (C) 2013 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package dcaiti.tu_berlin.de.stepdetectionrecording.footsensor;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.UUID;


/**
 * Adapted from:
 * http://developer.android.com/samples/BluetoothLeGatt/src/com.example.android.bluetoothlegatt/BluetoothLeService.html
 * https://github.com/lann/RFDuinoTest/blob/master/src/main/java/com/lannbox/rfduinotest/RFduinoService.java
 * http://www.nordicsemi.com/eng/Products/nRFready-Demo-APPS/nRF-Toolbox-for-Android-4.3
 *
 * Modified by Joris Clement
 */
public class RSCService extends Service {

    private final static String TAG = RSCService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;

    // Note: Changed prefix here to 'dcaiti'
    final static String ACTION_CONNECTED =
            "dcaiti.positioning.android.ACTION_CONNECTED";
    final static String ACTION_DISCONNECTED =
            "dcaiti.positioning.android.ACTION_DISCONNECTED";
    final static String ACTION_DATA_AVAILABLE =
            "dcaiti.positioning.android.ACTION_DATA_AVAILABLE";
    private final static String EXTRA_DATA =
            "dcaiti.positioning.android.EXTRA_DATA";

    private static final byte INSTANTANEOUS_STRIDE_LENGTH_PRESENT = 0x01; // 1 bit
    private static final byte TOTAL_DISTANCE_PRESENT = 0x02; // 1 bit
    private static final byte WALKING_OR_RUNNING_STATUS_BITS = 0x04; // 1 bit

    public final static UUID RUNNING_SPEED_AND_CADENCE_SERVICE_UUID =
            UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    /** Running Speed and Cadence Measurement characteristic */
    private static final UUID RSC_MEASUREMENT_CHARACTERISTIC_UUID =
            UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");

    /** Client configuration descriptor that will allow us to enable notifications and indications */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to Footsensor " +
                        FootSensor.side(gatt.getDevice().getAddress()));
                boolean success = bluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery: " + success);
                broadcastUpdateConnectionStatusChange(ACTION_CONNECTED,
                        gatt.getDevice().getAddress());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from FootSensor " +
                        FootSensor.side(gatt.getDevice().getAddress()));
                broadcastUpdateConnectionStatusChange(ACTION_DISCONNECTED,
                                                      gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService bluetoothGattService =
                        gatt.getService(RUNNING_SPEED_AND_CADENCE_SERVICE_UUID);
                if (bluetoothGattService == null) {
                    Log.e(TAG, "RUNNING_SPEED_AND_CADENCE_SERVICE not found!");
                    return;
                }
                Log.d(TAG, "trying to read rsc");
                BluetoothGattCharacteristic receiveCharacteristic =
                        bluetoothGattService.getCharacteristic(RSC_MEASUREMENT_CHARACTERISTIC_UUID);
                if (receiveCharacteristic != null) {
                    BluetoothGattDescriptor receiveConfigDescriptor =
                            receiveCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                    if (receiveConfigDescriptor != null) {
                        gatt.setCharacteristicNotification(receiveCharacteristic, true);

                        receiveConfigDescriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(receiveConfigDescriptor);
                    } else {
                        Log.e(TAG, "RSC receive config descriptor not found!");
                    }
                } else {
                    Log.e(TAG, "RSC receive characteristic not found!");
                }
                broadcastUpdateConnectionStatusChange(ACTION_CONNECTED,
                                                      gatt.getDevice().getAddress());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdateDataAvailable(characteristic, gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //  Log.d(TAG, "characteristic changed,...");
            broadcastUpdateDataAvailable(characteristic, gatt.getDevice().getAddress());
        }
    };



    private void broadcastUpdateConnectionStatusChange(final String action, final String mac) {
        final Intent intent = new Intent(action);

        intent.putExtra("MAC", mac);
        intent.putExtra("systemTimestamp", SystemClock.elapsedRealtimeNanos());

        sendBroadcast(intent, Manifest.permission.BLUETOOTH);
    }

    private void broadcastUpdateDataAvailable(final BluetoothGattCharacteristic characteristic,
                                 final String mac) {
        if (RSC_MEASUREMENT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {

            int offset = 0;
            final int flags = characteristic.getValue()[offset]; // 1 byte
            offset += 1;

            final Intent intent = new Intent(RSCService.ACTION_DATA_AVAILABLE);

            intent.putExtra("MAC", mac);

            final boolean islmPresent = (flags & INSTANTANEOUS_STRIDE_LENGTH_PRESENT) > 0;
            intent.putExtra("islmPresent", islmPresent);
            final boolean tdPresent = (flags & TOTAL_DISTANCE_PRESENT) > 0;

            intent.putExtra("tdPresent", tdPresent);
            final boolean running = (flags & WALKING_OR_RUNNING_STATUS_BITS) > 0;
            intent.putExtra("running", running);

            // flags: isInstrstrlengthpresent true
            // istotaldistanceresence: false
            // running: false

            // wird gesetzt
            // cadence (schrittfrequenz) ist immer 0
            // speed auch immer 0
            // stride length [cm]
            //

            // usere:
            // timestamp UINT32
            // stridex,y,z UINT16
            // sequence number uint16

            // / 256.0f * 3.6f; // 1/256 m/s in km/h
            final int instantaneousSpeed = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("instantaneousSpeed", instantaneousSpeed);


            offset += 2;

            final int instantaneousCadence = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT8, offset);
            intent.putExtra("instantaneousCadence", instantaneousCadence);
            offset += 1;

            final int instantaneousStrideLength = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("instantaneousStrideLength", instantaneousStrideLength);
            offset += 2;


            final int timestamp = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            intent.putExtra("timestamp", timestamp);
            offset += 4;

            final int strideX = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("strideX", strideX);
            offset += 2;
            final int strideY = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("strideY", strideY);
            offset += 2;
            final int strideZ = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("strideZ", strideZ);
            offset += 2;

            //sequence number is currently steo duration
            final int sequenceNumber = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("sequenceNumber", sequenceNumber);
            offset += 2;

            //sequence number is currently steo duration
            final int stepDuration = characteristic.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            intent.putExtra("stepDuration", stepDuration);

            //Log.d(TAG, "sequenceNUmber = " + sequenceNumber);

            intent.putExtra(EXTRA_DATA, characteristic.getValue());
            sendBroadcast(intent, Manifest.permission.BLUETOOTH);
        } else {
            Log.d(TAG, "unknown characteristic");
        }
    }

    class LocalBinder extends Binder {
        RSCService getService() {
            return RSCService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }


    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    // TODO return value is unused, remove?
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress)
                && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.");
            return bluetoothGatt.connect();
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bluetoothDeviceAddress = address;
        return true;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    // TODO why this function is not used outside?
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECTED);
        filter.addAction(ACTION_DISCONNECTED);
        filter.addAction(ACTION_DATA_AVAILABLE);
        return filter;
    }
}
