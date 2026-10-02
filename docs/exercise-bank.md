# 前四章习题库

## 数据文件

应用随包题库：`src/main/resources/database/exercise.db`。

数据库遵循 README 第 4.1 节，包含 `chapter`、`exercise`、`asset`、`exercise_asset` 和 `metadata` 五张表。正文使用 `schemaVersion: 1` 的 JSON；`content_text` 从正文自动派生。图片为数据库中的 PNG BLOB，运行时不需要原始截图。

| 章节 | 题号范围 | 数量 | 原始截图 |
| --- | --- | --- | --- |
| 第1章 | 1～4 | 4 | Chapter01.png |
| 第2章 | 1～9 | 9 | Chapter02_1.png、Chapter02_2.png |
| 第3章 | 1～20 | 20 | Chapter03_1.png、Chapter03_2.png |
| 第4章 | 1～12 | 12 | Chapter04_1.png、Chapter04_2.png、Chapter04_3.png |
| 合计 | | 45 | 8 张 |

全部为编程题，`exercise_type=PROGRAMMING`、`require_result=1`、`allow_uml=1`。主键按 `章节号 × 1000 + 题号` 设置，例如第4章第7题为 `4007`；`exercise_no` 保留章内题号，`sort_order` 按数值排序。

## 转录与资源

- 人工转录题干及原题示例；未添加题解或程序答案。只规范空白、标点及正文中的数学符号，保留原始条件和数值。
- 第4章第7题跨两张图片的内容已经合并；第6题的 Calculator 图位于下一张图片，关联到第6题；第10题跨页内容已合并并去除页眉。
- 星号图形和控制台示例保存为 `code` 内容块，`language=text`，保留换行；这批图片中没有需要转录为 `table` 的数据表。
- 提取 10 张公式图及 3 张 UML 图，共 13 个资源。公式图直接截取原题，配套保存 LaTeX 源码及中文说明；不依赖额外公式渲染服务。
- 图片以 SHA-256 标识并去重。`metadata.asset_sources` 保存每张图的来源文件及裁切范围 `[left, top, right, bottom]`，以原图左上角为原点，右、下边界不包含在内。
- `metadata.source_files` 保存 8 张原图的 SHA-256；`exercise_sources` 保存每题对应来源，便于核对。

原图未提供章节名称，使用“第1章”至“第4章”。题目的简短标题为整理标签，原题正文在 `content` 中。

### 保留的原题约定

- 第1章第2题的加法序列没有 13，按原图保留，不自行补入。
- 第3章第5题规定 2～4 月为春季、5～7 月为夏季、8～10 月为秋季、11、12、1 月为冬季，按原图保留。
- 第3章第6题仅给出“85 → 良好”示例，未给完整成绩分界；未自行增加分界规则。
- 第3章第20题按原图保存 π 的级数公式及 n 的取值范围。
- 第4章第12题的原图字段为 `annulRate`，相关方法为 `getAnnualRate()` / `setAnnualRate(...)`；图中说明文字又使用 `annualInterestRate`。保留原图，不擅自统一命名。
- 第4章第1、2、3、7、10题明确要求绘制 UML。题干完整保留该要求，同时在 `metadata.uml_required_exercise_ids` 中记录 `[4001,4002,4003,4007,4010]`。README 当前表结构没有 `require_uml` 字段，不能把 `allow_uml` 当作必做标记；后续校验层应读取此约定或通过数据库迁移增加专用字段。

## 重建与验证

本地生成脚本及转录文本位于 `tools/exercise_bank/build.py`，原始图片保存在同目录的 `sources/`，与用户提供的图片字节一致。`tools/` 不提交到 Git，克隆仓库后可直接使用已提交的题库；如需重建，需另行取得该本地工具目录及原图。维护题干时修改脚本中的转录内容；更换图片时同步更新资源裁切坐标。

使用安装了 Pillow 的 Python 3 执行：

```shell
python tools/exercise_bank/build.py
```

脚本直接读取 README 中的 SQL 建表，写入临时数据库，验证成功后替换题库文件。失败时不会替换现有题库；遗留的 `.building` 文件应先检查再删除。Python 和 Pillow 仅用于题库制作，不是 Java 应用的运行依赖。

验证包含：SQLite 完整性、外键、章节题数和连续题号、JSON 版本与内容块、派生搜索文本、公式字段、资源引用一致性、PNG 解码、尺寸、大小及 SHA-256，且不存在未被习题引用的资源。资源图片另经目视核对，确保公式及 UML 图未被截断。

release 版仍按 README 将此内置题库复制到 `<应用根目录>/data/database/exercise.db` 后使用。
