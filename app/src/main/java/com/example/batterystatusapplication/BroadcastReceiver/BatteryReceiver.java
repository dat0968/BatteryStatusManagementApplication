package com.example.batterystatusapplication.BroadcastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.batterystatusapplication.MainActivity;

public class BatteryReceiver extends BroadcastReceiver {
    public interface BatteryListener {
        void onBatteryChanged(int batteryPct, int status);
    }

    private BatteryListener listener;
    private static int lastBatteryLevel = -1;
    private static  int lastthreshold = -1;
    private static boolean lowBatteryNotified = false;
    private static int lastNotifiedBatteryLevel = -1;

    // Truyền callback qua constructor
    public BatteryReceiver(BatteryListener listener) {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int batteryPct = (int) ((level / (float) scale) * 100);
        // Đọc settings cấu hình
        SharedPreferences prefs = context.getSharedPreferences("BatteryPrefs", Context.MODE_PRIVATE);
        boolean enableNotification = prefs.getBoolean("enableLowBatteryNotification", true);
        boolean enableAutoOpen = prefs.getBoolean("enableAutoOpen", false);
        int threshold = prefs.getInt("low_battery_threshold", 20);

        Log.d("BatteryReceiver", "Mức pin: " + batteryPct + "%");
        if (listener != null) {
            listener.onBatteryChanged(batteryPct, status);
        }
        if (batteryPct == lastBatteryLevel && lastthreshold == threshold) return;
        lastBatteryLevel = batteryPct;
        lastthreshold = threshold;
        if (batteryPct <= threshold && !lowBatteryNotified && enableNotification) {
            if (batteryPct != lastNotifiedBatteryLevel) {
                showLowBatteryNotification(context, batteryPct);
                lowBatteryNotified = true;
                lastNotifiedBatteryLevel = batteryPct; // ghi nhận mức pin đã thông báo
            }
        }
        if (batteryPct > threshold) {
            lowBatteryNotified = false;
        }
        // Nếu bật auto open và đang cắm sạc
        if (enableAutoOpen && status == BatteryManager.BATTERY_STATUS_CHARGING) {
            Intent i = new Intent(context, MainActivity.class);
            /* Vì BatteryReceiver không có UI thread nên không có Task (ngăn xếp UI)
            ta cần tạo một task cho nó chứa Activity => dùng addFlags */
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            showChargingNotification(context, pendingIntent);
        }
    }
    private void showChargingNotification(Context context, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("BatteryReceiver", "POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        String channelId = "charging_channel";
        String channelName = "Charging Status";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Đang sạc")
                .setContentText("Thiết bị đang sạc, nhấn để mở ứng dụng")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (manager != null) {
            manager.notify(2, notification);
        }
    }
    private void showLowBatteryNotification(Context context, int batteryPct){
        String channelId = "battery_channel";
        String channelName = "Battery Status";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 trở lên bắt buộc tạo channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Tạo notification
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Cảnh báo pin yếu")
                .setContentText("Pin còn " + batteryPct + "%, hãy sạc ngay!")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        // Hiển thị notification
        if (manager != null) {
            manager.notify(1, notification);
        }
    }
}
