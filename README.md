# trmnl-android-legacy-lite

Android 5.1.1 (API 22) compatible lite TRMNL client.

## Modes
- BYOD: uses `https://trmnl.com` + token
- BYOS: uses custom base URL + token

## Flow
- First launch opens configuration
- Validate token loads `/api/display/current` preview
- Save enabled only when preview image loads
- Main screen is fullscreen image using `/api/display` with refresh timer from `refresh_rate`
- Tap image for menu: Configure Device, Refresh Current Image, Load Next Playlist Image

## Build
```bash
./gradlew assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`
