# PowerAct

[![Library Release CI](https://github.com/ryuunoakaihitomi/PowerAct/actions/workflows/release.yml/badge.svg)](https://github.com/ryuunoakaihitomi/PowerAct/actions/workflows/release.yml)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Fryuunoakaihitomi%2Fmaven-repository%2Fmain%2Fgithub%2Fryuunoakaihitomi%2Fpoweract%2Fpoweract%2Fmaven-metadata.xml)](https://github.com/ryuunoakaihitomi/maven-repository)

## Introduction

*Press power button, not by hand, but by code.*

An Android library that can manipulate power-related actions with just few lines of code.

## Usage

### Import

Check [this](https://github.com/ryuunoakaihitomi/maven-repository#usage).

### Depend

```groovy
dependencies {
    // (Required import statement)
    implementation 'github.ryuunoakaihitomi.poweract:poweract:latest.release'

    // (Optional but recommended) Bring better performance to use root shell.
    // -> https://github.com/topjohnwu/libsu
    implementation "com.github.topjohnwu.libsu:core:3.1.2"
    // (Optional but recommended) A more elegant and direct way of using privileged system API.
    // NOTE: Need more steps to integrate it.
    // -> https://github.com/RikkaApps/Shizuku
    def shizuku_version = '12.1.0'
    implementation "dev.rikka.shizuku:api:$shizuku_version"
    implementation "dev.rikka.shizuku:provider:$shizuku_version"
    // (Optional but recommended) Make Shizuku available in more situations.
    // NOTE: Enable it on your own.
    // -> https://github.com/LSPosed/AndroidHiddenApiBypass
    implementation "org.lsposed.hiddenapibypass:hiddenapibypass:2.0"
}
```

### Invoke

* Package

```java
import github.ryuunoakaihitomi.poweract.*;
```

* API Endpoints

| Class Name      | Brief                         |
|-----------------|-------------------------------|
| `PowerAct`      | For general environments.     |
| `PowerActX`     | For rooted environments.      |
| `Callback`      | Notify execution result.      |
| `ExternalUtils` | External utility tools.       |
| `PowerButton`   | Widget for quick integration. |

* Examples

Turn off the screen.
```java
PowerAct.lockScreen(activity);
```

Open system power menu, with callback,
```java
Callback callback = new Callback() {
    @Override
    public void done() {}
    @Override
    public void failed() {}
};
PowerAct.showPowerDialog(activity, callback);
```

...and guide user to enable the accessibility service.
```java
//... Continue the above section code.
ExternalUtils.setUserGuideRunnable(() -> Toast.makeText(this, "Please enable the accessibility service.", Toast.LENGTH_LONG).show());
```

Reboot to recovery.
```java
PowerActX.recovery();
```

Add a power button in layout.xml
```xml
<github.ryuunoakaihitomi.poweract.PowerButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" 
/>
```

* Behavior

**In theory, the library should not throw an exception at any time.**

* Additional setup for some functions that depend on Accessibility Service.

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

## License

```text
   Copyright 2020-2022 ZQY

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