[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$required = @(
    "README.md",
    "LICENSE",
    "launcher\settings.gradle.kts",
    "launcher\app\build.gradle.kts",
    "launcher\app\src\main\AndroidManifest.xml",
    "launcher\app\src\main\java\dev\helixoff\a15launcher\MainActivity.java",
    "module\module.prop",
    "module\customize.sh",
    "tools\apply-ui.ps1",
    "tools\restore-ui.ps1"
)

foreach ($relative in $required) {
    if (-not (Test-Path (Join-Path $root $relative))) { throw "Missing required file: $relative" }
}

Get-ChildItem -Path (Join-Path $root "launcher\app\src\main") -Filter *.xml -Recurse |
    ForEach-Object {
        try { [xml](Get-Content -Raw -Path $_.FullName) | Out-Null }
        catch { throw "Invalid XML: $($_.FullName): $($_.Exception.Message)" }
    }

$fontBinaries = Get-ChildItem -Path (Join-Path $root "module\payload\fonts") -File |
    Where-Object { $_.Extension -in ".ttf", ".otf" }
if ($fontBinaries) { throw "Font binaries are present in the source tree; do not commit them." }

Write-Host "Validation passed: required files, XML, and font-license guard."
