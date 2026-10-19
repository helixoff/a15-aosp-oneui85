# Compatibility matrix

| Component | Root | Expected on One UI 8.5 | Notes |
|---|---:|---|---|
| A15 Launcher | No | Yes | Uses public Android launcher APIs. |
| Dynamic wallpaper color | No | Yes | Uses Android 12+ system accent resources. |
| Google Sans in A15 Launcher | No | Yes | Import a user-owned TTF/OTF through the launcher. |
| Quick Settings tile order | No | Usually | Samsung may rename or ignore individual tile specs. |
| Status-bar icon cleanup | No | Firmware-dependent | The AOSP tuner key may be ignored by Samsung SystemUI. |
| Google Sans system-wide | Yes | Experimental | Optional systemless replacement; exact font lookup can vary by firmware. |
| Full AOSP Quick Settings layout | Yes + per-build overlay | Not included | Requires Samsung framework resources from the exact installed build. |

## Why there is no universal SystemUI overlay

Runtime resource overlays can only replace resources exposed by the target package's overlay policy. Samsung changes SystemUI resources between device and firmware builds, and those resources are signed/protected. Shipping one prebuilt overlay would be brittle and could cause a boot loop.

The safe baseline in this repository uses public launcher APIs and reversible settings. Per-build RRO work should start only after pulling `framework-res.apk` and `SystemUI.apk` from the exact phone build and inspecting its `overlayable` declarations.
