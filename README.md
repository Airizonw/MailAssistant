# MailAssistant

> 面向 Java 面向对象课程作业邮件提交场景的桌面辅助工具。

## 1. 项目简介

MailAssistant 是一个使用 Java 开发的桌面应用，用于简化课程作业邮件的整理、预览、导出与发送流程。

项目的核心目标不是替代用户完成作业，而是将用户已经完成的题目内容、代码、UML 图和运行结果按照教师规定的格式自动整理为合法的邮件正文，并在用户确认后发送邮件。

当前规划版本：`v1.0.0`

---

## 2. 背景与提交规范

课程作业邮件需要满足以下格式要求：

### 2.1 收件人与抄送

- 教师邮箱：由用户自行填写并保存。
- 助教邮箱：支持填写一个或多个 CC 邮箱。
- README 中出现的邮箱仅作为格式示例，程序不得将示例邮箱硬编码为默认真实收件人。

### 2.2 邮件主题

固定格式：

```text
Java-学号-姓名-学院-第X章作业
```

示例：

```text
Java-2025302051XXX-李XX-计算机院-第3章作业
```

MailAssistant 根据用户保存的个人信息和当前选择章节自动生成主题。

### 2.3 邮件正文头部

邮件正文最前方必须写明：

```text
学院-班号-学号-姓名
```

例如：

```text
计算机学院-2501班-2025302051XXX-李XX
```

### 2.4 每道题目的正文顺序

每道题目按以下顺序组织：

1. 作业题号与题目内容
2. 解答内容 / 程序代码
3. 程序运行结果
4. UML 图（可选）

所有作业内容直接写入邮件正文，不使用普通邮件附件提交。

> 注意：为了符合课程要求，程序中的 `.java` 文件、UML 图、运行结果截图等素材虽然可以从本地导入，但最终应转换/嵌入到邮件正文中，而不是作为独立附件发送。

---

## 3. v1.0.0 功能规划

### 3.1 邮箱支持

v1.0.0 计划支持：

- QQ 邮箱
- 网易 163 邮箱
- 武汉大学 WHU 邮箱

设计原则：

- 邮箱提供商配置与业务逻辑分离。
- 不在代码中硬编码用户名、密码、授权码或 Token。
- 优先采用 OAuth2 / 官方授权机制。
- 如果邮箱服务商仅允许 SMTP 授权码，则保存的是授权码或 Token，而不是邮箱登录密码。
- 武汉大学邮箱的 SMTP/认证参数采用“可配置 Provider”设计，避免学校邮箱服务升级后必须修改业务代码。

### 3.2 用户个人信息

用户可保存：

- 学院
- 班级 / 班号
- 学号
- 姓名
- 教师邮箱
- 一个或多个助教邮箱
- 常用邮箱账号
- 邮箱服务商
- 其他后续可扩展字段

所有个人信息仅在本地保存，并采用加密存储。

### 3.3 章节与习题选择

用户可以：

1. 选择课程章节；
2. 浏览该章节下的习题；
3. 勾选需要提交的题目；
4. 将题目加入本次作业。

习题数据由本地题库提供，不需要联网读取。

### 3.4 代码提交

每道编程题支持两种方式：

- 在程序文本编辑框中直接粘贴 / 输入代码；
- 选择本地 `.java` 文件，由程序读取文件内容。

程序不把 `.java` 文件作为邮件附件发送，而是读取源码并插入生成的正文。

### 3.5 UML 图

UML 图为可选内容。

支持：

- PNG
- JPG / JPEG

后续可以增加：

- SVG
- PlantUML
- Mermaid 转图片

### 3.6 运行结果截图

运行结果截图为编程题必填项。

在生成正文前，程序应进行完整性检查：

- 编程题是否存在代码；
- 是否存在运行结果截图；
- 必填个人信息是否完整；
- 教师邮箱是否合法；
- 是否至少选择一道题目。

若缺少必填内容，禁止直接发送，并明确指出缺失项。

### 3.7 自动生成正文

用户完成题目添加后，可点击：

```text
生成正文
```

