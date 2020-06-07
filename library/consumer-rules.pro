# Keep the whole library
# In most cases, we should not need it.
#-keep class github.ryuunoakaihitomi.poweract.*{*;}
# Keep main entry for PaxExecutor in PowerAct.
-keepclassmembers class * {
    public static void main(java.lang.String[]);
}