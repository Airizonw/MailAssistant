param(
    [string]$Destination = 'target/release',
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
Push-Location -LiteralPath $projectRoot
try {
    $destinationPath = [IO.Path]::GetFullPath((Join-Path $projectRoot $Destination))
    $releasePath = Join-Path $destinationPath 'MailAssistant'
    if (Test-Path -LiteralPath $releasePath) { throw "发布目录已存在，请先备份或移动：$releasePath" }
    $buildArgs = @('-B', 'package', 'dependency:copy-dependencies', '-DincludeScope=runtime', '-DoutputDirectory=target/package-input')
    if ($SkipTests) { $buildArgs += '-DskipTests' }
    & mvn @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Maven 构建失败' }
    $jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'target') -Filter 'mail-assistant-*.jar' | Select-Object -First 1
    if (-not $jar) { throw '未找到项目 JAR' }
    Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $projectRoot 'target/package-input')
    $packageArgs = @('--type', 'app-image', '--name', 'MailAssistant', '--app-version', '1.0.0',
        '--input', 'target/package-input', '--main-jar', $jar.Name,
        '--main-class', 'com.mailassistant.Launcher', '--dest', $destinationPath,
        '--java-options', '-Dfile.encoding=UTF-8', '--vendor', 'MailAssistant')
    $icon = Join-Path $projectRoot 'packaging/icons/MailAssistant.ico'
    if (Test-Path -LiteralPath $icon) { $packageArgs += @('--icon', $icon) }
    & jpackage @packageArgs
    if ($LASTEXITCODE -ne 0) { throw 'jpackage 构建失败' }
    Write-Output (Join-Path $releasePath 'MailAssistant.exe')
} finally {
    Pop-Location
}
