# Keep the whole library
# In most cases, we should not need it.
#-keep class github.ryuunoakaihitomi.poweract.*{*;}
# Keep main entry for PaxExecutor in PowerAct.
-keepclassmembers class * {
    public static void main(java.lang.String[]);
}
# Probe library: libsu
-keepnames class com.topjohnwu.superuser.Shell
# Probe library: Shizuku
-keepnames class rikka.shizuku.ShizukuProvider
-keepnames class rikka.shizuku.ShizukuBinderWrapper
-keepnames class rikka.shizuku.SystemServiceHelper