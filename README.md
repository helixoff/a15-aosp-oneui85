# A15 AOSP Surface for One UI 8.5

An experimental, reversible AOSP-style customization kit for Samsung Galaxy A15 devices running One UI 8.5.

## What works

1. **A15 Launcher** — a rootless, open-source HOME app with a Google-style search field, live clock/date, dynamic wallpaper colors, app filtering, and a launcher grid.
2. **AOSP UI preset** — rootless ADB tooling that backs up and applies a compact Quick Settings tile order and optional status-bar icon cleanup.
3. **Google Sans import** — tap **Aa Import font** in the launcher and select a font file you legally own. It is copied into the app's private storage and applied without root.
4. **Optional system font bridge** — an isolated Magisk/KernelSU module for users who explicitly choose system-wide typography.

## Important limits

- This is not a ROM and does not replace Samsung's framework or SystemUI.
- Samsung does not expose every status-bar or Quick Settings resource as an overlay. The scripts change supported settings and skip unsupported keys.
- Pixel Launcher and Google Sans are proprietary and are not included. The launcher is an original implementation; you must supply font files yourself.
- The rootless path does not unlock the bootloader, write to system partitions, or trip Knox. Read [docs/SAFETY.md](docs/SAFETY.md) before using the optional root module.

## Quick start

### 1. Build and install the launcher

Open `launcher/` in Android Studio Quail 4 or newer, then run the `app` configuration on the phone. When Android asks for the Home app, select **A15 Launcher**.

Command-line build with JDK 17, Android SDK 36, Gradle 9.6, and AGP 9.4:

```bash
gradle -p launcher :app:assembleDebug
adb install -r launcher/app/build/outputs/apk/debug/app-debug.apk
```

### 2. Apply the reversible UI preset

Enable Developer options and USB debugging, connect the phone, then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\apply-ui.ps1
```

The script writes a timestamped backup under `backups/`. Restore it with:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\restore-ui.ps1 -BackupFile .\backups\<backup>.json
```

### 3. Import Google Sans without root

Open **A15 Launcher**, tap **Aa Import font**, and select a `.ttf` or `.otf` file you legally own. The font applies only inside A15 Launcher; stock OneUI does not provide a reliable unsigned API for replacing the system font.

Russian step-by-step guide: [Google Sans on OneUI 8.5 without zFont](docs/GOOGLE_SANS_GUIDE_RU.md).

### 4. Optional: package the root-only system font module

Copy your legally obtained font files into `module/payload/fonts/` using these names:

- `GoogleSans-Regular.ttf`
- `GoogleSans-Medium.ttf`
- `GoogleSans-Bold.ttf`
- `GoogleSans-Italic.ttf` (optional)

Then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\package-module.ps1
```

Install the generated ZIP from `dist/` in Magisk or KernelSU and reboot. Uninstalling the module restores the stock fonts.

## Device scope

The launcher targets Android 12+ and is designed for One UI 8.5 on both Galaxy A15 families:

- SM-A155 variants (Galaxy A15 4G)
- SM-A156 variants (Galaxy A15 5G)

One UI builds differ by model and region. See [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) before enabling the SystemUI preset.

## Project map

```text
launcher/       Original HOME app
module/         Magisk/KernelSU systemless font module
tools/          Build, validation, ADB apply, and rollback scripts
docs/           Compatibility and safety notes
```

## License

Project source is Apache-2.0. Google Sans, Pixel Launcher, Samsung firmware, and other third-party assets are not licensed or distributed by this repository.
