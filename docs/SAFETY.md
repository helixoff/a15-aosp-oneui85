# Safety and rollback

## Before changing the phone

1. Back up photos, messages, authenticator recovery codes, and any Samsung Secure Folder content.
2. Record the exact model under **Settings → About phone** and the build number under **Software information**.
3. Test the launcher and ADB preset before considering root.
4. Keep the stock firmware package for the exact model, region, and bootloader revision.

## Knox and bootloader warning

Unlocking a Samsung bootloader normally factory-resets the phone. Rooting can permanently trip the Knox warranty bit. Samsung Wallet, Secure Folder, protected corporate apps, and some banking apps can stop working. Relocking a modified phone can make it unbootable.

This project does not unlock, flash, patch, or relock the bootloader.

## Rollback

- **Launcher:** Settings → Apps → Choose default apps → Home app → One UI Home. Then uninstall A15 Launcher.
- **UI preset:** run `tools/restore-ui.ps1` with the backup created during apply.
- **Font module:** disable or uninstall it in Magisk/KernelSU, then reboot.

If SystemUI repeatedly crashes, boot with modules disabled and remove the font module. The module never writes directly to the physical system partition.
