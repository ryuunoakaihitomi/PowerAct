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
# This so-called Initializer has never had any practical effect.
# But for the convenience of debuging, we don't delete it from the source code.
-assumenosideeffects class github.ryuunoakaihitomi.poweract.internal.Initializer