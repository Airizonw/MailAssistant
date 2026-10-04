$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$target = Join-Path $project 'target'
$staged = Join-Path $target 'release-latest/MailAssistant'
$current = Join-Path $target 'release/MailAssistant'
$backupRoot = Join-Path $PSScriptRoot 'runtime-data-backups/20261003-update'
if (-not (Test-Path -LiteralPath (Join-Path $staged 'MailAssistant.exe'))) { throw 'Latest build is missing' }
if (Get-Process MailAssistant -ErrorAction SilentlyContinue) { throw '请先退出 MailAssistant，避免丢失未保存内容。' }
if (Test-Path -LiteralPath $backupRoot) { throw 'Data backup directory already exists; inspect before repeating.' }

function Copy-VerifiedData([string]$source, [string]$destination) {
    if (-not (Test-Path -LiteralPath $source)) { return }
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    foreach ($file in Get-ChildItem -LiteralPath $source -File -Recurse) {
        $relative = [IO.Path]::GetRelativePath($source, $file.FullName)
        $copy = Join-Path $destination $relative
        New-Item -ItemType Directory -Path (Split-Path -Parent $copy) -Force | Out-Null
        Copy-Item -LiteralPath $file.FullName -Destination $copy
        if ((Get-FileHash -LiteralPath $file.FullName).Hash -ne (Get-FileHash -LiteralPath $copy).Hash) { throw "Data verification failed: $relative" }
    }
}

foreach ($name in @('MailAssistant', 'MailAssistant-before-rounded-tray', 'MailAssistant-before-tray')) {
    Copy-VerifiedData (Join-Path $target "release/$name/data") (Join-Path $backupRoot "$name/data")
}
Copy-VerifiedData (Join-Path $current 'data') (Join-Path $staged 'data')

# Validate every absolute target before any recursive deletion or directory move.
$obsolete = @('release/MailAssistant', 'release/MailAssistant-before-rounded-tray',
    'release/MailAssistant-before-tray', 'release-rounded-tray', 'release-tray-update',
    'exercise-bank-check', 'exe-icon-check.png', 'exe-large.png', 'exe-small.png')
$paths = foreach ($relative in $obsolete) {
    $resolved = [IO.Path]::GetFullPath((Join-Path $target $relative))
    if (-not $resolved.StartsWith($target + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe cleanup path' }
    if (Test-Path -LiteralPath $resolved) {
        if ((Get-Item -LiteralPath $resolved).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Refusing a reparse-point cleanup' }
        $resolved
    }
}
$moveSource = [IO.Path]::GetFullPath($staged)
$moveTarget = [IO.Path]::GetFullPath($current)
foreach ($path in @($moveSource, $moveTarget)) {
    if (-not $path.StartsWith($target + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe move path' }
}
foreach ($path in $paths) { Remove-Item -LiteralPath $path -Recurse -Force }
Move-Item -LiteralPath $moveSource -Destination $moveTarget
$stagingParent = Split-Path -Parent $moveSource
Remove-Item -LiteralPath $stagingParent # Empty directory only, never recursive.
Write-Output "Latest program: $moveTarget\MailAssistant.exe"
Write-Output "Preserved user data: $backupRoot"
