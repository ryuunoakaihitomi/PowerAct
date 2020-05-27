package github.ryuunoakaihitomi.poweract;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

/**
 * A {@link Button} acts like a real power button.
 * <p>
 * Click to lock screen, and long click to show system power dialog.
 */
public class PowerButton extends Button implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "PowerButton";

    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    @SuppressWarnings("CanBeFinal")
    private boolean mPrepareToUse;

    public PowerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setOnLongClickListener(this);
        mPrepareToUse = true;
    }

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
     * is always <b>false</b>.
     *
     * @param l {@link android.view.View.OnLongClickListener}
     * @see View#setOnClickListener(OnClickListener)
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (mPrepareToUse) mOnLongClickListener = l;
        else super.setOnLongClickListener(l);
    }

    @Override
    public void onClick(View v) {
        PowerAct.lockScreen(contextToActivityNoThrow(getContext()));
        if (mOnClickListener != null) mOnClickListener.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        PowerAct.showPowerDialog(contextToActivityNoThrow(getContext()));
        if (mOnLongClickListener != null) {
            boolean ret = mOnLongClickListener.onLongClick(v);
            Log.d(TAG, "onLongClick: onLongClick(v) -> " + ret);
        }
        return true;
    }
}
