# trmnl-android-legacy-lite

A legacy-focused remake of the TRMNL Android app for older Android hardware.

This project is a **separate implementation** inspired by the original TRMNL app experience and common user flows, but rebuilt for compatibility with older Android devices — especially **Android 5.1.1 (API 22)** panels like the **Avalue E-Ink display hardware**.

- Original reference app: `usetrmnl/trmnl-android`
- This repo target: Android 5.1.1/API 22 compatibility
- UI stack: classic Android Views/XML (no Compose)

---

## Purpose

`trmnl-android-legacy-lite` aims to preserve the practical day-to-day TRMNL usage patterns while avoiding modern Android dependencies that are difficult to run on legacy E-Ink devices.

It is designed to be:
- lightweight
- installable on older Android panels
- simple to configure
- focused on reliable display refresh behavior

---

## Supported Modes

- **BYOD**
  - Uses `https://trmnl.com`
  - Requires device token

- **BYOS**
  - Uses custom server base URL
  - Requires base URL + device token

---

## Core User Flow

1. On first launch (or if not configured), open **Configure Device**.
2. Select mode: BYOD or BYOS.
3. Validate token and load preview.
4. Save only after a valid preview image is returned.
5. After save, app launches straight into fullscreen display mode.
6. Refresh scheduling follows `refresh_rate` from API response.
7. Tapping image opens actions:
   - Configure Device
   - Refresh Current Image
   - Load Next Playlist Image

---

## E-Ink / Legacy Behavior

- Fullscreen immersive display (no persistent top/bottom system bars)
- Stage-1 full refresh fallback for E-Ink (black → white → target image)
- Troubleshooting output in config flow with formatted JSON when image is unavailable

---

## Build

```bash
./gradlew assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`
