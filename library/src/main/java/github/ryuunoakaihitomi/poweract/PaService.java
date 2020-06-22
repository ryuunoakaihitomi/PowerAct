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
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Random;
import java.util.UUID;

public final class PaService extends AccessibilityService {

    public static final String
            LOCK_SCREEN_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".LOCK_SCREEN_ACTION",
            POWER_DIALOG_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".POWER_DIALOG_ACTION",
            DISABLE_SERVICE_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".DISABLE_SERVICE_ACTION",
            EXTRA_TOKEN = "extra_token";
    private static final String TAG = "PaService";
    static String token = "";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.d(TAG, "onReceive: " + intent);
            String receivedToken = intent.getStringExtra(EXTRA_TOKEN);
            if (!token.equals(receivedToken)) {
                DebugLog.e(TAG, "onReceive: Unauthorized token!  Received token is " + receivedToken);
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DebugLog.e(TAG, "onReceive: action is null!");
                return;
            }
            switch (action) {
                case POWER_DIALOG_ACTION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // On Wear OS before 28, it will call out the voice assistant
                        // instead of the system power dialog.
                        perform(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                    }
                    break;
                case LOCK_SCREEN_ACTION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        perform(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                    }
                    break;
                case DISABLE_SERVICE_ACTION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        disableSelf();
                    } else {
                        DebugLog.w(TAG, "onReceive: Cannot disable it before 24.");
//                        stopSelf();
                        stopForeground(true);
                        unregisterReceiver(this);
                    }
                    break;
                default:
                    DebugLog.w(TAG, "onReceive: Unknown intent action.");
            }
        }
    };

    static void sendAction(Context context, @ActionType String action) {
        Intent intent = new Intent(action);
        intent.setPackage(context.getPackageName());
        String token = UUID.randomUUID().toString();
        DebugLog.d(TAG, "sendAction: Set token to " + token);
        PaService.token = token;
        intent.putExtra(PaService.EXTRA_TOKEN, token);
        context.sendBroadcast(intent);
    }

    private boolean isBroadcastRegistered;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void perform(int action) {
        boolean result = performGlobalAction(action);
        // GLOBAL_ACTION_POWER_DIALOG = 6
        // GLOBAL_ACTION_LOCK_SCREEN = 8
        DebugLog.i(TAG, "perform: Action " + action + " returned " + result);
    }

    @Override
    protected void onServiceConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LOCK_SCREEN_ACTION);
            intentFilter.addAction(POWER_DIALOG_ACTION);
            intentFilter.addAction(DISABLE_SERVICE_ACTION);
            registerReceiver(mBroadcastReceiver, intentFilter);
            isBroadcastRegistered = true;
            if (getResources().getBoolean(R.bool.poweract_accessibility_service_show_foreground_notification)) {
                loadForegroundNotification();
            }
        } else {
            DebugLog.w(TAG, "onServiceConnected: Useless service enabled before 21.");
            Utils.setComponentEnabled(getApplicationContext(), this.getClass(), false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;    // Keep alive in some env.
    }

    @StringDef({
            LOCK_SCREEN_ACTION,
            POWER_DIALOG_ACTION,
            DISABLE_SERVICE_ACTION
    })
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    private @interface ActionType {
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
            int id = randomNonZero();
            DebugLog.i(TAG, "onServiceConnected: notification id = " + id);
            startForeground(id, foregroundNotification);
        }
    }

    private int randomNonZero() {
        int ret = new Random().nextInt();
        return ret == 0 ? randomNonZero() : ret;
    }
}
