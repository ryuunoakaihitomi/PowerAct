# PowerAct

## Introduction

An Android library that can manipulate power-related actions with just few lines of code.

(Use `AccessibilityService` and `DevicePolicyManager`)

## Usage

Download

[ ![Download](https://api.bintray.com/packages/ryuunoakaihitomi/maven/poweract/images/download.svg) ](https://bintray.com/ryuunoakaihitomi/maven/poweract/_latestVersion)

```gradle
repositories {
    jcenter()
}

dependencies {
    implementation 'github.ryuunoakaihitomi.poweract:poweract:<latest-version>'
}
    
```

Use

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

Create `res/values/poweract_config.xml`

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

## Compatibility

**In theory, library should not throw an exception at any time.**

API level|Lock Screen|Show System Power Dialog
-|-|-
14 ~ 20 |√|×
21 ~ 27 |√|√
28+|√+|√

> √+ `Unlock by fingerprint`