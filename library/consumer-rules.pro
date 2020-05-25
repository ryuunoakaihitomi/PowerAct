# Remove Log
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
# Declare classes for the public API
-keep class github.ryuunoakaihitomi.poweract.Callback {public *;}
-keep class github.ryuunoakaihitomi.poweract.PowerAct {public *;}
-keep class github.ryuunoakaihitomi.poweract.PowerButton {public *;}