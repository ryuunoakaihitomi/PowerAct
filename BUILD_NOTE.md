# Build Note

Write some ~~weird~~special steps for rebuilding & releasing the lib next time.

## After `Clean Project`
* Create **hidden-api.jar**, go to see `build.gradle(:hidden-api)`.

## Build
* Modify **local.properties**, go to see `local.properties.sample.properties`.
* Modify **gradle.properties**, to change the version code ~~and developer id~~.
* Build & release, go to see `bintray-release.gradle(:library)` and `build.gradle(:library)`.

## Debug
* Import **PowerActLog.xml** to simplify debug log statement in Android Studio.
* Modify **build.gradle(:app)** to test the release aar library file. **Must do it before uploading.**