程序根据课程规定自动生成：

- 邮件主题；
- 正文头部；
- 各题题号；
- 题目内容；
- 解答内容；
- Java 源代码；
- 运行结果截图；
- UML 图（若存在）。

### 3.8 预览与编辑

生成后进入预览页面。

预览界面需要支持：

- 查看最终邮件主题；
- 查看 To / CC；
- 编辑正文；
- 删除或调整题目内容；
- 调整图片位置；
- 返回继续编辑原始作业；
- 导出为 DOCX；
- 确认发送。

### 3.9 DOCX 导出

使用 Apache POI 将预览内容导出为 `.docx`。

导出内容应尽量与最终邮件正文一致，包括：

- 标题；
- 个人信息；
- 题号；
- 题目正文；
- 代码；
- UML 图；
- 运行结果截图。

### 3.10 邮件发送

邮件发送仅在用户主动点击“提交”后进行。

发送流程：

```text
编辑作业
    ↓
生成正文
    ↓
预览
    ↓
点击提交
    ↓
二次确认
    ↓
发送
    ↓
显示发送结果
```

二次确认窗口至少显示：

- 发件邮箱；
- 收件人；
- 抄送人；
- 邮件主题；
- 题目数量。

只有用户再次确认后才建立网络连接并发送邮件。

---

## 4. 数据存储方案

MailAssistant 建议采用“分层存储”，不要把所有内容放进同一个文件。

### 4.1 习题库：SQLite + 结构化正文 + 内嵌资源

习题可能包含文字、代码、示意图、数据表格和数学公式，不能只用一个纯文本字段保存。v1.0.0 推荐将完整题库保存在一个 SQLite 文件中：

| 内容 | 存储方式 | 用途 |
| --- | --- | --- |
| 章节、题号、题型、排序与要求 | 普通关系表字段 | 筛选、排序与完整性检查 |
| 题目正文 | `content` 字段中的结构化 JSON | 保存文字、图片、表格、公式的顺序与语义 |
| 可搜索文字 | `content_text` 字段中的纯文本 | 搜索与文字摘要，由结构化正文派生 |
| 图片与公式渲染图 | 资源表中的 BLOB 二进制数据 | 离线显示，避免外部文件丢失 |
| 数据库与题库版本 | `metadata` 表 | 格式迁移与内容更新 |

SQLite 负责章节、习题和资源管理；JSON 只描述单道题目的正文结构，不替代关系表。公开题库不包含用户隐私，无需整体加密，也不得写入用户答案、截图或邮箱凭据。

#### 4.1.1 题目正文格式

正文使用带 `schemaVersion` 的内容块数组，按照数组顺序展示。v1.0.0 至少支持：

- `paragraph`：段落，内部 `inlines` 支持 `text` 和行内 `formula`；
- `code`：代码文本与语言标识，保留缩进和换行；
- `image`：题目插图、流程图、坐标图或复杂图表，通过 `assetId` 引用资源；
- `table`：结构化表头与数据行，单元格使用与段落相同的 `inlines` 格式；
- `formula`：独立公式，保存 LaTeX 源码及其 PNG 渲染资源引用。

示例（资源 ID 仅作示意，导入时必须有对应资源记录）：

```json
{
  "schemaVersion": 1,
  "blocks": [
    {
      "type": "paragraph",
      "inlines": [
        { "type": "text", "text": "根据下图和表格，计算 " },
        {
          "type": "formula",
          "latex": "x^2",
          "assetId": "formula-x-squared",
          "alt": "x 的平方"
        },
        { "type": "text", "text": " 的值。" }
      ]
    },
    {
      "type": "image",
      "assetId": "figure-001",
      "alt": "矩形的长为 x，宽为 2",
      "caption": "图 1：矩形示意图"
    },
    {
      "type": "table",
      "caption": "表 1：已知数据",
      "headers": [
        { "inlines": [{ "type": "text", "text": "变量" }] },
        { "inlines": [{ "type": "text", "text": "值" }] }
      ],
      "rows": [
        [
          { "inlines": [{ "type": "text", "text": "x" }] },
          { "inlines": [{ "type": "text", "text": "3" }] }
        ]
      ]
    },
    {
      "type": "formula",
      "latex": "S = \\frac{1}{2} a h",
      "assetId": "formula-area",
      "alt": "S 等于二分之一乘以 a 乘以 h",
      "number": "(1)"
    }
  ]
}
```

