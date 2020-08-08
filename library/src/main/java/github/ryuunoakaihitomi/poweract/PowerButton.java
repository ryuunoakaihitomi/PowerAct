package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

/**
 * A {@link Button} acts like a real power button.
 * <p>
 * Click to lock screen, and long click to show system power dialog.
 * In order to prevent the behavior from being destroyed, the class cannot be inherited.
 *
 * @see PowerAct#lockScreen(Activity)
 * @see PowerAct#showPowerDialog(Activity)
 */
public final class PowerButton extends Button {

    private static final String TAG = "PowerButton";

    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    @SuppressWarnings("CanBeFinal")
    private boolean mPrepareToUse;

    public PowerButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        /* Lock screen by click. */
        final OnClickListener lockScreenListener = v -> {
            PowerAct.lockScreen(contextToActivityNoThrow(getContext()));
            if (mOnClickListener != null) mOnClickListener.onClick(v);
        };
        setOnClickListener(lockScreenListener);

        /* Show power dialog by long click. */
        final OnLongClickListener showPowerDialogListener = v -> {
            PowerAct.showPowerDialog(contextToActivityNoThrow(getContext()));
            if (mOnLongClickListener != null) {
                boolean ret = mOnLongClickListener.onLongClick(v);
                DebugLog.d(TAG, "onLongClick: onLongClick(v) -> " + ret);
            }
            return true;
        };
        setOnLongClickListener(showPowerDialogListener);

        mPrepareToUse = true;
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
