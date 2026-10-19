[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$module = Join-Path $root "module"
$fontDir = Join-Path $module "payload\fonts"
$required = @("GoogleSans-Regular.ttf", "GoogleSans-Medium.ttf", "GoogleSans-Bold.ttf")

foreach ($name in $required) {
    $path = Join-Path $fontDir $name
    if (-not (Test-Path $path) -or (Get-Item $path).Length -eq 0) {
        throw "Missing $path. Supply the licensed font file first."
    }
}

$dist = Join-Path $root "dist"
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$destination = Join-Path $dist "a15-aosp-surface-fonts-0.1.0.zip"
if (Test-Path $destination) { Remove-Item -LiteralPath $destination }
Compress-Archive -Path (Join-Path $module "*") -DestinationPath $destination
Write-Host "Created $destination"
