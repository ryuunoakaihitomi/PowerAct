# Build Note

Write some ~~weird~~special steps for rebuilding & releasing the lib next time.

## Build
* Modify **gradle.properties**, to change the version code ~~and developer id~~.
* Build & release, go to see `maven-release.gradle(:library)` and `build.gradle(:library)`.

## Debug
* Import **PowerActLog.xml** to simplify debug log statement in Android Studio.
* Modify **build.gradle(:app)** to test the release aar library file. **Must do it before uploading.**