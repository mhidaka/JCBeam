/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.jcrom.jcbeam.nfc;

import com.jcrom.jcbeam.NfcActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class SimpleWiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private NfcActivity activity;

    /**
     * @param activity activity associated with the receiver
     */
    public SimpleWiFiDirectBroadcastReceiver( NfcActivity activity) {
        super();
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //自分のWi-Fi Directデバイス情報

            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            activity.updateThisDevice(device);
            Log.d(NfcActivity.TAG, "P2P this device changed");
            Log.d(NfcActivity.TAG, "name   : " + device.deviceName);
            Log.d(NfcActivity.TAG, "address: " + device.deviceAddress);
            Log.d(NfcActivity.TAG, "status : " + Integer.toString(device.status) );
        }
    }
}
