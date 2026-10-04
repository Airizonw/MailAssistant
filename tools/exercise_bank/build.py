"""Build the bundled exercise bank from reviewed transcriptions and source scans.

Requires Python 3 and Pillow. Run from any directory:
    python tools/exercise_bank/build.py
Source scans are retained for reproducible extraction, not loaded at app runtime.
"""
from __future__ import annotations

import hashlib
import io
import json
from pathlib import Path
import shutil
import sqlite3

from PIL import Image
from chapters_05_19 import COUNTS as NEW_COUNTS, transcribe as transcribe_new

ROOT = Path(__file__).resolve().parents[2]
SOURCES = Path(__file__).resolve().parent / 'sources'
OUTPUT = ROOT / 'src/main/resources/database/exercise.db'
ASSETS: dict[str, tuple[bytes, int, int]] = {}
ASSET_SOURCES: dict[str, dict] = {}
EXERCISES: list[dict] = []
CHAPTER_COUNTS = {1: 4, 2: 9, 3: 20, 4: 12, **NEW_COUNTS}


def p(text):
    return {'type': 'paragraph', 'inlines': [{'type': 'text', 'text': text}]}


def code(text):
    return {'type': 'code', 'language': 'text', 'text': text}


def extract(filename, box):
    with Image.open(SOURCES / filename) as source:
        assert 0 <= box[0] < box[2] <= source.width
        assert 0 <= box[1] < box[3] <= source.height
        region = source.crop(box).convert('RGB')
        buf = io.BytesIO()
        region.save(buf, format='PNG')
        data = buf.getvalue()
        digest = hashlib.sha256(data).hexdigest()
        ASSETS[digest] = (data, region.width, region.height)
        ASSET_SOURCES[digest] = {'file': filename, 'box': list(box)}
        return digest


def formula(latex, alt, filename, box):
    return {'type': 'formula', 'latex': latex, 'alt': alt,
            'assetId': extract(filename, box)}


def figure(alt, caption, filename, box):
    return {'type': 'image', 'alt': alt, 'caption': caption,
            'assetId': extract(filename, box)}


def add(chapter, number, title, *blocks, sources, uml=False):
    EXERCISES.append({'chapter': chapter, 'number': number, 'title': title,
                      'blocks': [p(b) if isinstance(b, str) else b for b in blocks],
                      'sources': sources, 'requireUml': uml})