格式约定：

- `latex` 保存公式本身，不包含 `$...$` 或 `\\[...\\]` 定界符；JSON 中的反斜杠需要转义。
- 行内公式置于 `inlines`，独立公式直接作为内容块；两者都要求可读的 `alt` 和渲染图。
- 简单数据表优先存为 `table`，保留可复制和可编辑的数据；v1.0.0 要求每行单元格数量与表头一致。
- 复杂合并单元格表格、统计图、几何图等可以先作为 `image` 保存，并补充文字说明。后续需要重绘统计图时再扩展图表数据块。
- 正文不保存任意 HTML、JavaScript、远程图片 URL、绝对磁盘路径或 Base64 图片。渲染器根据已知内容块生成展示内容，并对文本进行转义。

#### 4.1.2 推荐表结构

```sql
-- 每个 JDBC 连接初始化时都要开启外键约束。
PRAGMA foreign_keys = ON;

CREATE TABLE chapter (
    id          INTEGER PRIMARY KEY,
    chapter_no  INTEGER NOT NULL UNIQUE,
    title       TEXT NOT NULL,
    sort_order  INTEGER NOT NULL
);

CREATE TABLE exercise (
    id                INTEGER PRIMARY KEY,
    chapter_id        INTEGER NOT NULL,
    exercise_no       TEXT NOT NULL,
    title             TEXT,
    content           TEXT NOT NULL,
    content_text      TEXT NOT NULL DEFAULT '',
    exercise_type     TEXT NOT NULL,
    require_result    INTEGER NOT NULL DEFAULT 0,
    allow_uml         INTEGER NOT NULL DEFAULT 1,
    sort_order        INTEGER NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id),
    UNIQUE (chapter_id, exercise_no)
);

CREATE TABLE asset (
    id          TEXT PRIMARY KEY NOT NULL,
    mime_type   TEXT NOT NULL CHECK (mime_type IN ('image/png', 'image/jpeg')),
    data        BLOB NOT NULL,
    sha256      TEXT NOT NULL UNIQUE CHECK (length(sha256) = 64),
    byte_size   INTEGER NOT NULL CHECK (byte_size > 0 AND byte_size = length(data)),
    width_px    INTEGER NOT NULL CHECK (width_px > 0),
    height_px   INTEGER NOT NULL CHECK (height_px > 0)
);

-- 同一资源可被多道题目复用；删除题目不会删除共享资源。
CREATE TABLE exercise_asset (
    exercise_id INTEGER NOT NULL,
    asset_id    TEXT NOT NULL,
    PRIMARY KEY (exercise_id, asset_id),
    FOREIGN KEY (exercise_id) REFERENCES exercise(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE RESTRICT
);

CREATE TABLE metadata (
    key   TEXT PRIMARY KEY NOT NULL,
    value TEXT NOT NULL
);

CREATE INDEX idx_exercise_chapter_sort ON exercise(chapter_id, sort_order);
```

`metadata` 至少保存 `schema_version`（数据库结构版本）和 `content_version`（题库内容版本）。正文中的 `schemaVersion` 单独标记 JSON 格式版本，三者不要混用。

外键只约束关系表，不能自动检查 JSON 中的 `assetId`。题库导入程序必须递归校验内容块及单元格，确保所有资源引用存在，并在同一事务中写入正文、资源和 `exercise_asset` 关系。`content_text` 同步从段落、代码、表格文字、图片说明及公式 LaTeX / `alt` 派生，不作为第二份可独立编辑的正文。

#### 4.1.3 图表与公式资源

