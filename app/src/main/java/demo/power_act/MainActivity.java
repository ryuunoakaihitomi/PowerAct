package demo.power_act;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import github.ryuunoakaihitomi.poweract.Callback;
import github.ryuunoakaihitomi.poweract.ExternalUtils;
import github.ryuunoakaihitomi.poweract.PowerAct;
import github.ryuunoakaihitomi.poweract.PowerActX;
import github.ryuunoakaihitomi.poweract.PowerButton;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Toast.makeText(getApplicationContext(), "Denied", Toast.LENGTH_SHORT).show();
            }
        };

        Activity activity = this;

        // PowerAct examples
        lockScreenBtn.setOnClickListener(v -> {
            // Lock screen, without callback.
            PowerAct.lockScreen(activity);
        });
        powerDialogBtn.setOnClickListener(v -> {
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

        findViewById(R.id.disComBtn).setOnClickListener(v ->
                // Disable exposed components manually.
                ExternalUtils.disableExposedComponents(getApplicationContext()));

        // Control logcat output.
        ExternalUtils.enableLog(true);
    }

    private void setXButtonAction(int btnResId, Runnable action, Runnable longClickAction) {
        Button button = findViewById(btnResId);
        button.setOnClickListener(v -> action.run());
        if (longClickAction != null) {
            button.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
    }

    //<editor-fold desc="This part is not important for showing the library's usage.">
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
        switch (item.getItemId()) {
            /* For debugging */
            case R.id.info:
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
                        .setTitle("The version info of the app.")
                        .setMessage(info.toString())
                        .show();
                break;
            /* Readme inside */
            case R.id.about:
                final String logo = "PowerAct", link = "https://github.com/ryuunoakaihitomi/PowerAct";
                SpannableString text = new SpannableString("A demo app of the android library " + logo + ".");
                final int logoStart = text.length() - logo.length() - 1,
                        logoEnd = text.length() - 1,
                        flag = Spanned.SPAN_INCLUSIVE_INCLUSIVE;
                text.setSpan(new StyleSpan(Typeface.BOLD), logoStart, logoEnd, flag);
                text.setSpan(new ForegroundColorSpan(Color.GREEN), logoStart, logoEnd, flag);
                text.setSpan(new BackgroundColorSpan(Color.BLACK), logoStart, logoEnd, flag);
                new AlertDialog.Builder(this)
                        .setTitle("What's this?")
                        .setMessage(text)
                        .setPositiveButton("Link", (dialog, which) -> {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                            } catch (ActivityNotFoundException e) {
                                Log.e(TAG, "onClick: " + link, e);
                            }
                        })
                        .show();
                break;
            default:
                return false;
        }
        return true;
    }
    //</editor-fold>
}