def transcribe():
    c1 = ['Chapter01.png']
    add(1, 1, '输出姓名和年龄', '编写程序，打印输出你的姓名和年龄。', sources=c1)
    add(1, 2, '计算加法表达式', '编写程序，计算 2+3+5+7+11+17+19+23 的结果。', sources=c1)
    add(1, 3, '圆面积和周长', '编写程序，使用以下公式计算并显示半径为 5.5 的圆面积和周长。',
        formula(r'S=\pi r^2', '面积等于 π 乘以半径乘以半径', c1[0], (320, 106, 536, 135)),
        formula(r'C=2\pi r', '周长等于 2 乘以 π 乘以半径', c1[0], (320, 138, 506, 168)), sources=c1)
    add(1, 4, '打印星号三角形', '编写程序，打印输出下面的图形。',
        code('*\n*   *\n*   *   *\n*   *   *   *\n*   *   *   *   *'), sources=c1)

    a, b = 'Chapter02_1.png', 'Chapter02_2.png'
    add(2, 1, '华氏温度转换', '编写程序，从键盘输入一个 double 型的华氏温度，然后将其转换为摄氏温度输出。转换公式为：',
        formula(r'C=(5/9)\times(F-32)', '摄氏度等于 (5/9) 乘以 (华氏度减 32)，C 为摄氏度，F 为华氏度', a, (109, 44, 406, 72)), sources=[a])
    add(2, 2, '两位数逆序输出', '编写程序，从键盘输入一个两位数，按数位逆序输出。提示：使用“%”和“/”运算符可求出每一位数字。', sources=[a])
    add(2, 3, '三位整数各位数字之和', '编写程序，接收用户从键盘输入的一个三位整数，计算并输出各位数字之和。例如，输入整数 932，各位数字之和是 14。', sources=[a])
    add(2, 4, '圆柱体积', '编写程序，从键盘输入圆柱底面半径和高，计算并输出圆柱的体积。', sources=[a])
    add(2, 5, '人民币兑换美元', '设今日外汇牌价为 1 美元兑换 6.89 元人民币。编写程序，计算 10 000 元人民币能兑换多少美元。要求从键盘输入汇率。', sources=[a])
    add(2, 6, '身体质量指数', '编写程序，从键盘输入你的体重（单位：kg）和身高（单位：m），计算你的体重指数（Body Mass Index，BMI）。该值是衡量一个人是否超重的指标。假设 w 表示体重，h 表示身高，则 BMI 的计算公式为：',
        formula(r'\mathrm{BMI}=\frac{w}{h^2}', 'BMI 等于体重 w 除以身高 h 的平方', a, (407, 408, 508, 461)), sources=[a])
    add(2, 7, '浮点数的整数与小数部分', '编写程序，要求用户从键盘输入一个 double 型数，输出该数的整数部分和小数部分。下面是程序的一次运行结果：',
        code('请输入一个浮点数:2.71828\n整数部分:2\n小数部分:0.71828'), sources=[a])
    add(2, 8, '计算根式表达式', '编写程序，要求用户从键盘输入 a、b 和 c 的值，计算下列表达式的值。',
        formula(r'\frac{-b+\sqrt{b^2-4ac}}{2a}', '负 b 加上 b 平方减 4ac 的平方根，再除以 2a', b, (374, 45, 537, 104)), sources=[b])
    add(2, 9, '贷款月支付额', '编写程序，计算贷款的每月支付额。程序要求用户输入贷款的年利率、总金额和年数。程序计算月支付金额和总偿还金额，并将结果显示输出。计算贷款的月支付额公式为：',
        formula(r'\frac{P r}{1-\frac{1}{(1+r)^{12y}}}', '月支付额等于贷款总额乘月利率，除以 1 减去 (1 加月利率) 的 (年数乘 12) 次方的倒数；P 为贷款总额，r 为月利率，y 为年数', b, (340, 174, 567, 262)),
        '提示：可使用 Math.sqrt(double d) 方法计算数的平方根，使用 Math.pow(double a, double b) 方法计算 a 的 b 次方。', sources=[b])

    a, b = 'Chapter03_1.png', 'Chapter03_2.png'
    add(3, 1, '判断奇偶数', '编写程序，要求用户从键盘上输入一个正整数，程序判断该数是奇数还是偶数。', sources=[a])
    add(3, 2, '判断闰年', '编写程序，要求用户从键盘上输入一个年份，输出该年是否闰年。符合下面两个条件之一的年份即为闰年：①能被 4 整除，但不能被 100 整除；②能被 400 整除。下面是程序的一次运行。',
        code('请输入年份:2017\n2017 年不是闰年。'), sources=[a])
    add(3, 3, '四个整数的最大值与最小值', '编写程序，要求用户从键盘输入 4 个整数，找出其中最大值和最小值并打印输出。要求使用尽可能少的 if（或 if-else）语句实现。提示：4 条 if 语句就够了。', sources=[a])
    add(3, 4, '一元二次方程的根', '可以使用下面的公式求一元二次方程 ax²+bx+c=0 的两个根：',
        formula(r'x_1=\frac{-b+\sqrt{b^2-4ac}}{2a},\quad x_2=\frac{-b-\sqrt{b^2-4ac}}{2a}', 'x1 和 x2 分别等于负 b 加、减判别式平方根后除以 2a', a, (209, 302, 664, 361)),
        'b²−4ac 称为一元二次方程的判别式，如果它是正值，那么方程有两个实数根；如果它为 0，方程就只有一个根；如果它是负值，方程无实根。',
        '编写程序，提示用户输入 a、b 和 c 的值，程序根据判别式显示方程的根。如果判别式为负值，显示“方程无实根”。提示：使用 Math.sqrt(n) 方法计算数 n 的平方根。', sources=[a])
    add(3, 5, '月份与季节', '编写程序，要求从键盘输入一个月份数字（值为 1—12），程序输出月所在的季节，2—4 月为春季，5—7 月为夏季，8—10 月为秋季，11、12、1 月为冬季，要求使用多分支的 if-else 结构实现。', sources=[a])
    add(3, 6, '百分制转五级制', '从键盘输入一个百分制的成绩，输出五级制的成绩，如输入 85，输出“良好”，要求使用 switch 结构实现。', sources=[a])
    add(3, 7, '十个整数的最大值与最小值', '编写程序，接收用户从键盘输入 10 个整数，比较并输出其中的最大值和最小值。', sources=[a])
    add(3, 8, '同时被 5 和 6 整除的数', '编写程序，显示 100～1000 所有能被 5 和 6 整除的数，每行显示 10 个。数字之间用一个空格字符隔开。', sources=[a])
    add(3, 9, '含 7 或为 7 倍数的整数', '编写程序，分别使用 while 循环、do-while 循环和 for 循环结构，计算并输出 1～1000 含有 7 或者 7 的倍数的整数之和及个数。下面是部分输出结果。',
        code('……\n994\n997\n总个数 = 374\n总和 = 206191'), sources=[b])
    add(3, 10, '年份与生肖', '编写程序，要求用户从键盘输入一个年份，程序输出该年出生的人的生肖。中国生肖基于 12 年一个周期，每年用一个动物代表：鼠（rat）、牛（ox）、虎（tiger）、兔（rabbit）、龙（dragon）、蛇（snake）、马（horse）、羊（sheep）、猴（monkey）、鸡（rooster）、狗（dog）和猪（pig）。通过 year%12 确定生肖，1900 年属鼠。', sources=[b])
    add(3, 11, '石头剪刀布', '编写程序，模拟“石头、剪刀、布”游戏。程序随机产生一个数，这个数为 2、1 或 0，分别表示石头、剪刀和布。提示用户输入值 2、1 或 0，然后显示一条消息，表明用户和计算机谁赢了游戏。下面是运行示例：',
        code('你出什么?(石头(2)、剪刀(1)、布(0)):2\n计算机出的是:剪刀,你出石头,你赢了。'), sources=[b])
    add(3, 12, '整数各位数字之和', '编写程序，从键盘输入一个整数，计算并输出该数的各位数字之和。例如：',
        code('请输入一个整数:8899123\n各位数字之和为:40'), sources=[b])
    add(3, 13, '十进制转二进制', '编写程序，提示用户输入一个十进制整数，然后显示对应的二进制值。在这个程序中不要使用 Integer.toBinaryString(int) 方法。', sources=[b])
    add(3, 14, '分数级数求和', '编写程序，计算下面的级数之和：',
        formula(r'\frac{1}{3}+\frac{3}{5}+\frac{5}{7}+\frac{7}{9}+\frac{9}{11}+\frac{11}{13}+\cdots+\frac{95}{97}+\frac{97}{99}', '从 1/3、3/5 依次累加到 97/99', b, (251, 690, 634, 744)), sources=[b])
    add(3, 15, '鸡兔同笼', '求解“鸡兔同笼问题”：鸡和兔在一个笼里，共有腿 100 条，头 40 个。问：鸡、兔各有几只？', sources=[b])
    add(3, 16, '水仙花数', '编写程序，求出所有的水仙花数。水仙花数是这样的三位数，它的各位数字的立方和等于这个三位数本身，例如 371=3³+7³+1³，371 就是一个水仙花数。', sources=[b])
    add(3, 17, '最小公倍数与最大公约数', '从键盘输入两个整数，计算这两个数的最小公倍数和最大公约数并输出。', sources=[b])
    add(3, 18, '完全数', '编写程序，求出 1～1000 所有的完全数。完全数是其所有因子（包括 1 但不包括该数本身）的和等于该数。例如 28=1+2+4+7+14，28 就是一个完全数。', sources=[b])
    add(3, 19, '素数因子分解', '编写程序读入一个整数，显示该数的所有素数因子。例如，输入整数为 120，输出应为 2、2、2、3、5。', sources=[b])
    add(3, 20, 'π 的级数近似值', '编写程序，计算当 n=10000、20000、…、100000 时 π 的值。求 π 的近似值公式如下。',
        formula(r'\pi=4\left(1-\frac{1}{3}+\frac{1}{5}-\frac{1}{7}+\frac{1}{9}-\frac{1}{11}+\frac{1}{13}+\cdots+\frac{1}{2n-1}-\frac{1}{2n+1}\right)', 'π 等于 4 乘以括号内的交错分数和：1−1/3+1/5−1/7+1/9−1/11+1/13+…+1/(2n−1)−1/(2n+1)', b, (168, 1068, 719, 1129)), sources=[b])

    a, b, c = 'Chapter04_1.png', 'Chapter04_2.png', 'Chapter04_3.png'
    add(4, 1, 'Person 类', '定义一个名为 Person 的类，其中含有一个 String 类型的成员变量 name 和一个 int 类型的成员变量 age，分别为这两个变量定义访问方法和修改方法，另外再为该类定义一个名为 speak 的方法，在其中输出其 name 和 age 的值。画出该类的 UML 图。编写程序，使用上面定义的 Person 类，实现数据的访问、修改。', sources=[a], uml=True)
    add(4, 2, 'Circle 类', '定义 Circle 类表示圆，其中含有 double 型的成员变量 centerX、centerY 表示圆心坐标，radius 表示圆的半径。定义求圆面积的方法 getArea() 和求圆周长的方法 getPerimeter()。为半径 radius 定义访问方法和修改方法。定义一个带参数构造方法，通过给出圆的半径创建圆对象。定义默认构造方法，在该方法中调用有参数构造方法，将圆的半径设置为 1.0。画出该类的 UML 图。编写程序测试这个圆类的所有方法。', sources=[a], uml=True)
    add(4, 3, 'Rectangle 类', '定义 Rectangle 类表示矩形，其中含有 length、width 两个 double 型的成员变量，表示矩形的长和宽。要求为每个变量定义访问方法和修改方法，定义求矩形周长的方法 getPerimeter() 和求面积的方法 getArea()。定义一个带参数的构造方法，通过给出的长和宽创建矩形对象。定义默认构造方法，在该方法中调用有参数的构造方法，将矩形的长和宽都设置为 1.0。画出该类的 UML 图。编写程序测试这个矩形类的所有方法。', sources=[a], uml=True)
    add(4, 4, 'Triangle 类', '定义 Triangle 类表示三角形，其中包括 3 个 double 型变量 a、b、c，表示三条边长。为该类定义两个构造方法：默认构造方法设置三角形的三条边长都为 0.0；带 3 个参数的构造方法通过传递 3 个参数创建三角形对象。定义求三角形面积的方法 area()，面积计算公式为 area=Math.sqrt(s*(s−a)*(s−b)*(s−c))，其中 s=(a+b+c)/2。编写另一个程序测试这个三角形类的所有方法。', sources=[a])
    add(4, 5, 'BMICalculator 类', '定义 BMICalculator 类用来计算身体质量指数，其中包括 3 个 double 型变量 weight、height、bmi，分别表示体重、身高和 BMI。为该类定义带两个参数的构造方法，通过传递体重和身高创建该类实例。定义 calculate() 方法计算 bmi 值，定义 isOverWeight() 方法返回是否超重（假设 bmi 大于 25 属于超重）。编写程序测试该类的使用。', sources=[a])
    add(4, 6, 'Calculator 类', '定义 Calculator 类表示计算器。该类有两个 double 型成员 a 和 b，为该类定义加（add）、减（minus）、乘（multiply）、除（divide）4 个方法，它们均返回执行两个成员各种运算的结果。Calculator 类的 UML 图如图 4-14 所示。',
        figure('Calculator：私有 a、b 为 double；构造 Calculator() 和 Calculator(a:double,b:double)；setA(a:double):void、setB(b:double):void；add()、minus()、multiply()、divide() 返回 double。', '图 4-14 Calculator 类的 UML 图', b, (253, 3, 650, 334)), sources=[a, b])
    add(4, 7, 'Stock 类', '设计 Stock 的类表示股票，该类包括：',
        '• 一个名为 symbol 的字符串数据域，表示股票代码。',
        '• 一个名为 name 的字符串数据域，表示股票名称。',
        '• 一个名为 previousPrice 的 double 型数据域，它存储股票的前一日收盘价。',
        '• 一个名为 currentPrice 的 double 型数据域，它存储股票的当前价格。',
        '• 创建一个给定特定代码和名称的股票的构造方法。',
        '• 一个名为 getChangePercent() 的方法，返回从前一日价格到当前价格变化的百分比。',
        '画出该类的 UML 图并实现这个类。编写测试程序，创建一个 Stock 对象，它的股票代码是“600000”，股票名称是“浦发银行”，前一日收盘价是 25.50，当前的最新价是 28.6，显示市值变化的百分比。', sources=[a, b], uml=True)
    add(4, 8, 'Time 类', '定义 Time 类表示时间，该类有 hour（小时）、minute（分钟）和 second（秒）3 个数据成员，为该类定义两个构造方法和其他方法，这些方法的 UML 图如图 4-15 所示。提示：类的默认构造方法创建的时间为当前系统时间，请参阅 2.10 节的案例研究。关于时间对象还应满足条件：①3 个数据成员都必须是非负整数；②hour 值应该为 0～23，minute 和 second 值应该为 0～59。编写应用程序测试 Time 类的使用。',
        figure('Time：私有 hour、minute、second 为 int；Time() 返回系统当前时间，Time(hours:int,minutes:int,seconds:int) 按参数构造；getHour()、getMinute()、getSecond() 返回 int；isBefore(Time time)、isAfter(Time time) 返回 boolean；toString() 返回时间字符串，例如 20:12:59。', '图 4-15 Time 类的 UML 图', b, (161, 766, 738, 1103)), sources=[b])
    add(4, 9, '递归求最大公约数', '编写一个 gcd(m,n) 方法，使用递归求 m 和 n 的最大公约数。gcd() 方法可以递归地定义如下：',
        '• 如果 m%n 结果为 0，那么 gcd(m,n) 的结果为 n。',
        '• 否则，gcd(m,n) 就是 gcd(n,m%n)。',
        '编写测试程序，提示用户输入两个整数，显示它们的最大公约数。', sources=[b])
    add(4, 10, 'QuadraticEquation 类', '为一元二次方程 ax²+bx+c=0 设计一个名为 QuadraticEquation 的类。这个类包括：',
        '• 代表 3 个系数的私有数据域 a、b 和 c。',
        '• 一个参数为 a、b 和 c 的构造方法。',
        '• a、b、c 的 3 个 getter 方法。',
        '• 一个名为 getDiscriminant() 的方法返回判别式 b²−4ac。',
        '• 名为 getRoot1() 和 getRoot2() 的方法返回方程的两个根。',
        formula(r'x_1=\frac{-b+\sqrt{b^2-4ac}}{2a},\quad x_2=\frac{-b-\sqrt{b^2-4ac}}{2a}', 'x1 和 x2 分别等于负 b 加、减判别式平方根后除以 2a', c, (267, 374, 697, 437)),
        '这些方法只有在判别式为非负数时才有用，如果判别式为负，这些方法返回 0。',
        '画出该类的 UML 图并实现这个类。编写一个测试程序，提示用户输入 a、b 和 c 的值，然后显示判别式的结果。如果判别式为正数，显示两个根；如果判别式为 0，显示一个根；否则显示“方程无根”。', sources=[c], uml=True)
    add(4, 11, '回文素数', '回文素数是指一个数同时为素数和回文数。例如，131 是一个素数，同时也是一个回文数，757 也是回文素数。编写程序，显示前 20 个回文素数。每行显示 10 个数，数字之间用空格隔开，如下所示。',
        code('2   3   5   7   11   101   131   151   181   191\n313 353 373 383 727 757 787 797 919 929'), sources=[c])
    add(4, 12, 'Account 类', '定义一个名为 Account 的类实现账户管理，它的 UML 图如图 4-16 所示。编写一个应用程序测试 Account 的使用。',
        figure('Account：私有 id:int、balance:double、annulRate:double、dateCreated:LocalDate；Account()、Account(id:int,balance:double)；getId()、setId(int id)、getBalance()、setBalance(double balance)、getAnnualRate()、setAnnualRate(double annualRate)、getDateCreated()、getMonthlyInterestRate()、withdraw(amount:double)、deposit(amount:double)。annulRate 是原图字段拼写。', '图 4-16 Account 类的 UML 图', c, (239, 804, 734, 1290)), sources=[c])