- 图片以原始二进制 BLOB 保存，通过 SHA-256 去重。说明文字属于正文中的引用位置，同一图片在不同题目中可以有不同说明。
- v1.0.0 运行时只使用 PNG / JPEG。公式与线条图优先采用 PNG；扫描题目或照片可采用 JPEG。SVG 等源素材需要在题库制作阶段转换。
- 公式同时保存 LaTeX 源码和预先生成的 PNG：源码用于维护、检索和重新渲染，PNG 用于各输出端的一致展示。
- 公式图片在题库制作或导入阶段准备好；桌面应用不要求用户安装 TeX，也不依赖在线公式服务或 CDN。公式编辑后的图片必须重新生成并一起保存，不能继续引用旧图。
- 资源导入时校验实际图片格式、能否解码、像素尺寸、文件大小与 SHA-256；限制单张图片及整份题库的体积。显示时保持宽高比，正文中的公式图按行内或独立块布局缩放。
- v1.0.0 不识别扫描图片中的文字或公式；无法转录的内容可以保留为图片，并提供文字说明。

#### 4.1.4 预览、邮件与 DOCX 导出

三个输出端使用同一份结构化正文和资源数据：

| 输出端 | 文字与表格 | 图片与公式 |
| --- | --- | --- |
| JavaFX 预览 | 生成经过转义的 HTML 段落、代码块和表格 | 从数据库读取，必要时写入 `data/cache/`，使用本地资源展示 |
| HTML 邮件 | 生成静态 HTML 与内联样式 | 使用 `cid:` 引用 MIME 内嵌图片，包含公式 PNG |
| DOCX | 转为 POI 段落、代码文本和原生 Word 表格 | 插入图片，公式在 v1.0.0 中以图片呈现 |

公式 PNG 可以离线显示，但不是 Word 中可编辑的原生公式。原生 Word 公式转换可在后续版本扩展。邮件输出不依赖 JavaScript 执行公式排版，也不引用本地文件路径。

题目自带的图表和公式属于题目内容，不替代用户必须提供的运行结果截图，也不计作用户可选的 UML 图。生成作业草稿时，应保存所选题目正文和资源的快照到草稿目录，沿用草稿加密要求，避免后续题库更新导致已保存作业内容变化。

#### 4.1.5 发布与完整性检查

公开题库随项目保存在 `src/main/resources/database/exercise.db`，图表和公式图片已包含在数据库中，不需要额外的图片文件夹。release 版首次运行时将内置题库复制到 `<应用根目录>/data/database/exercise.db`，再通过 JDBC 访问；运行过程中按只读题库使用，不将缓存或用户数据写回其中。运行时文件统一位于应用根目录下的 `data/`，不使用 C 盘用户目录。

发布或导入题库前至少检查：

- 数据库外键、章节内题号唯一性与题目排序；
- 正文 JSON 格式版本、允许的内容块类型和必填字段；
- 表格行列数量、公式源码与渲染图是否齐全；
- JSON 资源引用与 `exercise_asset` 关系一致，图片可解码且摘要匹配；
- 同一题目包含文字、图表、行内公式和独立公式时，预览、邮件正文与 DOCX 的内容顺序一致。

现有纯文本题库迁移时，将原 `content` 包装为一个 `paragraph` 内容块，并保留原文到 `content_text`。题库更新需先校验候选数据库，关闭数据库连接后再替换运行时副本，保留可恢复的旧版本；不要在每次启动时直接覆盖已有题库。

### 4.2 用户配置：AES-GCM 加密文件

用户个人信息建议单独保存为：

```text
data/user-profile.enc
```

明文结构可以在加密前使用 JSON，例如：

```json
{
  "college": "计算机学院",
  "className": "2501班",
  "studentId": "2025302051XXX",
  "name": "李XX",
  "teacherEmail": "teacher@example.com",
  "assistantEmails": [
    "ta1@example.com",
    "ta2@example.com"
  ]
}
```

但磁盘上不得直接保存上述明文 JSON。

推荐使用：

```text
AES-256-GCM
```

原因：

- 同时提供机密性和完整性保护；
- 可以检测密文被修改；
- Java 标准加密 API 可以直接实现；
- 比自行设计加密格式可靠。

