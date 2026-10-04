# 第1～19章习题库

## 数据文件

应用随包题库：`src/main/resources/database/exercise.db`。

数据库包含 `chapter`、`exercise`、`asset`、`exercise_asset` 和 `metadata` 五张表。正文使用 `schemaVersion: 1` 的 JSON；`content_text` 从正文自动派生。图片为数据库中的 PNG BLOB，运行时不需要原始截图。

| 章节 | 题号范围 | 数量 | 原始截图 |
| --- | --- | --- | --- |
| 第1章 | 1～4 | 4 | Chapter01.png |
| 第2章 | 1～9 | 9 | Chapter02_1.png、Chapter02_2.png |
| 第3章 | 1～20 | 20 | Chapter03_1.png、Chapter03_2.png |
| 第4章 | 1～12 | 12 | Chapter04_1.png、Chapter04_2.png、Chapter04_3.png |
| 第5章 | 1～15 | 15 | Chapter05_1.png、Chapter05_2.png |
| 第6章 | 1～9 | 9 | Chapter06.png |
| 第7章 | 1～7 | 7 | Chapter07_1.png、Chapter07_2.png、Chapter07_3.png |
| 第8章 | 1～9 | 9 | Chapter08.png |
| 第9章 | 1～14 | 14 | Chapter09_1.png、Chapter09_2.png、Chapter09_3.png |
| 第10章 | 1～6 | 6 | Chapter10.png |
| 第11章 | 1～6 | 6 | Chapter11_1.png、Chapter11_2.png |
| 第12章 | 1～10 | 10 | Chapter12_1.png、Chapter12_2.png |
| 第13章 | 1～12 | 12 | Chapter13.png |
| 第14章 | 1～6 | 6 | Chapter14.png |
| 第15章 | 1～8 | 8 | Chapter15_1.png、Chapter15_2.png |
| 第16章 | 1～9 | 9 | Chapter16_1.png、Chapter16_2.png |
| 第17章 | 1～4 | 4 | Chapter17_1.png、Chapter17_2.png |
| 第18章 | 1～7 | 7 | Chapter18_1.png、Chapter18_2.png |
| 第19章 | 1～6 | 6 | Chapter19.png |
| 合计 | | 173 | 34 张 |

内容版本为 `2026.10.03.1`。本次新增第5～19章的 128 道题、26 张原图及 25 张内嵌资源；原有前四章的 45 道题、章节记录和 13 张资源逐项比对，保持不变。

全部为编程题，`exercise_type=PROGRAMMING`、`require_result=1`、`allow_uml=1`。主键按 `章节号 × 1000 + 题号` 设置，例如第4章第7题为 `4007`；`exercise_no` 保留章内题号，`sort_order` 按数值排序。

## 转录与资源

- 人工转录题干及原题示例；未添加题解或程序答案。只规范空白、标点及正文中的数学符号，保留原始条件和数值。
- 第4章第7题跨两张图片的内容已经合并；第6题的 Calculator 图位于下一张图片，关联到第6题；第10题跨页内容已合并并去除页眉。
- 星号图形和控制台示例保存为 `code` 内容块，`language=text`，保留换行；这批图片中没有需要转录为 `table` 的数据表。
- 合并第5章第11题、第7章第1/3题、第9章第3题、第11章第2题、第12章第3题、第18章第2题的跨页内容；第15章第1/2题、第16章第5/6题关联下一页的配图。页眉、二维码不作为题目正文导入。
- 提取 12 张公式图、8 张 UML/类层次图及 18 张界面图，共 38 个资源。公式图直接截取原题，配套保存 LaTeX 源码及中文说明；不依赖额外公式渲染服务。
- 图片以 SHA-256 标识并去重。`metadata.asset_sources` 保存每张图的来源文件及裁切范围 `[left, top, right, bottom]`，以原图左上角为原点，右、下边界不包含在内。
- `metadata.source_files` 保存 34 张原图的 SHA-256；`exercise_sources` 保存每题对应来源，`chapter_exercise_counts` 保存各章题数，便于核对。

原图未提供章节名称，使用“第1章”至“第19章”。题目的简短标题为整理标签，原题正文在 `content` 中。原题提供的 Java 代码、方法签名、控制台输出、SQL 字段说明和伪代码保存为 `code` 块，保留缩进和换行，不作为题解。

