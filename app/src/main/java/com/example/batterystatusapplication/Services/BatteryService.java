package com.example.batterystatusapplication.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.batterystatusapplication.R;

import java.security.Provider;

public class BatteryService extends Service {
    private BroadcastReceiver batteryReceiver;
    private WindowManager windowManager;
    private View overlayView;
    private boolean overlayDismissed = false;
    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Lắng nghe pin
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) ((level / (float) scale) * 100);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    Log.d("BatteryService", "Đang cắm sạc");
                    showChargingOverlay(batteryPct);
                }
                else {
                    removeOverlay(); // Rút sạc → tắt overlay
                }
            }
        };

        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void startForegroundService() {
        String channelId = "battery_service_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Battery Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Battery Service")
                .setContentText("Đang theo dõi trạng thái pin")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);
    }

    private void showChargingOverlay(int batteryPct) {
        if (overlayView != null || overlayDismissed) return; // đã hiển thị
        if (!Settings.canDrawOverlays(this)) return; // kiểm tra quyền
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_charging, null);
        TextView tvBatteryPercent =  overlayView.findViewById(R.id.tvBatteryPercent);
        tvBatteryPercent.setText(batteryPct + "%");
        Button btnClose = overlayView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            overlayDismissed = true; // đánh dấu đã đóng
            removeOverlay();
        });

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(overlayView, params);
    }

    private void removeOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) unregisterReceiver(batteryReceiver);
        removeOverlay();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
