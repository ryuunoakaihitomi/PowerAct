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

[Use Java 8 language features and APIs](https://developer.android.google.cn/studio/write/java8-support)

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
lockScreenBtn.setOnClickListener(v -> {
    // Lock screen, without callback.
    PowerAct.lockScreen(activity);
});
powerDialogBtn.setOnClickListener(v -> {
    // Show system power dialog, with callback.
    PowerAct.showPowerDialog(activity, callback);
});
// An additional widget.
PowerButton powerButton = findViewById(R.id.pwrBtn);
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

If you want to know more, click the download badge, download the `*javadoc.jar` and decompress it. ~~(excluding the xml's config)~~

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