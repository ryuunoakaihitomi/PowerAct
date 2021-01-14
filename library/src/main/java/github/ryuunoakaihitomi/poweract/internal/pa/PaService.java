package github.ryuunoakaihitomi.poweract.internal.pa;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
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
import android.system.Os;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.UUID;

import github.ryuunoakaihitomi.poweract.BuildConfig;
import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.R;
import github.ryuunoakaihitomi.poweract.internal.util.CallbackHelper;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

/**
 * <b>NOT FOR EXTERNAL USE!</b> It has to be public in order for system to call.
 * <p>
 * The {@link AccessibilityService} of PowerAct.
 * To provide the permission to show power dialog since 21 and lock screen since 28.
 *
 * @see AccessibilityService#GLOBAL_ACTION_POWER_DIALOG
 * @see AccessibilityService#GLOBAL_ACTION_LOCK_SCREEN
 * @since 1.0.0
 */
public final class PaService extends AccessibilityService {

    // There're 9 global actions in 29.
    private static final SparseArray<String> sGlobalActionMap;

    public static final String
            LOCK_SCREEN_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".LOCK_SCREEN_ACTION",
            POWER_DIALOG_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".POWER_DIALOG_ACTION",
            DISABLE_SERVICE_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + ".DISABLE_SERVICE_ACTION",
            EXTRA_TOKEN = "extra_token";
    private static final String TAG = "PaService";
    private static String token = "";
    private static Callback callback;
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
                        if (ActivityManager.isUserAMonkey()) {
                            DebugLog.e(TAG, "onReceive: Prevent monkey from showing power dialog.");
                            CallbackHelper.of(callback).failed();
                            break;
                        }
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
            // Consuming the callback in order to prevent memory leaks.
            callback = null;
        }
    };
    // It's too dirty.
    // But there's no more elegant way to get the check if the BroadcastReceiver is registered.
    // pm.queryBroadcastReceivers(intent, flags) only return the BR declared in AndroidManifest.xml.
    private static boolean sIsBroadcastRegistered;

    static {
        Class<?> as = AccessibilityService.class;
        SparseArray<String> globalActionMap = Utils.getClassIntApiConstant(as, "GLOBAL_ACTION");
        if (BuildConfig.DEBUG) {
            for (int i = 0; i < globalActionMap.size(); i++) {
                DebugLog.i(as.getSimpleName(), "Global Action: " +
                        Arrays.asList(globalActionMap.valueAt(i), globalActionMap.keyAt(i)));
            }
        }
        sGlobalActionMap = globalActionMap;
    }

    public static boolean sendAction(Context context, @Action String action, @Nullable Callback callback) {
        if (sIsBroadcastRegistered) {
            Intent intent = new Intent(action);
            intent.setPackage(context.getPackageName());
            String token = UUID.randomUUID().toString();
            DebugLog.d(TAG, "sendAction: Set token to " + token);
            PaService.token = token;
            PaService.callback = callback;
            intent.putExtra(PaService.EXTRA_TOKEN, token);
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            if (BuildConfig.DEBUG) {
                intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
            }
            context.sendBroadcast(intent);
        }
        return sIsBroadcastRegistered;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        sIsBroadcastRegistered = true;
        return super.registerReceiver(receiver, filter);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (sIsBroadcastRegistered) {
            super.unregisterReceiver(mBroadcastReceiver);
            sIsBroadcastRegistered = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void perform(int action) {
        boolean result = performGlobalAction(action);
        // GLOBAL_ACTION_POWER_DIALOG = 6
        // GLOBAL_ACTION_LOCK_SCREEN = 8
        /*
         * @see {@code frameworks/base/services/accessibility/java/com/android/server/accessibility/SystemActionPerformer.java}.
         */
        DebugLog.i(TAG, "perform: Action " + sGlobalActionMap.get(action) + " returned " + result);
        if (result) {
            CallbackHelper.of(callback).done();
        } else {
            CallbackHelper.of(callback).failed();
        }
    }

    @Override
    protected void onServiceConnected() {
        DebugLog.v(TAG, "onServiceConnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = new IntentFilter();
            // Only for ordered broadcast.
            //intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
            intentFilter.addAction(LOCK_SCREEN_ACTION);
            intentFilter.addAction(POWER_DIALOG_ACTION);
            intentFilter.addAction(DISABLE_SERVICE_ACTION);
            registerReceiver(mBroadcastReceiver, intentFilter);
            if (getResources().getBoolean(R.bool.poweract_accessibility_service_show_foreground_notification)) {
                loadForegroundNotification();
            }
            // I have no more other way to prevent it from being killed.
            // So add this line just as masturbation.
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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
    private @interface Action {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AppOpsManager appOps = getSystemService(AppOpsManager.class);
            int mode = Integer.MIN_VALUE;
            if (appOps != null) {
                try {
                    // https://android.googlesource.com/platform/frameworks/base/+/e9d9b4b9a27f419fbd6096698f692b474939cb48
                    // Add app op to control foreground services: OPSTR_START_FOREGROUND
                    final String op = AppOpsManager.permissionToOp(Manifest.permission.FOREGROUND_SERVICE);
                    final int uid = Os.getuid();
                    final String packageName = getPackageName();
                    if (op != null) {
                        mode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                                appOps.unsafeCheckOpNoThrow(op, uid, packageName) :
                                appOps.checkOpNoThrow(op, uid, packageName);
                    }
                } catch (IllegalArgumentException e) {
                    // "Unknown operation string: "
                    DebugLog.e(TAG, "loadForegroundNotification: " + e.getMessage(), e);
                }
            }
            if (mode != AppOpsManager.MODE_ALLOWED) {
                DebugLog.e(TAG, "loadForegroundNotification: The foreground service may be disabled by AppOps's restriction." +
                        " mode = " + Utils.getClassIntApiConstantString(AppOpsManager.class, "MODE", mode));
            }
        }
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
            int id = Utils.randomNonZero();
            DebugLog.i(TAG, "onServiceConnected: notification id = " + id);
            startForeground(id, foregroundNotification);
        }
    }
}
