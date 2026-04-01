package com.bluecallrouter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "BlueCallRouter";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state == null) return;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)
                || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            Log.d(TAG, "Call detected, routing audio to Bluetooth SCO");
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Log.d(TAG, "Call ended, releasing Bluetooth SCO");
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }
}
