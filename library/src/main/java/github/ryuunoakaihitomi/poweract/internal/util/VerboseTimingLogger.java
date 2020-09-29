package github.ryuunoakaihitomi.poweract.internal.util;

import android.util.TimingLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class VerboseTimingLogger {

    private Field mDisabledField, mSplitsField, mSplitLabelsField, mTagField, mLabelField;

    /**
     * Deprecated since 30.
     */
    private TimingLogger mLogger;

    private boolean mDisabled;

    public VerboseTimingLogger(String tag, String label) {
        mLogger = new TimingLogger(tag, label);
        initFields();
        reset(tag, label);
    }

    // reimplement
    public void reset(String tag, String label) {
        set(mTagField, tag);
        set(mLabelField, label);
        reset();
    }

    // reimplement
    @SuppressWarnings("rawtypes")
    public void reset() {
        if (get(mSplitsField) == null) {
            set(mSplitsField, new ArrayList<Long>());
            set(mSplitLabelsField, new ArrayList<String>());
        } else {
            ((ArrayList) get(mSplitsField)).clear();
            ((ArrayList) get(mSplitLabelsField)).clear();
        }
        addSplit(null);
    }

    public void addSplit(String splitLabel) {
        set(mDisabledField, false);
        mLogger.addSplit(splitLabel);
    }

    public void dumpToLog() {
        if (mDisabled) return;
        set(mDisabledField, false);
        mLogger.dumpToLog();
    }

    // new
    public void setDisabled(boolean disabled) {
        mDisabled = disabled;
    }

    private void initFields() {
        Class<?> c = TimingLogger.class;
        mDisabledField = ReflectionUtils.findField(c, "mDisabled");
        mSplitsField = ReflectionUtils.findField(c, "mSplits");
        mSplitLabelsField = ReflectionUtils.findField(c, "mSplitLabels");
        mTagField = ReflectionUtils.findField(c, "mTag");
        mLabelField = ReflectionUtils.findField(c, "mLabel");
    }

    private void set(Field field, Object value) {
        ReflectionUtils.setField(field, mLogger, value);
    }

    private Object get(Field field) {
        return ReflectionUtils.fetchField(field, mLogger);
    }
}