def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values():
            yield from walk(child)
    elif isinstance(value, list):
        for child in value:
            yield from walk(child)


def plain_text(document):
    return '\n'.join(str(node[key]) for node in walk(document)
                     for key in ('text', 'alt', 'caption', 'latex') if key in node)


def validate(db):
    assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
    assert not db.execute('PRAGMA foreign_key_check').fetchall()
    assert db.execute('SELECT chapter_id, count(*) FROM exercise GROUP BY chapter_id').fetchall() == list(CHAPTER_COUNTS.items())
    assert db.execute('SELECT id,chapter_no,title,sort_order FROM chapter ORDER BY id').fetchall() == [
        (i, i, f'第{i}章', i) for i in CHAPTER_COUNTS]
    for chapter, count in CHAPTER_COUNTS.items():
        rows = db.execute('SELECT exercise_no, sort_order FROM exercise WHERE chapter_id=? ORDER BY sort_order', (chapter,)).fetchall()
        assert rows == [(str(n), n) for n in range(1, count + 1)]
    for asset_id, mime, blob, digest, size, width, height in db.execute('SELECT * FROM asset'):
        assert asset_id == digest == hashlib.sha256(blob).hexdigest()
        assert size == len(blob) and size <= 10 * 1024 * 1024
        with Image.open(io.BytesIO(blob)) as img:
            assert mime == 'image/png' and img.format == 'PNG'
            assert img.size == (width, height)
            img.verify()
    for eid, raw, text in db.execute('SELECT id,content,content_text FROM exercise'):
        doc = json.loads(raw)
        assert doc['schemaVersion'] == 1 and doc['blocks']
        assert text == plain_text(doc)
        references = {n['assetId'] for n in walk(doc) if 'assetId' in n}
        links = {r[0] for r in db.execute('SELECT asset_id FROM exercise_asset WHERE exercise_id=?', (eid,))}
        assert references == links
        for node in walk(doc):
            if 'type' in node:
                assert node['type'] in {'paragraph', 'text', 'code', 'image', 'formula', 'table'}
            if node.get('type') == 'formula':
                assert node['latex'] and node['alt'] and node['assetId']
            if node.get('type') == 'image':
                assert node['alt'] and node['assetId']
            if node.get('type') == 'table':
                assert node['headers'] and node['rows']
                assert all(len(row) == len(node['headers']) for row in node['rows'])
    assert not db.execute('SELECT id FROM asset WHERE id NOT IN (SELECT asset_id FROM exercise_asset)').fetchall()
    metadata = dict(db.execute('SELECT key,value FROM metadata'))
    source_files = json.loads(metadata['source_files'])
    exercise_sources = json.loads(metadata['exercise_sources'])
    asset_sources = json.loads(metadata['asset_sources'])
    assert set(exercise_sources) == {str(r[0]) for r in db.execute('SELECT id FROM exercise')}
    assert set(source_files) == {f for files in exercise_sources.values() for f in files}
    for filename, digest in source_files.items():
        assert hashlib.sha256((SOURCES / filename).read_bytes()).hexdigest() == digest
    assert set(asset_sources) == {r[0] for r in db.execute('SELECT id FROM asset')}
    for eid, aid in db.execute('SELECT exercise_id,asset_id FROM exercise_asset'):
        assert asset_sources[aid]['file'] in exercise_sources[str(eid)]
    assert not db.execute('SELECT id FROM exercise WHERE id != chapter_id*1000+sort_order OR exercise_type != ? OR require_result != 1 OR allow_uml != 1', ('PROGRAMMING',)).fetchall()
    assert json.loads(metadata['uml_required_exercise_ids']) == [4001, 4002, 4003, 4007, 4010, 7004]


