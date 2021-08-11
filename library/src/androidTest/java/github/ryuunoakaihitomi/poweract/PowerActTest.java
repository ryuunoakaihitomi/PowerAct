package github.ryuunoakaihitomi.poweract;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.N;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.app.UiAutomation;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.FlakyTest;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.Suppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import github.ryuunoakaihitomi.poweract.internal.pa.PaReceiver;
import github.ryuunoakaihitomi.poweract.internal.util.LibraryCompat;
import github.ryuunoakaihitomi.poweract.internal.util.ReflectionUtils;
import github.ryuunoakaihitomi.poweract.test.BaseTest;
import github.ryuunoakaihitomi.poweract.test.CommonUtils;
import github.ryuunoakaihitomi.poweract.test.LockScreenTest;
import poweract.test.res.PlaygroundActivity;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuProvider;

@LargeTest
@SdkSuppress(minSdkVersion = JELLY_BEAN_MR2)    // Ui automator required.
public final class PowerActTest extends BaseTest {

    private static final String TAG = "PowerActTest";

    @Rule
    public final ActivityScenarioRule<PlaygroundActivity> rule = new ActivityScenarioRule<>(PlaygroundActivity.class);

    // It doesn't work...
    @Rule
    public final GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(ShizukuProvider.PERMISSION);

    private UiDevice mUiDevice;