每次加密应使用新的随机 IV / nonce。

### 4.3 加密密钥

禁止：

- 把 AES 密钥写死在 Java 源码中；
- 把密钥放在 `config.properties`；
- 把密钥和密文放在同一文件中；
- 使用 Base64 代替加密。

在 Windows 11 上，推荐将随机生成的主密钥交给 Windows 系统凭据保护能力保存。

可实现方案：

```text
Java
  ↓
JNA
  ↓
Windows DPAPI / Credential Manager
```

以后若支持 macOS / Linux，可封装统一接口：

```java
public interface SecretStore {
    void save(String key, byte[] secret);
    byte[] load(String key);
    void delete(String key);
}
```

然后分别实现：

- Windows Credential Manager / DPAPI
- macOS Keychain
- Linux Secret Service

### 4.4 邮箱凭据

邮箱认证信息与普通个人资料分开存储。

推荐：

```text
普通个人资料
    → AES-GCM 加密配置文件

邮箱 Token / SMTP 授权码
    → 操作系统安全凭据库
```

不得保存：

```text
邮箱登录密码明文
```

优先级：

```text
OAuth2 Token
    >
服务商 SMTP 授权码 / 应用密码
    >
普通登录密码
```

普通登录密码不建议作为程序持久化认证方式。

### 4.5 作业草稿

用户尚未发送的作业内容建议保存在：

```text
data/drafts/
```

每个草稿可以使用 JSON 描述结构，并将敏感内容整体加密。

例如：

```text
data/drafts/
├─ chapter-03-20261002-001.enc
└─ chapter-04-20261010-001.enc
```

草稿包含：

- 当前章节；
- 已选题目；
- 用户输入答案；
- 代码；
- UML 图片路径或内部副本；
- 运行结果截图路径或内部副本；
- 正文修改结果；
- 创建时间；
- 修改时间。

为了避免原始图片被用户移动或删除后草稿失效，推荐在保存草稿时将图片复制到应用自己的工作目录。

---

## 5. 推荐技术栈

### 5.1 基础环境

建议：

```text
JDK 21 LTS
Maven
JavaFX
```

### 5.2 UI

推荐：

```text
JavaFX
```

理由：

- 比 Swing 更适合现代桌面 UI；
- CSS 样式能力更强；
- 图片、WebView、复杂布局支持更方便；
- 后续代码高亮与富文本预览扩展更容易。

### 5.3 邮件

建议使用：

```text
Jakarta Mail
```

负责：

- SMTP 连接；
- MIME 邮件生成；
- HTML 邮件；
- 内嵌图片；
- To / CC；
- TLS。

注意：课程要求“图片放在邮件正文中”，因此图片应使用 MIME `Content-ID` 作为内嵌资源引用，而不是普通附件。

### 5.4 DOCX

推荐：

```text
Apache POI
```

用于：

- 创建 Word 文档；
- 插入段落；
- 插入代码文本；
- 插入 PNG/JPEG 图片；
- 设置简单格式。

### 5.5 SQLite

推荐：

```text
org.xerial:sqlite-jdbc
```

### 5.6 JSON

推荐：

```text
Jackson
```

用于：

- 用户配置对象序列化；
- 作业草稿；
- 应用设置；
- Provider 配置。

### 5.7 后续代码高亮

v1.x 后续版本可考虑：

```text
RichTextFX
```

用于编辑器代码高亮。

邮件最终 HTML 中的代码高亮可通过程序自身生成 `<pre><code>` 样式，而不依赖浏览器端 JavaScript。

---

## 6. 邮件正文建议格式

推荐最终邮件使用 HTML，而不是纯文本。

示意：

```text
计算机学院-2501班-2025302051XXX-李XX

第 1 题
题目：
……

解答：
……

代码：
public class Main {
    ...
}

程序运行结果：
[运行结果截图]

UML：
[UML 图]


第 2 题
……
```

HTML 邮件应保证：

- 不依赖外部 CDN；
- 不使用 JavaScript；
- CSS 尽量使用内联样式；
- 图片采用 CID 内嵌；
- 即使邮箱客户端样式能力较弱，正文仍然可阅读。

