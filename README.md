# PowerAct

## Introduction

An Android library that can manipulate power-related actions with just few lines of code.

(Using `AccessibilityService` and `DevicePolicyManager` or root)

## Usage

### Download

[ ![Download](https://api.bintray.com/packages/ryuunoakaihitomi/maven/poweract/images/download.svg) ](https://bintray.com/ryuunoakaihitomi/maven/poweract/_latestVersion)

```groovy
repositories {
    jcenter()
}


dependencies {
    implementation 'github.ryuunoakaihitomi.poweract:poweract:<latest-version>'
}
    
```

### Configure

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
// Guide user to grant the accessibility service permission.
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

If you want to know more...
* Demo app's source code. For example, [MainActivity.java](app/src/main/java/demo/power_act/MainActivity.java).
* Click the download badge, download the `*javadoc.jar` and decompress it. ~~(excluding the xml's config)~~

## Compatibility

**In theory, the library should not throw an exception at any time.**

### PowerAct

API level|Lock screen|Show system power dialog
:-|-|-
14 ~ 20 |√|×
21 ~ 27 |√|√
28+|√+|√

> √+ `Unlock by fingerprint`

### PowerActX

There are few compatibility issues in `PowerActX`. 

> Safe mode cannot work before 16,
> and shutdown before 17 can only be forced.

## License

```text
   Copyright 2020 ZQY

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