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
-keepnames class moe.shizuku.api.ShizukuProvider
-keepnames class moe.shizuku.api.ShizukuBinderWrapper
-keepnames class moe.shizuku.api.SystemServiceHelper