---

## 7. 核心业务对象

建议主要领域模型：

```text
UserProfile
MailAccount
MailProviderConfig
Chapter
Exercise
Submission
SubmissionItem
CodeContent
ImageResource
MailDraft
MailPreview
```

示例：

```java
public class SubmissionItem {
    private Exercise exercise;
    private String answer;
    private String sourceCode;
    private Path umlImage;
    private Path resultImage;
}
```

---

## 8. 核心模块

建议分为：

```text
ui
application
domain
repository
mail
security
export
storage
validation
config
util
```

其中：

- `ui`：JavaFX 界面；
- `application`：用例协调；
- `domain`：业务对象；
- `repository`：数据访问接口；
- `storage`：SQLite / 本地文件实现；
- `mail`：邮件生成与发送；
- `security`：AES-GCM、系统密钥库；
- `export`：DOCX 导出；
- `validation`：提交前检查；
- `config`：程序配置；
- `util`：通用工具。

详细目录见《项目结构.md》。

---

## 9. 发送前校验

发送前必须执行统一校验。

推荐规则：

```text
[必填] 姓名
[必填] 学号
[必填] 学院
[必填] 班级
[必填] 教师邮箱
[必填] 发件邮箱
[必填] 至少一道习题
[编程题必填] 源代码
[编程题必填] 运行结果截图
[可选] UML 图
```

还应检查：

- 邮箱格式是否合法；
- CC 是否存在重复地址；
- To 与 CC 是否重复；
- `.java` 文件能否读取；
- 图片格式是否合法；
- 图片大小是否过大；
- 生成正文是否成功；
- 邮箱认证状态是否有效。

---

## 10. 安全原则

MailAssistant 应遵循：

1. 默认离线运行；
2. 只有用户主动认证邮箱或发送邮件时联网；
3. 不上传作业内容到第三方服务器；
4. 不在日志中输出授权码、Token、密码；
5. 不在异常信息中显示完整凭据；
6. 不将密钥写入 Git；
7. 不把真实个人信息提交到 Git；
8. 发送前必须二次确认；
9. 发送操作不得后台静默执行；
10. 用户可以清除本地个人信息与邮箱授权信息。

建议 `.gitignore`：

```gitignore
target/
.idea/
.vscode/

data/
logs/
*.log

*.enc
*.key
*.db-journal
*.db-shm
*.db-wal

.env
```

如果 `exercise.db` 是项目自带的公开题库，则应单独放在 `src/main/resources/database/`，不要被上述规则忽略。

---

## 11. v1.0.0 建议开发顺序

```text
阶段 1
题库 + 章节 / 题目浏览

阶段 2
用户资料 + 本地加密

阶段 3
作业编辑器
代码 / Java 文件 / UML / 运行截图

阶段 4
正文生成器

阶段 5
预览与编辑

阶段 6
DOCX 导出

阶段 7
邮箱认证 + SMTP 发送

阶段 8
完整性校验 + 二次确认

阶段 9
打包发布
```

邮件发送建议最后接入，以便先把完全离线的核心流程稳定下来。

---

## 12. 后续版本规划

### v1.1.x

- Java 代码语法高亮；
- 更美观的代码块；
- 多套邮件正文模板；
- 自动保存草稿；
- 最近提交历史；
- 图片压缩。

### v1.2.x

- PlantUML；
- Mermaid；
- 题目搜索；
- 题库在线更新；
- 多课程配置。

### v2.0.0

可进一步考虑：

- 插件化邮箱 Provider；
- 多课程作业模板；
- 多账号管理；
- 跨平台系统凭据库；
- 可扩展 DOCX / HTML 模板系统。

---

## 13. 项目定位

MailAssistant 的核心原则是：

```text
用户完成作业
        ↓
MailAssistant 负责整理与格式化
        ↓
用户检查
        ↓
用户确认
        ↓
MailAssistant 发送
```

程序不会自动生成用户的作业答案，也不会在未经确认的情况下发送邮件。

