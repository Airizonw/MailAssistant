package com.mailassistant.desktop;

import java.nio.file.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.util.*;
import com.sun.jna.platform.win32.COM.util.annotation.ComObject;

/** Uses the Windows Desktop known folder, including redirected/OneDrive desktops. */
public final class DesktopShortcut {
    @ComObject(progId="WScript.Shell")
    public interface WindowsShell extends IDispatch {}
    public void create(Path root) throws Exception {
        String launcher = System.getProperty("jpackage.app-path");
        Path target;
        String arguments = "";
        if (launcher != null && !launcher.isBlank()) target = Path.of(launcher);
        else {
            target = Path.of(System.getProperty("java.home"), "bin", "javaw.exe");
            Path location = Path.of(DesktopShortcut.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            arguments = "-Dfile.encoding=UTF-8 " + quoteArgument("-Dmailassistant.home=" + root.toAbsolutePath());
            if (Files.isRegularFile(location)) arguments += " -jar " + quoteArgument(location.toString());
            else {
                String modules = System.getProperty("jdk.module.path", "");
                if (!modules.isBlank()) arguments += " --module-path " + quoteArgument(modules) + " --add-modules javafx.controls,javafx.web";
                arguments += " -cp " + quoteArgument(System.getProperty("java.class.path")) + " com.mailassistant.Launcher";
            }
        }
        String icon = Files.exists(root.resolve("packaging/icons/MailAssistant.ico"))
                ? root.resolve("packaging/icons/MailAssistant.ico").toString() : target.toString();
        create(null, target, arguments, root, icon);
    }

    // An explicit destination is used by tests; production resolves the real Desktop.
    public void create(Path directory, Path target, String arguments, Path workingDirectory, String icon) throws Exception {
        if (!Files.isRegularFile(target)) throw new IllegalStateException("找不到程序启动文件，无法创建快捷方式");
        Path desktop = directory == null ? Path.of(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Desktop)) : directory;
        Path link=desktop.resolve("MailAssistant.lnk");
        COMUtils.checkRC(Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED));
        ObjectFactory factory=new ObjectFactory();
        try {
            WindowsShell shell=factory.createObject(WindowsShell.class);
            IDispatch shortcut=shell.invokeMethod(IDispatch.class,"CreateShortcut",link.toAbsolutePath().toString());
            shortcut.setProperty("TargetPath",target.toAbsolutePath().toString());
            shortcut.setProperty("Arguments",arguments);
            shortcut.setProperty("WorkingDirectory",workingDirectory.toAbsolutePath().toString());
            shortcut.setProperty("IconLocation",icon);
            shortcut.invokeMethod(Void.class,"Save");
            if(!Files.isRegularFile(link))throw new IllegalStateException("无法创建桌面快捷方式，请检查桌面文件夹权限");
        } finally { factory.disposeAll(); Ole32.INSTANCE.CoUninitialize(); }
    }
    private static String quoteArgument(String value) { return "\"" + value + "\""; }
}
