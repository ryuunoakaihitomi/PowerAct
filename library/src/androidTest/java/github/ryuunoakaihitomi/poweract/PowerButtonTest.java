package github.ryuunoakaihitomi.poweract;

import android.os.Build;
import android.util.Log;
import android.util.TypedValue;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import github.ryuunoakaihitomi.poweract.test.BaseTest;
import github.ryuunoakaihitomi.poweract.test.R;
import moe.shizuku.api.ShizukuService;
import poweract.test.res.PlaygroundActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PowerButtonTest extends BaseTest {

    private static final String TAG = "PowerButtonTest";

    private static final UiDevice sUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    @Rule
    public final ActivityScenarioRule<PlaygroundActivity> rule = new ActivityScenarioRule<>(PlaygroundActivity.class);

    @Test
    public void checkPowerAction() {
        // Initialize button normal action listeners.
        final int onClick = 1, onLongClick = 2;
        AtomicInteger buttonAction = new AtomicInteger();
        rule.getScenario().onActivity(activity -> {
            PowerButton powerButton = activity.findViewById(R.id.btn_power);
            powerButton.setOnClickListener(v -> buttonAction.set(onClick));
            powerButton.setOnLongClickListener(v -> {
                buttonAction.set(onLongClick);
                return false;
            });
        });
        // Initialize button interaction.
        ViewInteraction powerButton = onView(withId(R.id.btn_power));
        powerButton.check(matches(isDisplayed()));
        // Test: Click
        powerButton.perform(click());
        await().atMost(ONE_SECOND).until(() -> buttonAction.getAndSet(0) == onClick);
        attemptLockScreenRequestUi();
        // Test: Long click
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerButton.perform(longClick());
            await().atMost(ONE_SECOND).until(() -> buttonAction.get() == onLongClick);
            attemptPowerDialogRequestUi();
        }
    }

    @Test
    public void unlockSize() {
        rule.getScenario().onActivity(activity -> {
            PowerButton powerButton = activity.findViewById(R.id.btn_power);
            final int customSizeDp = 48 << 1;   // normal size * 2
            final int customSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    customSizeDp,
                    targetContext.getResources().getDisplayMetrics()));
            powerButton.unlockSize();
            powerButton.setHeight(customSize);
            powerButton.setWidth(customSize);
            powerButton.post(() -> {
                assertThat(powerButton.getHeight(), is(customSize));
                assertThat(powerButton.getWidth(), is(customSize));
            });
        });
    }

    private void attemptLockScreenRequestUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // AccessibilityService
            attemptPowerDialogRequestUi();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ShizukuService.pingBinder()) {
                // Switched to com.android.permissioncontroller after 29.
                assertEquals(PKG_NAME_PACKAGE_INSTALLER, sUiDevice.getCurrentPackageName());
                final String deny = getStringResource(PKG_NAME_PACKAGE_INSTALLER, "grant_dialog_button_deny");
                sUiDevice.findObject(By.text(deny)).click();
            } else {
                verifySettingsUiTitle("add_device_admin_msg");
                sUiDevice.pressBack();
            }
        }
    }

    private void attemptPowerDialogRequestUi() {
        verifySettingsUiTitle("accessibility_settings");
        sUiDevice.pressBack();
    }

    private void verifySettingsUiTitle(String resName) {
        final String title = getStringResource(PKG_NAME_SETTINGS, resName);
        Log.i(TAG, "verifySettingsUi: title = " + title);
        // Out of the app, doesn't work.
        //onView(withText(title)).check(matches(isDisplayed()));
        assertTrue(sUiDevice.hasObject(By.text(title)));
    }
}
