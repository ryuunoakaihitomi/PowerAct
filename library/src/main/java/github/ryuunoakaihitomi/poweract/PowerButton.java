package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import github.ryuunoakaihitomi.poweract.internal.Initializer;
import github.ryuunoakaihitomi.poweract.internal.util.DebugLog;
import github.ryuunoakaihitomi.poweract.internal.util.Utils;

/**
 * A {@link Button} acts like a real power button.
 * <p>
 * Click to lock screen, and long click to show system power dialog if available.
 * In order to prevent the behavior from being destroyed, the class cannot be inherited.
 *
 * @see PowerAct#lockScreen(Activity)
 * @see PowerAct#showPowerDialog(Activity)
 * @since 1.0.0
 */
public final class PowerButton extends Button {

    private static final String TAG = "PowerButton";

    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    private boolean mPrepareToUse;
    private boolean mHasCustomMeasure;
    private int mSize;

    static {
        Initializer.notify(TAG);
    }

    public PowerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initNormalStyles();

        /* Lock screen by click. */
        final OnClickListener lockScreenListener = v -> {
            lockScreen();
            if (mOnClickListener != null) mOnClickListener.onClick(v);
        };
        setOnClickListener(lockScreenListener);

        /* Show power dialog by long click. */
        final OnLongClickListener showPowerDialogListener = v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PowerAct.showPowerDialog(contextToActivityNoThrow(getContext()));
            } else {
                DebugLog.e(TAG, "PowerButton: Showing power dialog is not supported before 21!" +
                        " downgrade to lockScreen()...");
                lockScreen();
            }
            if (mOnLongClickListener != null) {
                boolean ret = mOnLongClickListener.onLongClick(v);
                DebugLog.d(TAG, "onLongClick: onLongClick(v) -> " + ret);
            }
            return true;
        };
        setOnLongClickListener(showPowerDialogListener);

        mPrepareToUse = true;
    }

    private void lockScreen() {
        Activity activity = contextToActivityNoThrow(getContext());
        if (ExternalUtils.isExposedComponentAvailable(activity)) {
            PowerAct.lockScreen(activity);
        } else {
            PowerActX.lockScreen();
        }
    }

    private void initNormalStyles() {
        /* Normal Style: ic_lock_power_off, red */
        final @DrawableRes int powerIconResId = android.R.drawable.ic_lock_power_off;
        final @ColorInt int tintColor = Color.RED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Drawable powerIcon = getContext().getDrawable(powerIconResId);
            if (powerIcon != null) {
                powerIcon.setTint(tintColor);
                setBackground(powerIcon);
            }
        } else {
            final Bitmap powerIcon = BitmapFactory.decodeResource(getResources(), powerIconResId);
            setBackgroundDrawable(new BitmapDrawable(getResources(), Utils.makeTintBitmap(powerIcon, tintColor)));
        }
        // Normal Style: 48dp
        mSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
        mHasCustomMeasure = true;
    }

    /**
     * Make the size mutable.
     * <p>
     * The size of {@link PowerButton} is immutable in default.
     *
     * @since 1.1.2
     */
    public void unlockSize() {
        mHasCustomMeasure = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHasCustomMeasure) setMeasuredDimension(mSize, mSize);
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressWarnings("ConstantConditions")
    private static @NonNull
    Activity contextToActivityNoThrow(Context context) {
        try {
            return (Activity) context;
        } catch (ClassCastException ignored) {
        }
        return null;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (mPrepareToUse) mOnClickListener = l;
        else super.setOnClickListener(l);
    }

    /**
     * <b>Note:</b> To prevent {@link PowerAct#lockScreen(Activity)} from calling.
     * The return value of the {@link android.view.View.OnLongClickListener}
     * is always <b>true</b> for consuming the long click.
     *
     * @param l {@link android.view.View.OnLongClickListener}
     * @see View#setOnClickListener(OnClickListener)
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (mPrepareToUse) mOnLongClickListener = l;
        else super.setOnLongClickListener(l);
    }
}
