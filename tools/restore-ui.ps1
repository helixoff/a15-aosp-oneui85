[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory = $true)][string]$BackupFile,
    [string]$Adb = "adb"
)

$ErrorActionPreference = "Stop"
$backup = Get-Content -Raw -Path $BackupFile | ConvertFrom-Json

function Set-SecureSetting {
    param([string]$Key, [AllowNull()][string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -eq "null") {
        & $Adb shell settings delete secure $Key | Out-Null
    } else {
        & $Adb shell settings put secure $Key $Value | Out-Null
    }
    if ($LASTEXITCODE -ne 0) { throw "Failed to restore $Key" }
}

if ($PSCmdlet.ShouldProcess($backup.model, "Restore saved SystemUI settings")) {
    Set-SecureSetting -Key "sysui_qs_tiles" -Value $backup.qsTiles
    Set-SecureSetting -Key "icon_blacklist" -Value $backup.iconBlacklist
    & $Adb shell am force-stop com.android.systemui | Out-Null
}

Write-Host "Restored SystemUI settings from $BackupFile"
