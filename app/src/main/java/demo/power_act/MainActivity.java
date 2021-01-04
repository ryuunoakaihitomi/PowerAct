package demo.power_act;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.UiModeManager;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.PowerManager;
import android.provider.Browser;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import java.util.HashMap;
import java.util.Map;

import demo.power_act.util.EmptyAccessibilityDelegate;
import demo.power_act.util.Utils;
import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.ExternalUtils;
import github.ryuunoakaihitomi.poweract.PowerAct;
import github.ryuunoakaihitomi.poweract.PowerActX;
import github.ryuunoakaihitomi.poweract.PowerButton;
import moe.shizuku.api.ShizukuApiConstants;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_PERMISSION_SHIZUKU = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        foolProof();
        setContentView(R.layout.activity_main);

        Button
                lockScreenBtn = findViewById(R.id.lockScreenBtn),
                powerDialogBtn = findViewById(R.id.powerDialogBtn),
                rebootBtn = findViewById(R.id.rebootBtn);

        // Create callback object
        final Callback callback = new Callback() {

            @Override
            public void done() {
                Log.i(TAG, "done: PowerAct cb DONE! -------");
                finish();
            }

            @Override
            public void failed() {
                Log.e(TAG, "failed: PowerAct cb FAILED! *******");
                // E/ContextImpl: java.lang.IllegalAccessException: Tried to access visual service WindowManager from a non-visual Context:
                // Visual services, such as WindowManager, WallpaperService or LayoutInflater should be accessed from Activity or other visual Context.
                // Use an Activity or a Context created with Context#createWindowContext(int, Bundle), which are adjusted to the configuration and visual bounds of an area on screen.
                Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_SHORT).show();
            }
        };

        Activity activity = this;

        // PowerAct examples
        lockScreenBtn.setOnClickListener(v -> {
            tipForAccessibilityService();
            // Lock screen, without callback.
            PowerAct.lockScreen(activity);
        });
        powerDialogBtn.setOnClickListener(v -> {
            tipForAccessibilityService();
            // Show system power dialog, with callback.
            PowerAct.showPowerDialog(activity, callback);
        });
        rebootBtn.setOnClickListener(v -> PowerAct.reboot(activity));

        // An additional widget.
        PowerButton powerButton = findViewById(R.id.pwrBtn);
        powerButton.setOnClickListener(v -> Log.d(TAG, "onClick: pwrBtn"));
        powerButton.setOnLongClickListener(v -> {
            Log.d(TAG, "onLongClick: pwrBtn");
            return false;
        });
        /* Change the size of PowerButton. */
        powerButton.unlockSize();
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        final int customSize = Math.min(p.x, p.y) >> 1;
        Log.d(TAG, "onCreate: The size of PowerButton is " + customSize);
        powerButton.setHeight(customSize);
        powerButton.setWidth(customSize);
        // ExternalUtils.isExposedComponentsAvailable
        if (!ExternalUtils.isExposedComponentAvailable(this)) {
            Log.w(TAG, "onCreate: Disabled component(s)");
            powerButton.setVisibility(View.GONE);
        }

        // PowerActX examples
        /*
         * Usage:
         * PowerActX.<action>()
         * PowerActX.<action>(callback)
         * If force mode available:
         * PowerActX.<action>(force)
         * PowerActX.<action>(callback, force)
         */
        setXButtonAction(R.id.ls_btn, () -> PowerActX.lockScreen(callback), null);
        setXButtonAction(R.id.rb_btn, () -> PowerActX.reboot(callback), () -> PowerActX.reboot(callback, true));
        setXButtonAction(R.id.sd_btn, () -> PowerActX.shutdown(callback), () -> PowerActX.shutdown(callback, true));
        setXButtonAction(R.id.rec_btn, () -> PowerActX.recovery(callback), () -> PowerActX.recovery(callback, true));
        setXButtonAction(R.id.bl_btn, () -> PowerActX.bootloader(callback), () -> PowerActX.bootloader(callback, true));
        setXButtonAction(R.id.sm_btn, () -> PowerActX.safeMode(callback), () -> PowerActX.safeMode(callback, true));
        setXButtonAction(R.id.srb_btn, () -> PowerActX.softReboot(callback), null);
        setXButtonAction(R.id.rsu_btn, () -> PowerActX.restartSystemUi(callback), null);

        findViewById(R.id.disComBtn).setOnClickListener(v ->
                // Disable exposed components manually.
                ExternalUtils.disableExposedComponents(getApplicationContext()));

        // Control logcat output.
        Switch enableLogSwitch = findViewById(R.id.enableLog);
        enableLogSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> ExternalUtils.enableLog(isChecked));

        // Security mechanism against Accessibility Service attack.
        getWindow().getDecorView().setAccessibilityDelegate(new EmptyAccessibilityDelegate());
    }

    private void setXButtonAction(int btnResId, Runnable action, Runnable longClickAction) {
        Button button = findViewById(btnResId);
        button.setOnClickListener(v -> {
            // We must request the permission manually if we want to "PowerActX" with Shizuku.
            requestShizukuPermission();
            tipForRootAccess();
            action.run();
        });
        if (longClickAction != null) {
            button.setOnLongClickListener(v -> {
                requestShizukuPermission();
                tipForRootAccess();
                longClickAction.run();
                return true;
            });
        }
    }

    private void tipForAccessibilityService() {
        // User guide.
        ExternalUtils.setUserGuideRunnable(() -> Toast.makeText(this, "Please enable the accessibility service.", Toast.LENGTH_LONG).show());
    }

    private void tipForRootAccess() {
        ExternalUtils.setUserGuideRunnable(() -> Toast.makeText(this, "Please grant the root permission.", Toast.LENGTH_SHORT).show());
    }

    private void requestShizukuPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(ShizukuApiConstants.PERMISSION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_SHIZUKU);
        }
    }

    //<editor-fold desc="This part is not important for showing the library's usage.">

    /**
     * Using the simplest and most effective way as a fool-proofing design.
     * If the device does not have a screen lock login password,
     * then it is assumed that it is a test device and these steps are skipped.
     */
    private void foolProof() {
        final String
                opHint = " Press back key to enter.",
                caution = "Please save all your work before proceeding!";
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                new BiometricPrompt.Builder(this)
                        .setTitle(caution)
                        .setSubtitle(opHint)
                        .setDeviceCredentialAllowed(true)
                        .build().authenticate(new CancellationSignal(), command -> {
                }, new BiometricPrompt.AuthenticationCallback() {
                });
            } else {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(caution, opHint);
                if (intent != null) startActivityForResult(intent, 123);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(caution)
                    .setMessage(opHint)
                    .setCancelable(false)
                    .setOnKeyListener((dialog, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK) dialog.cancel();
                        return true;
                    });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (keyguardManager.isKeyguardSecure()) builder.show();
            } else {
                // ¯\_(ツ)_/¯
                builder.show();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        /* Show which view is focused on Android TV. */
        Log.d(TAG, "onKeyUp: " + KeyEvent.keyCodeToString(keyCode));
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            View focusedView = findViewById(R.id.root_layout).findFocus();
            ViewDebug.dumpCapturedView(TAG, focusedView);
            if (focusedView instanceof Button) {
                Log.d(TAG, "onKeyUp: [" + ((Button) focusedView).getText() + "] focused.");
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        /* For debugging */
        // Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements.
        if (itemId == R.id.info) {
            StringBuilder info = new StringBuilder();
            final String timeFormat = "yyyy-MM-dd HH:mm:ss:SSS";
            PackageInfo myPkgInfo = new PackageInfo();
            try {
                myPkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("Build Type", BuildConfig.BUILD_TYPE);
            infoMap.put("Build Time", Utils.timestamp2String(timeFormat, BuildConfig.BUILD_TIME));
            infoMap.put("First Install Time", Utils.timestamp2String(timeFormat, myPkgInfo.firstInstallTime));
            infoMap.put("Last Update Time", Utils.timestamp2String(timeFormat, myPkgInfo.lastUpdateTime));
            for (Map.Entry<String, String> entry : infoMap.entrySet()) {
                info.append(entry.getKey())
                        .append(":\n\t")
                        .append(entry.getValue())
                        .append("\n\n");
            }
            new AlertDialog.Builder(this)
                    .setMessage(info.toString())
                    .show();
            Toast.makeText(this, Build.FINGERPRINT, Toast.LENGTH_LONG).show();
            /* Readme inside */
        } else if (itemId == R.id.about) {
            final int flag = Spanned.SPAN_INCLUSIVE_INCLUSIVE;

            /* title */
            final String titleText = "What's this?";
            SpannableString title = new SpannableString(titleText);
            title.setSpan(new ForegroundColorSpan(Color.WHITE), 0, titleText.length(), flag);
            title.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titleText.length(), flag);

            /* content */
            final String
                    logo = "PowerAct",
                    link = "https://github.com/ryuunoakaihitomi/PowerAct",
                    contentText = "A demo app of the android library " + logo + ".";
            SpannableString content = new SpannableString(contentText);
            content.setSpan(new ForegroundColorSpan(Color.GRAY), 0, contentText.length(), flag);

            /* PALETTE: Change the color styles of About Dialog by wallpaper and dark mode. (just for curiosity) */
            @ColorInt int
                    logoColor = Color.GREEN,
                    textColor = Color.WHITE,
                    bgColor = Color.RED;
            palette:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                if (getSystemService(PowerManager.class).isPowerSaveMode()) {
                    Log.i(TAG, "onOptionsItemSelected: Using default colors in power save mode.");
                    break palette;
                }
                UiModeManager uiModeManager = getSystemService(UiModeManager.class);
                final boolean nightMode = uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
                Log.d(TAG, "onOptionsItemSelected: night mode = " + nightMode);
                WallpaperColors colors = WallpaperManager.getInstance(this).getWallpaperColors(
                        nightMode ? WallpaperManager.FLAG_LOCK : WallpaperManager.FLAG_SYSTEM);
                if (colors != null) {
                    final Color
                            pri = colors.getPrimaryColor(),
                            sec = colors.getSecondaryColor(),
                            ter = colors.getTertiaryColor();
                    Log.d(TAG, "onOptionsItemSelected: text (pri) " + pri);
                    textColor = pri.toArgb();
                    if (sec != null) {
                        Log.d(TAG, "onOptionsItemSelected: background (sec) " + sec);
                        bgColor = sec.toArgb();
                    }
                    if (ter != null) {
                        Log.d(TAG, "onOptionsItemSelected: logo (ter) " + ter);
                        logoColor = ter.toArgb();
                    }
                }
            } else {
                Log.d(TAG, "onOptionsItemSelected: normal colors below 27");
            }

            /* logo */
            final int
                    logoStart = contentText.indexOf(logo),
                    logoEnd = contentText.indexOf(logo) + logo.length();
            content.setSpan(new StyleSpan(Typeface.BOLD), logoStart, logoEnd, flag);
            content.setSpan(new ForegroundColorSpan(logoColor), logoStart, logoEnd, flag);
            TypefaceSpan monospaceSpan = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                    new TypefaceSpan(Typeface.MONOSPACE) :
                    new TypefaceSpan("monospace");
            content.setSpan(monospaceSpan, logoStart, logoEnd, flag);

            AlertDialog aboutDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(content)
                    .setPositiveButton("Link", (dialog, which) -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "onClick: " + link, e);
                            Browser.sendString(this, link);
                        }
                    })
                    .create();

            aboutDialog.show();

            /* button */
            Button linkBtn = aboutDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            linkBtn.setAllCaps(false);
            linkBtn.setTextColor(textColor);
            linkBtn.setBackgroundColor(bgColor);
            linkBtn.setTypeface(Typeface.DEFAULT_BOLD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                linkBtn.setTooltipText(link);
            }

            /* background */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Window w = aboutDialog.getWindow();
                if (w != null)
                    w.getDecorView().setBackgroundResource(R.drawable.about_dialog_bg);
            }
        } else {
            return false;
        }
        return true;
    }
    //</editor-fold>
}