def main():
    transcribe()
    transcribe_new(add, code, formula, figure)
    schema = (SOURCES.parent / 'schema.sql').read_text(encoding='utf-8')
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    # Build separately; a failed validation leaves the existing bank untouched.
    temporary = OUTPUT.with_suffix('.building')
    if temporary.exists():
        raise FileExistsError(f'Remove or inspect stale build file first: {temporary}')
    db = sqlite3.connect(temporary)
    try:
        db.executescript(schema)
        with db:
            # Chapter names are absent from the supplied scans, so do not invent them.
            db.executemany('INSERT INTO chapter VALUES (?, ?, ?, ?)', [(i, i, f'第{i}章', i) for i in CHAPTER_COUNTS])
            for digest, (data, width, height) in sorted(ASSETS.items()):
                db.execute('INSERT INTO asset VALUES (?, ?, ?, ?, ?, ?, ?)',
                           (digest, 'image/png', data, digest, len(data), width, height))
            for ex in EXERCISES:
                eid = ex['chapter'] * 1000 + ex['number']
                doc = {'schemaVersion': 1, 'blocks': ex['blocks']}
                db.execute('INSERT INTO exercise VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                           (eid, ex['chapter'], str(ex['number']), ex['title'], json.dumps(doc, ensure_ascii=False),
                            plain_text(doc), 'PROGRAMMING', 1, 1, ex['number']))
                for asset_id in sorted({node['assetId'] for node in walk(doc) if 'assetId' in node}):
                    db.execute('INSERT INTO exercise_asset VALUES (?, ?)', (eid, asset_id))
            metadata = {
                'schema_version': '1', 'content_version': '2026.10.03.1',
                'course': 'Java 面向对象程序设计',
                'source_description': '用户提供的第1～19章习题截图；人工转录，保留原题语义及示例。',
                'exercise_type_values': json.dumps(['PROGRAMMING']),
                'source_files': json.dumps({f.name: hashlib.sha256(f.read_bytes()).hexdigest() for f in sorted(SOURCES.glob('*.png'))}, ensure_ascii=False),
                'exercise_sources': json.dumps({str(e['chapter'] * 1000 + e['number']): e['sources'] for e in EXERCISES}, ensure_ascii=False),
                'asset_sources': json.dumps(ASSET_SOURCES, ensure_ascii=False),
                'uml_required_exercise_ids': json.dumps([e['chapter'] * 1000 + e['number'] for e in EXERCISES if e['requireUml']]),
                'chapter_exercise_counts': json.dumps(CHAPTER_COUNTS),
                'source_limitations': json.dumps({
                    '7002': '题目引用教材正文图7-2，所给截图未包含该图，未推测各类继承关系。',
                    '7003': 'Chapter07_3 第（2）项右边缘略有截断；play() 方法名按同题 UML 图和上下文还原。',
                    '7006': '题目引用 Shape 抽象类，截图未附该类定义。',
                    '9009': '题目要求修改 Employee，截图未附原类定义。',
                    '9010': '题目要求修改 Circle，截图未附原类定义。',
                    '9012': '题目引用 Student，截图未附原类定义。',
                    '12002': '题目引用教材程序8.6 IntStack，截图未附原程序。',
                    '15008': '题目要求 images/card/1.png～52.png，截图未附52张纸牌资源。',
                    '17001': '题目引用 webstore 数据库 books 表，截图未附建表语句或数据。',
                    '17004': '题目引用 products 表，截图仅有界面示例，未附建表语句。',
                    '18001': '题目引用教材程序18.1，截图未附原程序。',
                    '18003': '题目引用教材程序18.6 Counter，截图未附原程序。',
                }, ensure_ascii=False),
                'transcription_notes': '章节原名未提供，使用第N章；题目短标题为整理标签。原题未给出的评分区间等条件未补写。Account 图中 annulRate、第11章第5题 unsigned、第11章第6题 ZEGNG、原题伪代码均按图保留。第8章第9题年份范围、第15章第7题正文5个节点与图中4个Hello的差异均保留。UML 必做要求保留在题干和元数据中，allow_uml 仅表示允许提交。外部教材引用及截图裁切限制见 source_limitations。',
            }
            db.executemany('INSERT INTO metadata VALUES (?, ?)', metadata.items())
        validate(db)
    finally:
        db.close()
    if OUTPUT.exists():
        digest = hashlib.sha256(OUTPUT.read_bytes()).hexdigest()
        backups = SOURCES.parent / 'backups'
        backups.mkdir(exist_ok=True)
        backup = backups / f'exercise-{digest}.db'
        if not backup.exists():
            shutil.copy2(OUTPUT, backup)
        assert hashlib.sha256(backup.read_bytes()).hexdigest() == digest
    temporary.replace(OUTPUT)
    print(f'Validated {len(EXERCISES)} exercises, {len(CHAPTER_COUNTS)} chapters, {len(ASSETS)} assets: {OUTPUT}')


if __name__ == '__main__':
    main()
