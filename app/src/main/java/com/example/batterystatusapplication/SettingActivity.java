package com.example.batterystatusapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.batterystatusapplication.BroadcastReceiver.BatteryReceiver;

public class SettingActivity extends AppCompatActivity {
    EditText edtLowBatteryThreshold;
    Button btnSaveSettings;
    CheckBox chkEnableLowBatteryNotification, chkEnableAutoOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        chkEnableLowBatteryNotification = findViewById(R.id.chkEnableLowBatteryNotification);
        chkEnableAutoOpen = findViewById(R.id.chkEnableAutoOpen);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        edtLowBatteryThreshold = findViewById(R.id.edtLowBatteryThreshold);

        SharedPreferences prefs = getSharedPreferences("BatteryPrefs", MODE_PRIVATE);
        chkEnableLowBatteryNotification.setChecked(prefs.getBoolean("enableLowBatteryNotification", true));
        chkEnableAutoOpen.setChecked(prefs.getBoolean("enableAutoOpen", false));
        edtLowBatteryThreshold.setText(String.valueOf(prefs.getInt("low_battery_threshold", 20)));

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int threshold = Integer.parseInt(edtLowBatteryThreshold.getText().toString());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("enableLowBatteryNotification", chkEnableLowBatteryNotification.isChecked());
                editor.putBoolean("enableAutoOpen", chkEnableAutoOpen.isChecked());
                editor.putInt("low_battery_threshold", threshold);
                editor.commit();
                Toast.makeText(SettingActivity.this, "Đã lưu cài đặt", Toast.LENGTH_SHORT).show();
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(new BatteryReceiver(new BatteryReceiver.BatteryListener() {
                    @Override
                    public void onBatteryChanged(int batteryPct, int status) {

                    }
                }), filter);
                finish();
            }
        });
    }
}