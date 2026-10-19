[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$Adb = "adb",
    [switch]$HideOptionalStatusIcons
)

$ErrorActionPreference = "Stop"

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    $result = & $Adb @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $result" }
    return ($result | Out-String).Trim()
}

if ((Invoke-Adb get-state) -ne "device") { throw "No authorized Android device found." }

$model = Invoke-Adb shell getprop ro.product.model
$oneUi = Invoke-Adb shell getprop ro.build.version.oneui
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupDir = Join-Path $PSScriptRoot "..\backups"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
$backupPath = Join-Path $backupDir "ui-$stamp.json"

$backup = [ordered]@{
    model = $model
    oneUi = $oneUi
    createdAt = (Get-Date).ToString("o")
    qsTiles = Invoke-Adb shell settings get secure sysui_qs_tiles
    iconBlacklist = Invoke-Adb shell settings get secure icon_blacklist
}
$backup | ConvertTo-Json | Set-Content -Encoding UTF8 -Path $backupPath

$tiles = "internet,bt,flashlight,dnd,rotation,battery,airplane,screenrecord,dark"
if ($PSCmdlet.ShouldProcess("$model (One UI $oneUi)", "Apply rootless AOSP-style SystemUI preset")) {
    Invoke-Adb shell settings put secure sysui_qs_tiles $tiles | Out-Null
    if ($HideOptionalStatusIcons) {
        Invoke-Adb shell settings put secure icon_blacklist "alarm_clock,nfc,volume" | Out-Null
    }
    Invoke-Adb shell am force-stop com.android.systemui | Out-Null
}

Write-Host "Applied reversible UI preset to $model."
Write-Host "Backup: $backupPath"
