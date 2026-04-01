package com.bluecallrouter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class BluetoothAudioService extends Service {
    private static final String CHANNEL_ID = "bluecallrouter_channel";
    private CallReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        callReceiver = new CallReceiver();
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("BlueCallRouter")
                .setContentText("Routage audio Bluetooth actif")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "BlueCallRouter Service",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Notification pour le service de routage audio Bluetooth");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