### 保留的原题约定

- 第1章第2题的加法序列没有 13，按原图保留，不自行补入。
- 第3章第5题规定 2～4 月为春季、5～7 月为夏季、8～10 月为秋季、11、12、1 月为冬季，按原图保留。
- 第3章第6题仅给出“85 → 良好”示例，未给完整成绩分界；未自行增加分界规则。
- 第3章第20题按原图保存 π 的级数公式及 n 的取值范围。
- 第4章第12题的原图字段为 `annulRate`，相关方法为 `getAnnualRate()` / `setAnnualRate(...)`；图中说明文字又使用 `annualInterestRate`。保留原图，不擅自统一命名。
- 第4章第1、2、3、7、10题及第7章第4题明确要求绘制 UML。题干完整保留该要求，同时在 `metadata.uml_required_exercise_ids` 中记录 `[4001,4002,4003,4007,4010,7004]`。当前数据库表结构没有 `require_uml` 字段，不能把 `allow_uml` 当作必做标记；应用校验层读取此元数据。
- 第8章第9题对“21世纪”的描述及其 `2000.1.1～2099.12.31` 测试范围均按原图保留。
- 第11章第5题的默认值 `unsigned`、第6题的 `firstName = "ZEGNG"` 按原图保留，未改成其他单词或姓名。
- 第15章第7题正文要求显示 5 个文本节点，原图仅可见 4 个 Hello；正文与配图均保留。第18章第6题包含中文占位内容的伪代码，不补成可运行答案。

### 原图范围与缺失引用

这些是教材习题中的外部引用，不是本次漏导；保留题干原文，并写入 `metadata.source_limitations`：

- 第7章第2题引用图7-2，截图未提供该图；未推测 A～E 的继承关系。
- 第7章第6题及第9章第9/10/12题引用已有 Shape、Employee、Circle、Student 类，截图未附原类定义。
- 第12章第2题引用程序8.6；第18章第1/3题引用程序18.1/18.6，截图未附这些程序。
- 第15章第8题引用 52 张纸牌图片；第17章第1/4题引用 books/products 表，未提供配套图片文件或建表语句。
- 第7章第3题第（2）项右边缘略有截断，`play()` 方法名按同题 UML 图和上下文还原。

## 重建与验证

本地生成脚本及转录文本位于 `tools/exercise_bank/build.py` 和 `chapters_05_19.py`，原始图片保存在同目录的 `sources/`，与用户提供的图片字节一致。`tools/` 不提交到 Git，克隆仓库后可直接使用已提交的题库；如需重建，需另行取得该本地工具目录及原图。维护题干时修改脚本中的转录内容；更换图片时同步更新资源裁切坐标。

使用安装了 Pillow 的 Python 3 执行：

```shell
python tools/exercise_bank/build.py
```

脚本直接读取 README 中的 SQL 建表，写入临时数据库，验证成功后将旧题库按 SHA-256 文件名备份到 `tools/exercise_bank/backups/`，再替换题库文件。失败时不会替换现有题库；遗留的 `.building` 文件应先检查再删除。Python 和 Pillow 仅用于题库制作，不是 Java 应用的运行依赖。

验证包含：SQLite 完整性、外键、章节题数和连续题号、JSON 版本与内容块、派生搜索文本、公式字段、资源引用一致性、PNG 解码、尺寸、大小及 SHA-256、来源文件与习题的关联，且不存在未被习题引用的资源。`audit.py` 与原 45 题备份逐项比较，核对全部资源与原图裁切的像素一致性，并在 `review/` 生成新增资源联系表供目视复核。已核对全部 25 张新增资源，没有因提取而截断图表内容。应用的 `CoreWorkflowTest` 验证 19 章题数、173 题正文解析及 HTML 渲染、38 张资源和 6 道 UML 必做题。

release 版按 README 将此内置题库复制到 `<应用根目录>/data/database/exercise.db` 后使用。已有运行时副本在内置内容版本更新时，通过完整性、外键和结构版本检查后升级；旧副本备份到同目录 `backups/`，相同版本或更高版本不覆盖。
