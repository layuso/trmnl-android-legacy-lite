# EPDC Stage 2 Investigation Notes (mxc_epdc_fb / Android 5.1.1)

## Goal
Investigate native force-full-refresh support for i.MX EPDC (`/dev/graphics/fb0`) and feasibility of JNI helper.

## Device context observed
- Framebuffer device: `/dev/graphics/fb0`
- Sysfs path available: `/sys/class/graphics/fb0`
- No obvious exposed EPDC refresh sysfs trigger in current environment.

## Relevant Linux/NXP i.MX EPDC references
Typical i.MX framebuffer EPDC integrations expose ioctls via `mxcfb.h`, often including:
- `MXCFB_SEND_UPDATE`
- `MXCFB_WAIT_FOR_UPDATE_COMPLETE`
- update region / waveform / update mode structs (`mxcfb_update_data` family)

These are platform-kernel dependent and require exact kernel header/driver support.

## Why Stage 2 is not fully enabled yet
1. Need confirmed kernel headers and ioctl constants matching this panel kernel build.
2. Need verification that app process has permission to issue framebuffer ioctls on `/dev/graphics/fb0`.
3. Need on-device native testing with exact EPDC waveform/update mode parameters.

## Current status
- Stage 1 fallback full refresh implemented in app (black -> white -> target image) and working path available.
- Stage 2 native JNI EPDC ioctl path: **not enabled yet** pending on-device ioctl capability and kernel-constant validation.

## Proposed next Stage 2 execution plan
1. Capture kernel/driver signatures from device (`uname -a`, framebuffer driver info, available headers if any).
2. Build tiny NDK test binary to open `/dev/graphics/fb0` and probe known ioctl IDs.
3. Validate a full-screen update call sequence safely with no crash.
4. Wrap in JNI `forceFullRefresh()` and gate behind runtime capability check.
5. Fall back to Stage 1 cycle if native path unavailable.
