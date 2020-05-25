package github.ryuunoakaihitomi.poweract;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Random;

public class PaService extends AccessibilityService {

    public static final String
            LOCK_SCREEN_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".LOCK_SCREEN_ACTION",
            POWER_DIALOG_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".POWER_DIALOG_ACTION";
    private static final String TAG = "PaService";
    private static final String FOREGROUND_NOTIFICATION_CHANNEL_TAG =
            TextUtils.join(".", new String[]{BuildConfig.LIBRARY_PACKAGE_NAME, TAG});

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            if (POWER_DIALOG_ACTION.equals(intent.getAction())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;    // Keep alive before 19.
    }

    @Override
    protected void onServiceConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LOCK_SCREEN_ACTION);
            intentFilter.addAction(POWER_DIALOG_ACTION);
            registerReceiver(mBroadcastReceiver, intentFilter);

            if (getResources().getBoolean(R.bool.poweract_accessibility_service_show_foreground_notification)) {
                loadForegroundNotification();
            }
        } else {
            Log.w(TAG, "onServiceConnected: Useless service enabled before 21.");
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mBroadcastReceiver);
        stopForeground(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadForegroundNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel =
                        new NotificationChannel(FOREGROUND_NOTIFICATION_CHANNEL_TAG,
                                getString(R.string.poweract_accessibility_service_label),
                                NotificationManager.IMPORTANCE_MIN);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setShowBadge(false);
                builder = new Notification.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_TAG);
                manager.createNotificationChannel(channel);
            } else {
                builder = new Notification.Builder(this);
            }
            builder.setSmallIcon(android.R.drawable.ic_lock_power_off)
                    .setPriority(Notification.PRIORITY_MIN) // Still in effect in 26+.
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .build();
            Notification foregroundNotification = builder.build();
            int id = new Random().nextInt();
            Log.i(TAG, "onServiceConnected: notification id = " + id);
            startForeground(id, foregroundNotification);
        }
    }
}