    @Before
    public void prepareUiDeviceAndWakeUp() throws RemoteException {
        if (mUiDevice == null) {
            Configurator.getInstance().setUiAutomationFlags(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
        // Please clear password before test. Settings -> Security -> Screen lock -> None
        mUiDevice.wakeUp();
    }

    @Test(timeout = LockScreenTest.WAIT_TIME_MILLIS)
    public void lockScreen() throws InterruptedException {
        LockScreenTest test = new LockScreenTest(callback -> {
            rule.getScenario().onActivity(activity -> activity.runOnUiThread(() -> PowerAct.lockScreen(activity, callback)));
            if (LibraryCompat.isShizukuPrepared()) {
                Log.d(TAG, "lockScreen: Shizuku running...");
                grantShizukuPermission();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                final String addDevAdminBtnText = CommonUtils.getStringResource(targetContext, CommonUtils.PKG_NAME_SETTINGS, "add_device_admin");
                Log.i(TAG, "lockScreen: addDevAdminBtnText = " + addDevAdminBtnText);
                final UiObject addAdminActionBtn = mUiDevice.findObject(new UiSelector().text(addDevAdminBtnText));
                if (addAdminActionBtn.exists()) {
                    Log.d(TAG, "lockScreen: Click add device admin action button.");
                    addAdminActionBtn.click();
                }
            } else {
                Log.d(TAG, "lockScreen: Enable accessibility service by uiautomator.");
                enableAccessibilityService();
            }
        });
        test.test();

        /* Remove admin for uninstalling. */
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) targetContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        assert devicePolicyManager != null;
        devicePolicyManager.removeActiveAdmin(new ComponentName(targetContext, PaReceiver.class));
    }

    @SdkSuppress(minSdkVersion = N) // Cannot enable accessibility service before 24.
    @Test
    public void showPowerDialog() throws Throwable {
        callHiddenApiBypass();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Callback callback = new Callback() {

            @Override
            public void done() {
                countDownLatch.countDown();
            }

            @Override
            public void failed() {
                fail("showPowerDialog");
            }
        };

        rule.getScenario().onActivity(activity -> PowerAct.showPowerDialog(activity, callback));
        if (LibraryCompat.isShizukuPrepared() && !ReflectionUtils.hasHiddenApiRestriction()) {
            grantShizukuPermission();
        } else {
            try {
                enableAccessibilityService();
            } catch (UiObjectNotFoundException e) {
                fail(e.getMessage());
            }
        }
        countDownLatch.await();

        /* Check if it's system ui. (The system power dialog) */
        final String currentPackageName = mUiDevice.getCurrentPackageName();
        Log.d(TAG, "showPowerDialog: Test end. currentPackageName = " + currentPackageName);
        assertThat(currentPackageName, anyOf(is("android"), is("com.android.systemui")));
        // Dump window hierarchy to logcat.
        CommonUtils.dumpWindowHierarchy2Stdout(mUiDevice);
        /* Check "Power Off" item. */
        final Resources sysRes = Resources.getSystem();
        final String powerOffText = sysRes.getString(sysRes.getIdentifier("power_off", "string", "android"));
        Log.d(TAG, "showPowerDialog: powerOffText = " + powerOffText);
        assertTrue(mUiDevice.hasObject(By.text(powerOffText)));
        // Exit power dialog.
        mUiDevice.pressBack();
    }

    @FlakyTest(detail = "The case will change the power state of the device.")
    @SdkSuppress(minSdkVersion = N)
    @Suppress   // It will cut off the entire test process. Be careful if you want to test reboot().
    @Test
    public void reboot() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && LibraryCompat.isShizukuPrepared()) {
            Shizuku.requestPermission(1);
            grantShizukuPermission();
            assertEquals("Shell does not have REBOOT permission.",
                    PackageManager.PERMISSION_GRANTED, Shizuku.checkRemotePermission(Manifest.permission.REBOOT));
        } else {
            final String shell = "dpm set-device-owner " + new ComponentName(targetContext, PaReceiver.class).flattenToShortString();
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(shell).close();
            TimeUnit.SECONDS.sleep(1);
        }
        rule.getScenario().onActivity(activity -> {
            PowerAct.reboot(activity, () -> fail("reboot() callback"));
            // Reboot is NOT stuck in Shizuku with 30.
            fail("reboot() doesn't work.");
        });
    }

    /**
     * This case exists only to eliminate the change caused by the {@link #reboot()}.
     */
    @Suppress   // After reboot() executed, comment the annotation and execute the case manually.
    @Test
    public void clearDeviceOwner() {
        final DevicePolicyManager dpm = targetContext.getSystemService(DevicePolicyManager.class);
        assertNotNull(dpm);
        final String myPkgName = targetContext.getPackageName();
        if (dpm.isDeviceOwnerApp(myPkgName)) {
            dpm.clearDeviceOwnerApp(myPkgName);
        }
    }

    private void enableAccessibilityService() throws UiObjectNotFoundException {
        final UiSelector myServiceItem = new UiSelector().text(targetContext.getString(R.string.poweract_accessibility_service_label));
        final UiObject myServiceItemUi = mUiDevice.findObject(myServiceItem);
        if (!myServiceItemUi.exists()) {
            Log.d(TAG, "enableAccessibilityService: ?");
            return;
        }
        myServiceItemUi.click();
        mUiDevice.findObject(new UiSelector().className(Switch.class)).click();
        final UiSelector allowButton;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allowButton = new UiSelector().className(Button.class).index(0);
        } else {
            allowButton = new UiSelector().text(targetContext.getString(android.R.string.ok));
        }
        mUiDevice.findObject(allowButton).click();
        mUiDevice.pressBack();
        mUiDevice.pressBack();
    }

    public void grantShizukuPermission() {
        // Unfortunately, Shizuku Manager's string resources are obfuscated.
        mUiDevice.findObject(By.pkg(ShizukuProvider.MANAGER_APPLICATION_ID).clickable(true)).click();
    }

    public static void callHiddenApiBypass() {
        @SuppressWarnings("SpellCheckingInspection")
        Class<?> habClz = ReflectionUtils.findClass("org.lsposed.hiddenapibypass.HiddenApiBypass");
        Log.d(TAG, "call... " + habClz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Objects.nonNull(habClz)) {
            ReflectionUtils.invokeStaticMethod(ReflectionUtils.findMethod(habClz, "addHiddenApiExemptions", String[].class), (Object) new String[]{""});
        }
    }
}
