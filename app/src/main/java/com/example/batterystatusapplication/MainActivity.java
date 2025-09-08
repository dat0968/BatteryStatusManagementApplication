package com.example.batterystatusapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.batterystatusapplication.BroadcastReceiver.BatteryReceiver;

public class MainActivity extends AppCompatActivity {

    private TextView txtBatteryLevel, txtChargingStatus, txtLowBatteryWarning;
    private ProgressBar progressBattery;
    private ImageButton btnSettings;
    private ImageView imgBattery;

    private BatteryReceiver batteryReceiver;
    private int lowBatteryThreshold = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtBatteryLevel = findViewById(R.id.txtBatteryLevel);
        txtChargingStatus = findViewById(R.id.txtChargingStatus);
        txtLowBatteryWarning = findViewById(R.id.txtLowBatteryWarning);
        progressBattery = findViewById(R.id.progressBattery);
        imgBattery = findViewById(R.id.imgBattery);
        btnSettings = findViewById(R.id.btnSettings);
        // Tạo BatteryReceiver
        batteryReceiver = new BatteryReceiver(new BatteryReceiver.BatteryListener() {
            @Override
            public void onBatteryChanged(int batteryPct, int status) {
                txtBatteryLevel.setText("Mức pin: " + batteryPct + "%");
                progressBattery.setProgress(batteryPct);
                String statusText;
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusText = "Đang sạc";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusText = "Pin đầy";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusText = "Không sạc";
                        break;
                    default:
                        statusText = "Không xác định";
                        break;
                }
                txtChargingStatus.setText("Trạng thái: " + statusText);
            }
        });
        // Xác nhận loại BroadCast muốn nhận. Ở đây ta lọc theo action
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(myintent);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}