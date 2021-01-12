# PowerAct

## Introduction

*Press power button, not by hand, but by code.*

An Android library that can manipulate power-related actions with just few lines of code.

(Using [`Shizuku`](https://shizuku.rikka.app/), `AccessibilityService`, `DevicePolicyManager`, and root)

## Usage

### Import

[ ![If the badge is not shown, click here to check the latest version.](https://api.bintray.com/packages/ryuunoakaihitomi/maven/poweract/images/download.svg) ](https://bintray.com/ryuunoakaihitomi/maven/poweract/_latestVersion)

```groovy
repositories {
    jcenter()
}


dependencies {
    // (Required)
    implementation 'github.ryuunoakaihitomi.poweract:poweract:<latest-version>'

    // (Optional) Bring better performance to use root shell.
    // https://github.com/topjohnwu/libsu
    implementation "com.github.topjohnwu.libsu:core:3.0.2"
    // (Optional) A more elegant and direct way of using privileged system API.
    // NOTE: Need more steps to integrate it.
    // https://github.com/RikkaApps/Shizuku/blob/master/README.md
    implementation 'moe.shizuku.privilege:api:4.2.1'
}
    
```

[Use Java 8 language features and APIs](https://developer.android.com/studio/write/java8-support)

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

### Use

```java
import github.ryuunoakaihitomi.poweract.*;

...

// Lock screen.
PowerAct.lockScreen(activity);
// Show system power dialog.
PowerAct.showPowerDialog(activity, callback);
// An additional widget for Quick Integration.
/*
        <github.ryuunoakaihitomi.poweract.PowerButton
            android:id="@+id/pwrBtn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
*/
PowerButton powerButton = findViewById(R.id.pwrBtn);
// Guide user to grant the accessibility service permission if necessary.
ExternalUtils.setUserGuideRunnable(runnable);
```

Create [`res/values/poweract_config.xml`](library/src/main/res/values/public.xml)

```xml
<resources xmlns:tools="http://schemas.android.com/tools" tools:ignore="UnusedResources">
    <!--  In order to configure some UI properties you must rewrite the res of the library.  -->
    <string name="poweract_accessibility_service_label">Power Action Service</string>
    <string name="poweract_accessibility_service_description">The service is used to perform some power action without reaching the actual power button on the side of the phone. It will never collect any user data.</string>
    <string name="poweract_accessibility_service_summary">Virtual power key accessibility service.</string>
    <!--  Optional.  -->
    <bool name="poweract_accessibility_service_show_foreground_notification">true</bool>
</resources>
```

Class `PowerActX` provides advanced power actions, but it only for **rooted** environment.
Nevertheless, if you only use the library without `PowerAct` and `PowerButton` class, you don't need to create `poweract_config.xml`.
Use `ExternalUtils.disableExposedComponents()` to make the exposed components for `PowerAct` invisible.

## Compatibility

**In theory, the library should not throw an exception at any time.**

### PowerActX

> PowerActX class must be called in main thread before 18.

## License

```text
   Copyright 2020-2021 ZQY

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```