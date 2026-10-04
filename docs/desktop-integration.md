# 图标与系统托盘

## 窗口图标

`desktop/AppIconProvider` 已在启动入口接入，按顺序加载以下可选文件：

```text
src/main/resources/images/icons/app-16.png
src/main/resources/images/icons/app-32.png
src/main/resources/images/icons/app-48.png
src/main/resources/images/icons/app-128.png
src/main/resources/images/icons/app-256.png
```

上述 PNG 已从用户提供的 ICO 导出，用于窗口、任务栏和系统托盘。资源缺失或解码失败不会阻止窗口启动。

Windows 可执行文件图标独立位于 `packaging/icons/MailAssistant.ico`。jpackage 脚本检测到该文件时自动使用；没有图标时使用 JDK 默认图标。

## 托盘接口

首次显示菜单时先应用 CSS 并测量内容的首选尺寸，再设置窗口大小和位置；显示后按实际尺寸再次定位，避免未显示的 Stage 尺寸为 NaN 导致菜单落在屏幕左上角。定位使用 JavaFX 坐标及对应显示器的可用区域。

`AwtTrayService` 负责系统托盘图标，右键打开 JavaFX `TrayMenu`：微软雅黑中文、圆角浅色卡片和青绿色按钮，与主程序共用 CSS。按顺序提供「主界面」「个人信息」「草稿箱」「退出」，点击外部或 Escape 收起菜单；按屏幕可用区域定位，支持缩放和多显示器。双击托盘图标或点击「主界面」恢复当前窗口及编辑页面；「个人信息」打开个人中心；「草稿箱」显示已保存的草稿，支持打开和确认删除，最多保留最近保存的 30 条。后台任务进行中，页面切换暂不执行，窗口仍可恢复。

托盘与窗口生命周期：

1. 在 AWT EventQueue 创建与移除 TrayIcon，加载多分辨率图标；菜单回调通过 `Platform.runLater` 切回 JavaFX 线程。
2. 仅在托盘安装成功后设置 `Platform.setImplicitExit(false)`；右上角关闭按钮默认隐藏窗口到托盘，保留当前编辑内容及后台任务，不触发退出提示。
3. 点击「退出」直接调用 `mayExit`，保留未保存草稿和发送中的保护。取消退出后仍可使用窗口与托盘；真正退出时移除图标并停止后台任务。
4. 托盘不可用或图标安装失败时保留正常退出行为，不隐藏窗口。

接口位于独立 `desktop` 包，邮箱服务和领域模型不依赖 AWT 或托盘状态。

## 单实例与题库更新

启动时先取得应用 `data/app.lock` 的操作系统文件锁，再创建主窗口和加载数据。同一应用目录重复启动会立即退出第二个进程；首次启动的窗口或托盘实例保持不变。锁保持到程序真正退出，崩溃后由系统释放；锁文件本身保留，不能通过删除锁文件判断或解除占用。

启动时校验内置题库和运行时题库的结构版本、完整性及外键。若内置 `content_version` 更新，先把旧库按 SHA-256 文件名备份到 `data/database/backups/`，关闭数据库连接后替换为新题库。相同版本、更高版本及自定义版本不覆盖。此更新只操作题库，不修改配置、密钥、草稿和导出文件。当前版本内置第1～19章，共173题。

## Windows 应用目录构建

在 Windows、JDK 21（包含 jpackage）和 Maven 环境下执行：

```powershell
./packaging/windows/package.ps1
```

输出 `target/release/MailAssistant/MailAssistant.exe`，运行时自带 Java，并使用已提供的 ICO。脚本不会覆盖已有发布目录；若已存在，请先自行备份/移动该目录再构建。此脚本生成 app-image，不生成安装器，不需要 WiX。

可以通过 `-Destination target/release-next` 构建到独立目录。替换原路径的 EXE 后，若资源管理器仍显示旧 Java 图标，运行 `./packaging/windows/refresh-icon.ps1` 通知 Windows 更新图标缓存，无需删除缓存或重启资源管理器。
