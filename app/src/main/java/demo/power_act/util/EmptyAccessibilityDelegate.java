package demo.power_act.util;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Usage: {@link View#setAccessibilityDelegate(View.AccessibilityDelegate)}
 * <p>
 * Test: <pre>adb shell uiautomator dump</pre>
 */
public class EmptyAccessibilityDelegate extends View.AccessibilityDelegate {

    @Override
    public void sendAccessibilityEvent(View host, int eventType) {
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        return true;
    }

    @Override
    public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
    }

    @Override
    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
    }

    @Override
    public void addExtraDataToAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfo info, @NonNull String extraDataKey, @Nullable Bundle arguments) {
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
        return false;
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
        return null;
    }
}
