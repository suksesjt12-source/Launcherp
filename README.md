# Storm Revolution Native Prototype (Android ARM64)

This repository is a **clean native Android prototype**, not a port or reimplementation of the commercial game. It currently proves only that an Android ARM64 app can render a placeholder arena through OpenGL ES 3 and receive touch-control input.

## What is included
- Kotlin Android activity, landscape orientation, immersive mode.
- C++ NDK shared library (`arm64-v8a`).
- OpenGL ES 3 placeholder arena and abstract fighter silhouettes.
- On-screen joystick and attack/guard/jump/chakra touch regions.
- GitHub Actions workflow to build a debug APK.

## What is not included
- No original game code, character models, textures, animation, audio, stages, story, combat logic, or proprietary assets.
- No Direct3D 9 compatibility layer and no Windows executable loader.
- No Vulkan renderer yet. OpenGL ES 3 is used for the first device bring-up prototype; a Vulkan backend would be a later engineering milestone.
- This project does not load `NSUNSR.exe` and does not turn it into an APK.

## Build on GitHub (phone-friendly)
1. Create a new empty GitHub repository.
2. Upload the contents of this folder to the repository root (not the ZIP file itself).
3. Open **Actions** and run **Build Android APK**.
4. When the workflow finishes, open its run and download the `StormNative-debug-apk` artifact.

## Local build
Requires JDK 17, Android SDK, NDK 27.2.12479018, CMake 3.22.1, and Gradle 8.9.

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Next engineering milestones
1. Verify the APK on the target Mali-G57 device and capture GPU/driver behavior.
2. Replace placeholder geometry with independently created/licensed rigged character and arena assets.
3. Implement deterministic fighter state, movement, hit detection, guard, chakra, camera, and animation state machines.
4. Inspect legally accessible game data formats and document them before writing any importer. Do not distribute copyrighted game assets.
5. Profile CPU/GPU/memory and decide whether a Vulkan backend is justified.

## Rights and provenance
Only use assets and code that you created or are licensed to use. Keep original commercial game files private; this prototype does not require them.
