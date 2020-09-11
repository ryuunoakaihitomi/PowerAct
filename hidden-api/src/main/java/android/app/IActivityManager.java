package android.app;

import java.util.List;

/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 */
public interface IActivityManager {

    /**
     * Retrieve running application processes in the system
     */
    //@UnsupportedAppUsage
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
}
