# Modified proguard-android-optimize.txt


-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 99
-dontskipnonpubliclibraryclasses
-verbose

# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-dontnote androidx.**
-dontwarn androidx.**

# @Keep
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# These classes are duplicated between android.jar and core-lambda-stubs.jar.
-dontnote java.lang.invoke.**


# -----------------------------------------------------------------------------
# Remove Debug log
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
# Declare classes for the public API
-keep class github.ryuunoakaihitomi.poweract.Callback {public *;}
-keep class github.ryuunoakaihitomi.poweract.PowerAct {public *;}
-keep class github.ryuunoakaihitomi.poweract.PowerActX {public *;}
-keep class github.ryuunoakaihitomi.poweract.PowerButton {public *;}
-keep class github.ryuunoakaihitomi.poweract.ExternalUtils {public *;}
# -----------------------------------------------------------------------------