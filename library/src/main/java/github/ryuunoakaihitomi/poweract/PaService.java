package github.ryuunoakaihitomi.poweract;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

import java.util.Random;

public class PaService extends AccessibilityService {

    public static final String
            LOCK_SCREEN_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".LOCK_SCREEN_ACTION",
            POWER_DIALOG_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".POWER_DIALOG_ACTION",
            EXTRA_TOKEN = "extra_token";
    private static final String TAG = "PaService";
    static String token = "";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            String receivedToken = intent.getStringExtra(EXTRA_TOKEN);
            if (!token.equals(receivedToken)) {
                Log.e(TAG, "onReceive: Unauthorized token!  Received token is " + receivedToken);
                return;
            }
            if (POWER_DIALOG_ACTION.equals(intent.getAction())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    perform(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                }
            } else if (LOCK_SCREEN_ACTION.equals(intent.getAction())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    perform(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                }
            } else {
                Log.w(TAG, "onReceive: Unknown intent action.");
            }
        }
    };
    private boolean isBroadcastRegistered;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void perform(int action) {
        boolean result = performGlobalAction(action);
        // GLOBAL_ACTION_POWER_DIALOG = 6
        // GLOBAL_ACTION_LOCK_SCREEN = 8
        Log.i(TAG, "perform: Action " + action + " returned " + result);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;    // Keep alive in some env.
    }

    @Override
    protected void onServiceConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LOCK_SCREEN_ACTION);
            intentFilter.addAction(POWER_DIALOG_ACTION);
            registerReceiver(mBroadcastReceiver, intentFilter);
            isBroadcastRegistered = true;
            if (getResources().getBoolean(R.bool.poweract_accessibility_service_show_foreground_notification)) {
                loadForegroundNotification();
            }
        } else {
            Log.w(TAG, "onServiceConnected: Useless service enabled before 21.");
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (isBroadcastRegistered) {
            unregisterReceiver(mBroadcastReceiver);
            isBroadcastRegistered = false;
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final String channelTag = TextUtils.join(".", new String[]{BuildConfig.LIBRARY_PACKAGE_NAME, TAG});
                NotificationChannel channel =
                        new NotificationChannel(channelTag,
                                getString(R.string.poweract_accessibility_service_label),
                                NotificationManager.IMPORTANCE_MIN);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setShowBadge(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    channel.setAllowBubbles(false);
                }
                builder = new Notification.Builder(this, channelTag);
                manager.createNotificationChannel(channel);
            } else {
                builder = new Notification.Builder(this);
            }
            if (BuildConfig.DEBUG) {
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(this, 0,
                                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                builder.setContentIntent(pendingIntent);
            }
            builder.setSmallIcon(android.R.drawable.ic_lock_power_off)
                    /* Try our best to make the notification look less conspicuous. */
                    .setPriority(Notification.PRIORITY_MIN)     // Still has effect in 26+.
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setShowWhen(false)
                    .setLocalOnly(true)
                    .setOngoing(true)
                    .build();
            Notification foregroundNotification = builder.build();
            int id = new Random().nextInt();
            Log.i(TAG, "onServiceConnected: notification id = " + id);
            startForeground(id, foregroundNotification);
        }
    }
}
