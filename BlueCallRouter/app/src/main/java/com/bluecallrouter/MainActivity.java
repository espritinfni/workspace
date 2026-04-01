package com.bluecallrouter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView statusText;
    private Button toggleButton;
    private boolean serviceRunning = false;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);

        toggleButton.setOnClickListener(v -> toggleService());

        requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : results) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    statusText.setText("Permissions refusées. L'app ne peut pas fonctionner.");
                    toggleButton.setEnabled(false);
                    return;
                }
            }
            updateStatus();
        }
    }

    private void toggleService() {
        Intent serviceIntent = new Intent(this, BluetoothAudioService.class);
        if (serviceRunning) {
            stopService(serviceIntent);
            serviceRunning = false;
        } else {
            startForegroundService(serviceIntent);
            serviceRunning = true;
        }
        updateStatus();
    }

    private void updateStatus() {
        StringBuilder sb = new StringBuilder();

        // Bluetooth status
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            sb.append("Bluetooth : Non disponible\n");
        } else if (!adapter.isEnabled()) {
            sb.append("Bluetooth : Désactivé\n");
        } else {
            sb.append("Bluetooth : Activé\n");
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                for (BluetoothDevice device : adapter.getBondedDevices()) {
                    sb.append("  → ").append(device.getName()).append("\n");
                }
            }
        }

        // Audio status
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        sb.append("\nBluetooth SCO : ").append(am.isBluetoothScoOn() ? "ON" : "OFF").append("\n");

        // Service status
        sb.append("\nService : ").append(serviceRunning ? "ACTIF ✓" : "INACTIF");

        statusText.setText(sb.toString());
        toggleButton.setText(serviceRunning ? "Désactiver" : "Activer");
    }
}
