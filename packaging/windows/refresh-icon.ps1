param([string]$Executable = (Join-Path $PSScriptRoot '../../target/release/MailAssistant/MailAssistant.exe'))
$ErrorActionPreference = 'Stop'
$executablePath = (Resolve-Path -LiteralPath $Executable).Path
# Notify Explorer after replacing an executable at the same path. No cache files are deleted.
if (-not ('MailAssistant.ShellIconRefresh' -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
namespace MailAssistant {
    public static class ShellIconRefresh {
        [DllImport("shell32.dll", CharSet = CharSet.Unicode)]
        public static extern void SHChangeNotify(uint change, uint flags, string item1, IntPtr item2);
    }
}
'@
}
[MailAssistant.ShellIconRefresh]::SHChangeNotify(0x2000, 0x1005, $executablePath, [IntPtr]::Zero)
[MailAssistant.ShellIconRefresh]::SHChangeNotify(0x08000000, 0, $null, [IntPtr]::Zero)
