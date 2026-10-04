"""Reviewed transcriptions of the supplied chapter 5–19 scans; no solutions."""


COUNTS = {5: 15, 6: 9, 7: 7, 8: 9, 9: 14, 10: 6, 11: 6, 12: 10,
          13: 12, 14: 6, 15: 8, 16: 9, 17: 4, 18: 7, 19: 6}


def transcribe(add, code, formula, figure):
    a, b = 'Chapter05_1.png', 'Chapter05_2.png'
    add(5, 1, '数组统计', '给定数组 a 的声明如下，编写程序，计算所有元素的和、最大值、最小值及平均值。',
        code('double[] a = {75, 53, 32, 12, 46, 199, 17, 54};'), sources=[a])
    add(5, 2, '数组最小元素', '编写一个方法，求出一个 double 型数组中的最小元素，方法的声明格式如下：',
        code('public static double min(double[] array)'),
        '编写测试程序，提示用户从键盘输入 5 个 double 型数，并存放到一个数组中，然后调用这个方法返回最小值。', sources=[a])
    add(5, 3, 'Fibonacci 数列', '编程打印输出 Fibonacci 数列的前 20 个数。Fibonacci 数列是第一个和第二个数都是 1，以后每个数是前两个数之和，用公式表示为：',
        formula(r'f_1=f_2=1,\ f_n=f_{n-1}+f_{n-2}\ (n\geq3)', 'f1=f2=1，fn=f(n−1)+f(n−2)，n≥3', a, (580, 375, 943, 409)),
        '要求使用数组存储 Fibonacci 数。', sources=[a])
    add(5, 4, '数组元素逆序', '编写程序，定义一个有 10 个元素的整型数组，然后将其前 5 个元素与后 5 个元素对换，即：第 1 个元素与第 10 个元素互换，第 2 个元素与第 9 个元素互换，……，第 5 个元素与第 6 个元素互换。分别输出数组原来各元素的值和互换后各元素的值。', sources=[a])
    add(5, 5, '选择排序', '编写程序，定义一个有 8 个元素的整型数组，然后使用选择排序法对数组按升序排序。选择排序法先找到数列中最小的数，然后将它和第一个元素交换。接下来，在剩下的数中找到最小数，将它和第二个元素交换，以此类推，直到数列中仅剩一个数为止。', sources=[a])
    add(5, 6, '两个数组之和', '编写一个方法，计算给定的两个数组之和，格式如下：',
        code('public static int[] sumArray(int[] a, int[] b)'),
        '要求返回的数组元素是两个参数数组对应元素之和，不对应的元素直接赋给相应的位置，如 {1,2,4}+{2,4,6,8}={3,6,10,8}。', sources=[a])
    add(5, 7, '合并并排序数组', '编写一个方法，合并给定的两个数组，并以升序返回合并后的数组，格式如下：',
        code('public static int[] arrayMerge(int[] a, int[] b)'),
        '例如，一个数组是 {16,13,15,18}，另一个数组是 {29,36,100,9}，返回的数组应该是 {9,13,15,16,18,29,36,100}。', sources=[a])
    add(5, 8, '数组保存方程的根', '编写程序，使用下面的方法头编写一个解一元二次方程式的方法：',
        code('public static int solveQuadratic(double[] eqn, double[] roots)'),
        '一元二次方程式 ax²+bx+c=0 的系数都传给数组 eqn，然后将两个非复数的根存在 roots 中。方法返回根的个数。', sources=[a])
    add(5, 9, '筛选法求素数', '编写程序，使用筛选法求出 2～100 中的所有素数。筛选法是在 2～100 的数中先去掉 2 的倍数，再去掉 3 的倍数，以此类推，最后剩下的数就是素数。注意：2 是最小的素数，不能去掉。', sources=[a])
    add(5, 10, '判断数组相同', '如果两个数组 list1 和 list2 的长度相同，而且对于每个 i，list1[i] 都等于 list2[i]，那么认为 list1 和 list2 是完全相同的。使用下面的方法头编写一个方法，如果 list1 和 list2 完全相同，那么这个方法返回 true。',
        code('public static boolean equals(int[] list1, int[] list2)'), sources=[a])
    add(5, 11, '约瑟夫问题', '编程求解约瑟夫（Josephus）问题：有 12 个人排成一圈，从 1 号开始报数，凡是数到 5 的人就离开，然后继续报数。试问：最后剩下的一人是谁？', sources=[a, b])
    add(5, 12, '四张牌点数之和', '编写程序，从一副 52 张的牌中选出 4 张，然后计算它们的和，程序应该显示得到和为 24 的选牌次数。A、J、Q 和 K 分别表示 1、11、12 和 13。', sources=[b])
    add(5, 13, '矩阵运算', '有下面两个矩阵 A 和 B：',
        formula(r'A=\begin{bmatrix}1&3&5\\-3&6&0\\13&-5&7\\-2&19&25\end{bmatrix},\quad B=\begin{bmatrix}0&-1&-2\\7&-1&6\\-6&13&2\\12&-8&-13\end{bmatrix}',
                '矩阵 A 各行为 (1,3,5)、(−3,6,0)、(13,−5,7)、(−2,19,25)；矩阵 B 各行为 (0,−1,−2)、(7,−1,6)、(−6,13,2)、(12,−8,−13)。', b, (255, 157, 725, 295)),
        '编写程序，计算：（1）A+B；（2）A−B；（3）矩阵 A 的转置。', sources=[b])
    add(5, 14, '二维数组最大元素的位置', '编写下面的方法，返回二维数组中最大元素的位置。',
        code('public static int[] locateLargest(double[][] a)'),
        '返回值是包含两个元素的一维数组。这两个元素表示二维数组中最大元素的行下标和列下标。编写一个测试程序，提示用户输入一个二维数组，然后显示这个数组中最大元素的位置。',
        code('请输入数组的行数和列数: 3 4\n请输入每行元素的值:\n23.5  35    2    10\n 4.5   3   45   3.5\n  35  44  5.5   9.6\n最大元素的位置是(1,2).'), sources=[b])
    add(5, 15, '矩阵顺时针旋转', '编写程序，从键盘输入矩阵阶数 n，给 n 阶矩阵的元素按行序从 1～n×n 的顺序赋值，然后将矩阵按顺时针旋转 90°，输出旋转后的矩阵。运行结果如下：',
        code('请输入矩阵的阶数:4\n13   9   5   1\n14  10   6   2\n15  11   7   3\n16  12   8   4'), sources=[b])

    a = 'Chapter06.png'
    add(6, 1, '字符串长度及首尾字符', '编写程序，提示用户输入一个字符串，要求该字符串包括中文、大小写英文、数字和一个特殊字符。输出显示它的长度、第一个字符和最后一个字符。', sources=[a])
    add(6, 2, '检测子串', '编写程序，提示用户输入两个字符串，检测第二个字符串是否是第一个字符串的子串。', sources=[a])
    add(6, 3, '检查 ISBN', '国际标准书号（ISBN）由 13 位数字组成，分为 5 段。例如，978-7-111-50690-4 是一个合法的书号。编写程序，提示用户输入一个书号，检查该书号是否合法。', sources=[a])
    add(6, 4, '统计字母个数', '使用下面的方法签名编写一个方法，统计一个字符串所包含字母的个数。编写测试程序调用 countLetters("Beijing 2022") 方法并显示它的返回值 7。',
        code('public static int countLetters(String s)'), sources=[a])
    add(6, 5, '十进制转二进制字符串', '编写一个方法，将十进制数转换为二进制数的字符串，方法签名如下。',
        code('public static String toBinary(int value)'), sources=[a])
    add(6, 6, '字符串排序', '使用下列方法签名编写一个方法，返回排好序的字符串。例如，调用 sort("morning") 方法，应返回 "gimnnor"。',
        code('public static String sort(String s)'), sources=[a])
    add(6, 7, '解析单词数组', '编写程序，将字符串“no pains,no gains.”解析成含有 4 个单词的字符串数组。', sources=[a])
    add(6, 8, '命令行整数最值', '编写程序，从命令行输入 3 个整数，输出其中的最大值和最小值。', sources=[a])
    add(6, 9, '命令行城市排序', '编写程序，从命令行输入 3 个城市的名字，比较城市名字字符串，按从小到大的顺序输出。', sources=[a])

    a, b, c = 'Chapter07_1.png', 'Chapter07_2.png', 'Chapter07_3.png'
    add(7, 1, 'Input 输入工具类', '编写一个名为 com.boda.utils.Input 的类，使用该类实现各种（字符型除外）数据输入，其中的方法有 readInt()、readDouble()、readString() 等。在用户程序中通过调用 Input.readDouble() 即可从键盘上输入 double 型数据。例如，下面程序可以读入一个 double 型数据：',
        code('''import com.boda.utils.Input;
public class Test{
    public static void main(String[] args){
        System.out.print("请输入一个浮点数:");
        double d = Input.readDouble();
        System.out.println("d = " + d);
    }
}'''), sources=[a, b])
    add(7, 2, '访问权限验证', '给定本章图 7-2 所示的两个包 com.boda.xy 和 org.demo.ab 的结构，其中 A、B、C 类属于 com.boda.xy 包，D 和 E 类属于 org.demo.ab 包，箭头表示继承关系。根据图中给出的包和类的定义。假设 A 类的定义如下，编写代码验证 A 类中的 4 个变量和 4 个方法在其他类中的可见性。',
        code('''package com.boda.xy;
public class A{
    private int a = 10;
    int b = 20;
    protected int c = 30;
    public int d = 40;

    private void methodA(){
        System.out.println("方法 A.");
    }
    void methodB(){
        System.out.println("方法 B.");
    }
    protected void methodC(){
        System.out.println("方法 C.");
    }
    public void methodD(){
        System.out.println("方法 D.");
    }
}'''), '代码图注：4 个具有不同访问权限的变量；4 个具有不同访问权限的方法。', sources=[b])
    add(7, 3, 'Player 继承与多态', '给定如图 7-9 所示的 Player 抽象类及其子类 MusicPlayer 和 VideoPlayer 的继承关系 UML 图。其中，Player 表示播放器，fileName 表示播放文件，play() 和 stop() 表示播放和停止方法，MusicPlayer 表示音频播放器，volume 表示音量，encodeAudio() 表示音频解码方法，VideoPlayer 表示视频播放器，duration 表示视频持续时间，encodeVideo() 表示视频解码方法。',
        figure('Player：私有 fileName:String，公有 play():void、stop():void；子类 MusicPlayer：公有 volume:int、encodeAudio():void；子类 VideoPlayer：公有 duration:int、encodeVideo():void。', '图 7-9 Player 类及子类继承关系', b, (225, 998, 704, 1286)),
        '（1）编写代码实现这些类，为这些类定义无参数构造方法，在构造方法中输出一句话，实现每个类中定义的方法。',
        '（2）在 main() 方法中创建一个 MusicPlayer 对象，访问该对象的 volume 成员、play()、encodeAudio() 和 stop() 方法。访问 toString() 方法，该方法是在哪里定义的？',
        '（3）在 main() 方法中编写下面代码，该段代码是否有编译错误，为什么？',
        code('''Player player;
MusicPlayer mplayer = new MusicPlayer();
mplayer.play();
player = mplayer;
player.encodeAudio();
MusicPlayer player2 = (MusicPlayer)player;
player2.encodeAudio();'''), sources=[b, c])
    add(7, 4, 'Cylinder 类', '定义一个名为 Cylinder 类表示圆柱，它继承 Circle 类（参见编程练习 4.2），要求定义一个变量 height 表示圆柱高度。覆盖 getArea() 方法求圆柱的表面积，定义 getVolume() 方法求圆柱体积。定义默认构造方法和带 radius 和 height 两个参数的构造方法。',
        '画出 Circle 类和 Cylinder 类的 UML 图，并实现这些类。编写测试程序，提示用户输入圆柱的底面圆的半径和高度，程序创建一个圆柱对象，计算并输出圆柱表面积和体积。', sources=[c], uml=True)
    add(7, 5, 'Vehicle 与 Bus 类', '设计一个车类 Vehicle。其中包含一个表示速度的 double 型的成员变量 speed 和表示启动的 start() 方法、表示加速的 speedUp() 方法以及表示停止的 stop() 方法。再设计一个 Vehicle 类的子类 Bus 表示公共汽车，在 Bus 类中定义一个 int 型的表示乘客数的成员变量 passenger，另外定义两个方法 gotOn() 和 gotOff() 表示乘客上车和下车。编写程序测试 Bus 类的使用。', sources=[c])
    add(7, 6, 'Square 类', '定义一个名为 Square 的类表示正方形，使其继承 Shape 抽象类，覆盖 Shape 类中的抽象方法 getPerimeter() 和 getArea()。编写程序测试 Square 类的使用。', sources=[c])
    add(7, 7, 'Cuboid 类', '定义一个名为 Cuboid 的长方体类，使其继承 Rectangle 类，其中包含一个表示高的 double 型成员变量 height。定义一个构造方法 Cuboid(double length,double width,double height)。定义一个求长方体体积的 volume() 方法。编写程序，求一个长、宽和高分别为 10、5、2 的长方体的体积。', sources=[c])

    a = 'Chapter08.png'
    add(8, 1, 'Point 类的 Object 方法', '定义一个名为 Point 类表示点，构造方法 public Point(double x,double y)，访问方法有 getX() 和 getY()。（1）覆盖从超类继承的 toString() 方法，调用该方法返回类名及点的 x 和 y 坐标值。（2）覆盖 equals() 方法，当两个点的 x 和 y 坐标分别相等，方法返回 true，否则返回 false。（3）覆盖 hashCode() 方法，要求使用 Objects 类的 hash() 方法计算哈希码。（4）编写 main() 方法，测试上述几个方法的使用。', sources=[a])
    add(8, 2, 'Product 类的克隆与比较', '定义一个名为 Product 类表示商品信息，它的 UML 图如图 8-3 所示。',
        figure('Product：私有 id:Integer、name:String、brand:String、price:double；公有 Product()、Product(id:Integer,name:String,brand:String,price:double)、equals(Object obj):boolean、hashCode():int、toString():String。字段分别为商品号、商品名、品牌、价格。', '图 8-3 Product 类的 UML 图', a, (480, 278, 897, 548)),
        '要求为每个字段定义访问方法（getter）和修改方法（setter）并保证对象能够调用 clone() 方法进行克隆。覆盖父类的 equals() 方法和 hashCode() 方法，为保证 hashCode() 方法和 equals() 方法的兼容，请使用 Objects 类的 hash() 方法和 equals() 方法。覆盖父类的 toString() 方法，要求当调用该方法时输出 Product 各属性信息。',
        '编写程序测试 equals()、hashCode()、clone() 和 toString() 方法的使用。', sources=[a])
    add(8, 3, '随机数频次', '编写程序，随机产生 600 个 1～6 的整数，统计每个数出现的次数。修改程序，使之产生 6000 个 1～6 的随机数，并统计每个数出现的次数。比较不同的结果并给出结论。', sources=[a])
    add(8, 4, '数值包装类的最值', '编写程序，输出 6 种数值型包装类的最大值和最小值。', sources=[a])
    add(8, 5, '程序员日', '程序员日是每年的第 256 天，编写程序计算 2024 年的程序员日是哪一天？', sources=[a])
    add(8, 6, '出生至今的天数', '编写程序，从键盘输入出生日期（格式为 yyyy-MM-dd），计算并输出从出生到现在已经过去多少天？', sources=[a])
    add(8, 7, '输出月历', '编写程序，提示用户输入一个年份和一个月份（如 2024 2），程序在控制台输出 2024 年 2 月的月历，程序运行结果如下：',
        code('输入一个年份和月份(如 2024 2):2024 2\n2024 年     二月\n-----------------------\n 一  二  三  四  五  六  日\n             1   2   3   4\n 5   6   7   8   9  10  11\n12  13  14  15  16  17  18\n19  20  21  22  23  24  25\n26  27  28  29'), sources=[a])
    add(8, 8, '出生日期与星座', '编写程序，要求从键盘输入一个出生日期（要求公历），输出该出生日期所属的星座。', sources=[a])
    add(8, 9, '星期五与 13 号', '编写程序，打印出 21 世纪所有日期为 13 号并且是星期五的日期。提示：测试从 2000.1.1 到 2099.12.31 每月 13 日是不是星期五共有 172 天。', sources=[a])

    transcribe_09_14(add, code, formula, figure)
    transcribe_15_19(add, code, formula, figure)


def transcribe_09_14(add, code, formula, figure):
    a, b, c = 'Chapter09_1.png', 'Chapter09_2.png', 'Chapter09_3.png'
    add(9, 1, 'Swimmable 与 Flyable 接口', '设计一个名为 Swimmable 的接口，其中包含 void swim() 方法，设计另一个名为 Flyable 的接口，其中包含 void fly() 方法。定义一个 Duck 类实现上述两个接口。定义测试类，演示接口类型的使用。', sources=[a])
    add(9, 2, '随机整数序列接口', '设计一个名为 IntSequence 的接口表示整数序列，该接口包含 boolean hasNext() 和 int next() 两个方法。定义一个名为 RandomIntSequence 的类实现 IntSequence 接口，其中包含一个 private 整型变量 n。在 hasNext() 方法中随机生成一个两位整数，存储到变量 n 中，然后返回 true。在 next() 方法中返回 n 的值。', sources=[a])
    add(9, 3, '序列平均值', '设计一个名为 SequenceTest 的类，在其中编写一个 static 方法用于计算一个整数序列前 n 个整数的平均值，方法签名如下：',
        code('public static double average(IntSequence seq, int n)'),
        '在 main() 方法中编写代码通过 RandomIntSequence 的方法获得前 10 个随机整数，并计算它们的平均值。', sources=[a, b])
    add(9, 4, '接口静态方法的继承', '编写程序，证明下面叙述：接口的静态方法不能被子接口继承，也不被实现类继承。', sources=[b])
    add(9, 5, 'Outer 内部类输出', '编译和执行下面的 Outer 类，输出结果如何？',
        code('''public class Outer {
    protected Inner ic;
    public Outer() {
        ic = new Inner();
    }
    public void displayStrings() {
        System.out.println(ic.getString() + ".");
        System.out.println(ic.getAnotherString() + ".");
    }
    // 内部类定义
    protected class Inner {
        public String getString() {
            return "Inner: getString invoked";
        }
        public String getAnotherString() {
            return "Inner: getAnotherString invoked";
        }
    }
    public static void main(String[] args) {
        Outer mc = new Outer();
        mc.displayStrings();
    }
}'''), sources=[b])
    add(9, 6, '返回内部类对象', '编写一个名为 Outer 的类，它包含一个名为 Inner 的类。在 Outer 中添加一个方法，它返回一个 Inner 类型的对象。在 main() 方法中，创建并初始化一个指向某个 Inner 对象的引用。', sources=[b])
    add(9, 7, '局部内部类的访问', '编写程序，证明下面的叙述：局部内部类可以访问外层类的成员，若要访问其所在方法的参数和局部变量，这些参数和局部变量隐含使用了 final 修饰符。', sources=[b])
    add(9, 8, '内部类访问私有成员', '定义一个类，类中包含私有数据成员和私有方法。在这个类中定义一个内部类，内部类中定义一个方法修改外部类的数据成员值，并调用外部类的私有方法。在外部类的公共静态方法中创建内部类对象，并调用内部类的方法。', sources=[b])
    add(9, 9, 'Employee 按年龄比较', '编写程序，修改 Employee 的定义，使它能够根据员工的年龄（age 字段值）进行比较，年龄大的员工排在前面。', sources=[b])
    add(9, 10, 'Circle 按面积比较', '编写程序，修改 Circle 的定义，使它能够按圆的面积大小比较，要求 compareTo() 方法返回两个圆的面积差（整数）。', sources=[b])
    add(9, 11, 'Position 比较距离', '设计一个 Position 类，该类有 x 和 y 两个成员变量表示坐标。要求该类实现 Comparable<T> 接口的 compareTo() 方法，实现比较两个 Position 对象到原点 (0,0) 的距离之差。', sources=[b])
    add(9, 12, 'Student 按姓名排序', '编写一个类实现 java.util.Comparator<T> 接口，使用该类对象实现 Student 对象按姓名排序。', sources=[b])
    add(9, 13, '字符串降序比较器', '编写一个类实现 java.util.Comparator<T> 接口，实现字符串按降序排序。编写测试类，使用 Arrays 类的带两个参数的 sort() 方法对一个 String 数组进行降序排序。', sources=[b])
    add(9, 14, '类和接口层次图', '有如图 9-4 所示的接口和类的层次关系图，请编写代码实现这些接口和类。',
        figure('Flyable 接口定义 takeoff():void、land():void、fly():void。AirPlane 继承 Vehicle 并实现 Flyable；Bird 和 Superman 继承 Animal 并实现 Flyable。Animal 有 eat():void。AirPlane 有 takeoff()、land()、fly()；Bird 另有 buildNest()、layEggs()、eat()；Superman 另有 leapBuilding()、stopBullet()、eat()。', '图 9-4 类和接口层次图', c, (188, 61, 680, 420)), sources=[c])

    a = 'Chapter10.png'
    add(10, 1, '圆面积的输入异常', '编写程序，要求从键盘输入一个 double 型的圆的半径，计算并输出其面积。测试当输入的数据不是 double 型数据（如字符串 "abc"）会抛出什么异常？试用异常处理方法修改程序。', sources=[a])
    add(10, 2, '整数输入重试', '编写程序，提示用户读取两个整数，然后显示它们的和。程序应该在输入不正确时提示用户再次读取数字。', sources=[a])
    add(10, 3, '数组下标越界', '编写程序，首先创建一个由 100 个随机选取的整数构成的数组，然后提示用户输入数组的下标，程序显示对应的元素值。如果指定的下标越界，则显示消息“下标越界”。', sources=[a])
    add(10, 4, 'try、catch 与 finally', '编写程序，在 main() 方法中使用 try 块抛出一个 Exception 类的对象，为 Exception 的构造方法提供一个字符串参数。在 catch 块内捕获该异常并打印出字符串参数。添加一个 finally 块并打印一条消息。', sources=[a])
    add(10, 5, 'IOException 的传播', '编写程序，定义一个 static 方法 methodA()，令其声明抛出一个 IOException 异常，再定义另一个 static 方法 methodB()，在该方法中调用 methodA() 方法，在 main() 方法中调用 methodB() 方法。试编译该类，看编译器会报告什么？对于这种情况应如何处理？由此可得到什么结论？', sources=[a])
    add(10, 6, '自定义异常', '创建一个自定义的异常类，该类继承 Exception 类，为该类写一个构造方法，该构造方法带一个 String 类型的参数。写一个方法，令其打印出保存下来的 String 对象。再编写一个类，在 main() 方法中使用 try…catch 结构创建一个 MyException 类的对象并抛出，在 catch 块中捕获该异常并打印出传递的 String 消息。', sources=[a])

    a, b = 'Chapter11_1.png', 'Chapter11_2.png'
    add(11, 1, 'Book 记录类型', '定义一个名为 Book 的记录类型，要求实现 Serializable 接口和 Comparable 接口，Book 记录包含下面 5 个字段。',
        code('id int, name String, author String, price double, press String'),
        '编写程序，创建两个 Book 记录类型对象，输出记录的字段值，比较两个记录的大小。', sources=[a])
    add(11, 2, 'Product 记录类型', '定义一个名为 Product 记录类型表示商品信息，其 UML 图如图 11-1 所示。',
        figure('Product：id:Integer（商品号）、name:String（商品名）、price:double（价格）；Product(id:Integer)、equals(Object obj):boolean、hashCode():int、toString():String。', '图 11-1 Product 记录类型的 UML 图', a, (268, 285, 705, 499)),
        '要求为该记录类型定义一个仅带 id 参数的构造方法，覆盖父类的 equals() 方法和 hashCode() 方法。为保证 hashCode() 方法和 equals() 方法兼容，使用 Objects 类的 hash() 方法和 equals() 方法。覆盖父类的 toString() 方法，要求当调用该方法时输出 Product 各属性信息。编写程序测试 Product 记录类型所有方法的使用。', sources=[a, b])
    add(11, 3, 'TrafficLight 枚举', '定义一个名为 TrafficLight 的 enum 类型，它包含 3 个常量：GREEN、RED 和 YELLOW，分别表示交通灯的 3 种颜色。通过 values() 方法和 ordinal() 方法循环并打印每一个值及其顺序值。编写一个 switch 语句，为 TrafficLight 的每个常量输出有关信息。', sources=[b])
    add(11, 4, '纸牌枚举类型', '一副纸牌有 52 张，每张牌有两个不同属性：花色和等级。定义两个枚举类型 Suit 和 Rank 分别表示花色和等级。Suit 的枚举值包括 DIAMONDS、CLUBS、HEARTS 和 SPADES。Rank 的枚举值包括 DEUCE、THREE、FOUR、FIVE、SIX、SEVEN、EIGHT、NINE、TEN、JACK、QUEEN、KING 和 ACE。定义 Card 类表示一张牌，它包含两个 private 属性：Suit 和 Rank，一个带两个参数的构造方法，以及 getSuit()、getRank() 和 toString() 方法。下面的 Deck 类表示一副纸牌，使用了 Suit 和 Rank 枚举以及 Card 类。',
        code('''import java.util.*;
public class Deck {
    private static Card[] cards = new Card[52];
    public Deck() {
        int i = 0;
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards[i++] = new Card(rank, suit);
            }
        }
    }
}'''), sources=[b])
    add(11, 5, 'Enhancement 注解', '定义一个名为 Enhancement 的注解类型，包含 id、synopsis、engineer 和 date 4 个元素，为 engineer 和 date 分别指定默认值 unsigned 和 unknown。', sources=[b])
    add(11, 6, 'Author 注解', '下面代码为 Book 类添加了 Author 注解，编写程序定义 Author 注解类型。',
        code('''@Author(
    firstName = "ZEGNG",
    lastName = "SHEN",
    age = 45
)
public class Book{
    //
}'''), sources=[b])

    a, b = 'Chapter12_1.png', 'Chapter12_2.png'
    add(12, 1, '泛型 Point 类', '定义一个泛型类 Point<T>，其中包含 x 和 y 两个类型为 T 的成员，定义带两个参数的构造方法，为 x 和 y 定义 setter 和 getter，另外定义 translate() 方法将点移到新的坐标。编写 main() 方法，创建 Point<Integer> 对象和 Point<Double> 对象。', sources=[a])
    add(12, 2, '泛型栈 GenStack', '编写程序，修改程序 8.6 的 IntStack 类，定义一个泛型类 GenStack，使它可以用来创建任何类型元素的栈。编写应用程序测试 GenStack 类的使用。', sources=[a])
    add(12, 3, '泛型 Cage 与通配符', '定义一个类 Animal 表示动物，定义它的两个子类 Bird 表示鸟，Lion 表示狮子。定义一个泛型类 Cage 表示笼子，它继承 java.util.HashSet 类。创建 Animal、Bird 和 Lion 对象，创建 Cage<Animal>、Cage<Bird> 和 Cage<Lion> 对象。动物对象可以添加到这些笼子对象中吗？笼子对象之间具有子类关系吗？如果要创建一个能装各种动物的笼子，应该使用什么通配符声明 Cage 对象？', sources=[a, b])
    add(12, 4, '泛型 Library 类', '下面的代码定义了一个媒体（Media）接口及其 3 个子接口：图书（Book）、视频（Video）和报纸（Newspaper），Library 类是一个非泛型类，使用泛型重新设计该类。',
        code('''import java.util.List;
import java.util.ArrayList;
interface Media { }
interface Book extends Media { }
interface Video extends Media { }
interface Newspaper extends Media { }
public class Library {
    private List resources = new ArrayList();
    public void addMedia(Media x) {
        resources.add(x);
    }
    public Media retrieveLast() {
        int size = resources.size();
        if (size > 0) {
            return (Media)resources.get(size - 1);
        }
        return null;
    }
}'''), sources=[b])
    add(12, 5, 'ArrayList 字符串转大写', '创建一个元素是字符串的 ArrayList 对象，在其中添加若干元素。编写程序，用下面三种方法将其中每个字符串转换成大写。①通过索引循环访问。②使用迭代器。③调用 replaceAll() 方法。', sources=[b])
    add(12, 6, 'HashSet 单词去重', '编写程序，将一个字符串中的单词解析出来，然后将它们添加到一个 HashSet 中，并输出每个重复的单词、不同单词的个数及消除重复单词后的列表。', sources=[b])
    add(12, 7, 'ArrayList 实现 MyStack', '编写程序，实现一个对象栈类 MyStack<T>，要求使用 ArrayList 类实现该栈类，该栈类的 UML 图如图 12-3 所示。',
        figure('MyStack<T>：私有 list:ArrayList<T> 存储元素；公有 MyStack()、isEmpty():boolean、getSize():int、peek():T、pop():T、push(t:T):void、search(t:T):int，分别为构造、判空、大小、返回栈顶、弹出栈顶、入栈和查找方法。', '图 12-3 MyStack 类的 UML 图', b, (548, 868, 927, 1122)), sources=[b])
    add(12, 8, 'HashSet 与 TreeSet', '编写程序，随机生成 20 个两位整数，将其分别存入 HashSet 和 TreeSet 对象，然后将它们输出，观察输出结果的不同。', sources=[b])
    add(12, 9, 'TreeSet 中的 Employee', '假设 Employee 类包含一个 int 型成员 id，如果要求 Employee 可按 id 值比较大小，编写 Employee 类。编写程序，创建几个 Employee 对象，将它们存放到 TreeSet 中并输出。', sources=[b])
    add(12, 10, 'PriorityQueue 队列', 'PriorityQueue 类是 Queue 接口的一个实现类，它实现一种优先级队列。编写程序，创建一个 PriorityQueue 对象，将整型数组 {1,5,3,7,6,9,8} 的元素插入队列，然后输出并观察结果。', sources=[b])

    a = 'Chapter13.png'
    add(13, 1, 'File 文件重命名', '编写程序 RenameFile，使用 File 实现将一个文件重新命名。要求源文件名和目标文件名从命令行输入，如下所示：',
        code(r'D:\study>java RenameFile Welcome.java Welcome.txt'), sources=[a])
    add(13, 2, '删除指定文件', '编写程序，程序执行后将一个指定的文件删除。如果该文件不存在，要求给出提示。用命令行参数传递要删除的文件名。', sources=[a])
    add(13, 3, '字节流复制文件', '编写程序，使用 FileInputStream 和 FileOutputStream 对象实现文件的复制，要求源文件和目标文件从命令行输入。', sources=[a])
    add(13, 4, '数据流读写整数', '编写程序，随机生成 10 个 1000～2000 的整数，将它们写到文件 data.dat 中，然后从该文件中读取这些整数，计算并输出这 10 个整数的和。要求使用 DataInputStream 和 DataOutputStream 类编程。', sources=[a])
    add(13, 5, '比较文件内容', '编写程序，比较两个指定的文件内容是否相同。', sources=[a])
    add(13, 6, 'Employee 对象序列化', '定义一个 Employee 类，编写程序使用对象输出流将几个 Employee 对象写入 employee.ser 文件中，然后使用对象输入流读取这些对象。', sources=[a])
    add(13, 7, 'Files 文件改名', '编写程序，使用 Files 类的有关方法实现文件改名，要求源文件不存在时给出提示，目标文件存在也给出提示。', sources=[a])
    add(13, 8, '列出目录内容', '编写程序，要求从命令行输入一个目录名称，输出该目录中所有子目录和文件。', sources=[a])
    add(13, 9, '读取小文本文件', '编写程序，读取一指定小文本文件的内容，并在控制台输出。如果该文件不存在，要求给出提示。', sources=[a])
    add(13, 10, '字符简单加密', '编写实现简单加密的程序。要求从键盘上输入一个字符，输出加密后的字符。加密规则是输入 A 则输出 Z，输入 B 则输出 Y，输入 a 则输出 z，输入 b 则输出 y。', sources=[a])
    add(13, 11, '文本中的重复单词', '编写程序，从一文本文件中读取若干行，实现将重复的单词存入一个 Set 对象中，将不重复的单词存入另一个 Set 对象中。', sources=[a])
    add(13, 12, '合并有序文件', '编写程序，合并两个已排序的文件。有两个文本文件，分别含有若干已排序（从小到大排序）的整数，程序读取两个文件中的整数，按顺序写入一个新的文件中。假设 a.txt 文件包含 123 234 565 789 892，b.txt 文件包含 65 92 101 214 565 881 960 1024，新文件 c.txt 的内容应该为 65 92 101 123 214 234 565 565 789 881 892 960 1024，其中，两个文件中相同的数（如 565）将被保留。', sources=[a])

    a = 'Chapter14.png'
    add(14, 1, 'Calculator 函数式接口', '有一个函数式接口 Calculator，它包含单一的 calculate() 抽象方法，另外它还包含两个默认方法，定义如下：',
        code('''@FunctionalInterface
public interface Calculator{
    public abstract double calculate(int a, int b);  // 唯一的抽象方法
    public default int subtract(int a, int b) {
        return a - b;
    }
    public default int add(int a, int b) {
        return a + b;
    }
}'''), '编写程序，使用 Lambda 表达式实现 calculate() 方法，使该方法可以计算 a²+b²。', sources=[a])
    add(14, 2, 'Predicate 判断素数', '编写程序，实现 Predicate<T> 接口的 test() 方法，通过 Lambda 表达式计算传递的整数是否是素数。然后使用 Lambda 表达式计算并打印 2～1000 的所有素数。', sources=[a])
    add(14, 3, '集合与流统计成绩', '编写程序，分别使用 Java 集合类和流（Stream）实现：一个班的学生按分数从高到低排序并打印出学生姓名、学号和分数。计算并打印出班级平均成绩、最高分和最低分。统计并打印优秀、良好、中等、及格和不及格的人数和占比。', sources=[a])
    add(14, 4, '筛选含 7 或被 7 整除的数', '编写程序，生成一个包含 1000 个随机生成的三位整数的流，过滤该流，使其仅包含能被 7 整除或含有 7 的数。输出这些数和个数。', sources=[a])
    add(14, 5, '单词流统计', '编写程序，将一个文件 article.txt 中的所有单词读入一个 Stream 中，统计长单词（长度大于 10 个字符）的数量，有多少个不同的单词。打印出最长的 5 个单词。', sources=[a])
    add(14, 6, '顺序流与并行流求阶乘', '编写程序，分别使用顺序流和并行流计算 10、20、30 和 40 这几个数的阶乘，输出结果及完成计算的时间。使用并行流是否比使用顺序流计算得更快？', sources=[a])


def transcribe_15_19(add, code, formula, figure):
    a, b = 'Chapter15_1.png', 'Chapter15_2.png'
    add(15, 1, 'FlowPane 和 HBox 布局', '编写程序，实现如图 15-28 所示的图形用户界面，要求如下：①创建两个 HBox 面板对象，其中控件之间间距为 10 像素，每个 HBox 面板上放置 3 个按钮。②创建 FlowPane 根面板，设置内容与边界的上下距离为 20 像素，左右距离为 15 像素，控件水平和垂直间距均为 10 像素。将两个 HBox 面板添加到 FlowPane 面板中。',
        figure('面板布局示例：两行按钮，第一行为 Button 1、Button 2、Button 3，第二行为 Button 4、Button 5、Button 6。', '图 15-28 FlowPane 和 HBox 面板布局', b, (99, 35, 411, 173)), sources=[a, b])
    add(15, 2, 'BorderPane 和 HBox 布局', '编写程序，实现如图 15-29 所示的图形用户界面，要求 4 个按钮添加到 HBox 面板中，将该面板添加到 BorderPane 根面板的下方。创建一个标签，把它添加到 Pane 面板中，将 Pane 面板添加到根面板的中央。',
        figure('面板布局示例：中央显示“这是一个标签”，下方为“第一页”“上一页”“下一页”“最后页”四个按钮。', '图 15-29 BorderPane 和 HBox 面板布局', b, (547, 33, 867, 176)), sources=[a, b])
    add(15, 3, '绘制圆柱', '编写程序，绘制一个圆柱，在其上放置一个文本，如图 15-30 所示。要求程序使用 Ellipse 类、Arc 类、Line 类以及 Text 类完成，根面板使用 Group 对象。',
        figure('圆柱由椭圆、弧和两条竖直线组成，中间显示文本“数据库”。', '图 15-30 绘制椭圆、弧和直线', b, (117, 376, 414, 550)), sources=[b])
    add(15, 4, '国际象棋盘', '编写程序，界面中显示国际象棋盘，其中每个黑白单元格都是一个填充了黑色或白色的 Rectangle 对象，如图 15-31 所示。',
        figure('显示国际象棋盘窗口，黑色与白色矩形交替排列。', '图 15-31 绘制国际象棋盘', b, (550, 376, 858, 555)), sources=[b])
    add(15, 5, '奥运五环旗', '编写程序，使用 Circle 对象和 Arc 对象绘制如图 15-32 所示的奥运五环旗。提示：五环的颜色分别为蓝色、黑色、红色、黄色和绿色，五环相互套在一起。',
        figure('奥运五环旗窗口：上排三个圆环、下排两个圆环互相套接；原始截图为黑白图，颜色要求见正文。', '图 15-32 奥运五环旗', b, (79, 780, 437, 1003)), sources=[b])
    add(15, 6, '绘制五角星', '编写程序，在界面中显示如图 15-33 所示的五角星。五角星是一个多边形，它共包含 10 个顶点。因此绘制五角星的重点是首先确定五角星外接圆的圆心坐标和半径，然后根据圆心坐标计算 10 个顶点的坐标位置。',
        figure('绘制五角星窗口：一个圆内有黑色五角星。', '图 15-33 绘制五角星', b, (565, 801, 879, 998)), sources=[b])
    add(15, 7, '文本节点样式', '编写程序，显示 5 个文本节点。对每个文本节点设置一个随机颜色和背景，并且将每个文本的字体设置为 Times New Roman、粗体（bold）和斜体（italic），字体大小为 22 像素，如图 15-34 所示。',
        figure('显示多个文本窗口，图中显示旋转后的 Hello 文本。正文要求显示 5 个文本节点，原图可见 4 个 Hello。', '图 15-34 绘制多个文本', b, (119, 1290, 407, 1421)), sources=[b])
    add(15, 8, '随机显示四张纸牌', '编写程序，显示从一副 52 张的扑克牌中随机选择的 4 张牌，如图 15-35 所示。牌的图像文件分别命名为 1.png、2.png、…、52.png，并保存在 images/card 目录下。4 张牌都是不同的且是随机选取的。',
        figure('随机显示四张牌窗口：示例为红桃 5、红桃 A、黑桃 10、梅花 K。', '图 15-35 随机显示 4 张牌', b, (531, 1258, 849, 1415)), sources=[b])

    a, b = 'Chapter16_1.png', 'Chapter16_2.png'
    add(16, 1, '按钮移动文本', '编写如图 16-25 所示的程序，通过按钮控制文本在面板中左右移动。程序运行时，单击“向左”按钮，文本向左移动 10 像素；单击“向右”按钮，文本向右移动 10 像素。',
        figure('按钮示例窗口：文本“JavaFX程序设计”，下方有“◀ 向左”和“▶ 向右”按钮。', '图 16-25 按钮示例', a, (123, 229, 432, 369)), sources=[a])
    add(16, 2, '按钮缩放文本', '编写程序，运行界面如图 16-26 所示。单击“放大”按钮时，文本字体放大 2 像素；单击“缩小”按钮时，文本字体缩小 2 像素。',
        figure('动作事件处理窗口：显示“2022 北京”，下方有“放大”“缩小”按钮。', '图 16-26 文本放大缩小', a, (569, 230, 837, 366)), sources=[a])
    add(16, 3, '文本框同步标签', '编写程序，其中包含一个标签和一个文本框。标签中使用字号为 100 的字体并显示“Hello,JavaFX”字符串，使用相同的字符串初始化文本框。当用户编辑文本框中的内容时同时更新标签上的内容。', sources=[a])
    add(16, 4, '鼠标事件改变圆的颜色', '编写程序，程序开始运行时在界面中显示一个白色的圆，在圆中按下鼠标键时圆的颜色变为蓝色，释放鼠标时，圆的颜色变为红色。', sources=[a])
    add(16, 5, '四则运算界面', '编写程序，实现加法、减法、乘法和除法操作。程序运行效果如图 16-27 所示。',
        figure('计算器示例：数1 为 3、数2 为 5、结果为 0.6，下方有加法、减法、乘法、除法按钮。', '图 16-27 计算器示例', b, (108, 66, 405, 207)), sources=[a, b])
    add(16, 6, '椭圆轨道动画', '编写程序，为一个圆创建动画效果，让圆表示一个行星，即它需要按照一个椭圆形的轨迹运动。程序运行效果如图 16-28 所示。要求使用 PathTransition 类完成。',
        figure('移动动画窗口：一个小圆沿椭圆轨道运动。', '图 16-28 移动动画', b, (576, 19, 894, 218)), sources=[a, b])
    add(16, 7, 'Timeline 闪烁文本', '使用 Timeline 编写动画程序，显示一个闪烁的文本。文本交替显示和消失，以产生闪烁动画效果，如图 16-29 所示。',
        figure('闪烁的文本窗口，文本内容为“Programming is fun”。', '图 16-29 文本闪烁动画', b, (157, 376, 409, 470)), sources=[b])
    add(16, 8, '滚动字幕', '编写程序，实现滚动字幕动画。要求字幕从右向左移动，如图 16-30 所示。',
        figure('滚动字幕窗口，字幕为“不忘初心，牢记使命”。', '图 16-30 滚动字幕动画', b, (582, 374, 850, 469)), sources=[b])
    add(16, 9, '简单浏览器', '在 javafx.scene.web 包中定义了 WebEngine 类，用来管理 Web 页面。WebView 类是节点类，来管理 WebEngine 并显示其内容。研究并使用这两个类，编写一个简单的浏览器程序。', sources=[b])

    a, b = 'Chapter17_1.png', 'Chapter17_2.png'
    add(17, 1, '查询 books 表', '编写程序，通过动态加载驱动程序的方式访问 webstore 数据库，查询 books 表的所有信息并从控制台打印。', sources=[a])
    add(17, 2, 'Oracle 员工表', 'Oracle 是一种著名的数据库管理系统，该数据库安装后其 JDBC 驱动程序也一并安装到系统中。如果假设其驱动程序名为 oracle.jdbc.driver.OracleDriver，数据库 URL 为 jdbc:oracle:thin:@127.0.0.1:1521:ORCL。数据库中有名为 WEBSTORE 的用户，密码为 123456，在该用户模式下建有 EMPLOYEES 表，其结构如下：',
        code('ENO CHAR(8)         -- 员工号\nENAME VARCHAR(20)   -- 姓名\nGENDER CHAR(1)      -- 性别\nBIRTHDATE DATE     -- 出生日期\nSALARY DOUBLE      -- 工资'),
        '编写程序，实现向表中插入一条记录，并显示表中所有记录。', sources=[b])
    add(17, 3, 'CustomerDao 数据访问', '在 MySQL 的 webstore 数据库中创建一个 customers 客户表，它包含字段及数据类型如下：',
        code('customer_id INT           -- 客户号\ncustomer_name VARCHAR(20) -- 客户名\nemail VARCHAR(50)         -- 邮箱地址\nbalance DOUBLE            -- 余额'),
        '编写程序，采用 DAO 模式设计访问数据库，定义 Dao 接口获得数据库连接对象，定义 CustomerDao 接口，其中包含下面方法：',
        code('public void addCustomer(Customer customer)\npublic void updateCustomer(Customer customer)\npublic void deleteCustomer(int customerId)\npublic Customer findCustomer(int customerId)'),
        '编写 CustomerDao 接口的实现类 CustomerDaoImpl。编写测试程序测试 DAO 接口各种方法的使用。', sources=[b])
    add(17, 4, '图形界面操作 products 表', '编写如图 17-5 所示的图形界面程序，要求通过按钮实现对 products 表中记录的查询、插入、删除及修改功能。提示：需使用可滚动、可更新的结果集对象。关于 JavaFX 图形界面程序请参阅第 15 章和第 16 章的内容。',
        figure('访问数据库窗口：商品号 102、商品名 平板电脑、品牌 苹果、价格 1990.0、库存量 5；按钮为第一条、前一条、下一条、最后一条、插入、删除、修改。', '图 17-5 通过按钮操作表记录', b, (249, 871, 730, 1105)), sources=[b])

    a, b = 'Chapter18_1.png', 'Chapter18_2.png'
    add(18, 1, 'Lambda 创建 Runnable', 'Runnable 接口是函数式接口，创建 Runnable 实例可以使用 Lambda 表达式。使用 Lambda 表达式改写程序 18.1。', sources=[a])
    add(18, 2, '账户取款线程冲突', '编写程序，创建一个 Account 类表示账户，初始余额 10000 元。定义一个线程类模拟从账户中取钱，规定每个线程每次只能取 100 元。编写程序，创建两个线程，从账户取钱，分析可能发生的冲突。Account 类定义如下：',
        code('''public class Account {
    private int balance = 10000;
    public void deposit(int amount) {     // 存款方法
        balance = balance + amount;
    }

    public void withdraw(int amount) {    // 取款方法
        balance = balance - amount;
    }

    public int getBalance() {            // 返回账户余额
        return balance;
    }
}'''), sources=[a, b])
    add(18, 3, 'Executor 计数任务', '编写程序，创建一个 Counter 对象（见程序 18.6），使用 Runnable 创建 100 个任务，在每个任务中调用 Counter 对象的 increment() 方法 100 次。同时输出每个任务的任务号和 Counter 对象的 count 成员值。将每个任务添加到 Executor 中执行，并分析执行结果。', sources=[b])
    add(18, 4, '同步计数任务', '修改习题 3 的程序，分别采用方法同步、块同步和 Lock 锁的方式使程序运行结果正确。', sources=[b])
    add(18, 5, 'Callable 与 Future 求和', '编写程序，覆盖 Callable<Long> 的 call() 方法，定义两个任务，一个任务求前 10 个斐波那契数之和。第二个任务求前 10 个素数之和。将这两个子任务提交给 ExecutorService 执行，通过返回的 Future<Long> 的 get() 方法输出两个子任务的结果。', sources=[b])
    add(18, 6, '并发统计文件单词', '编写程序，计算某个单词在一组文件中出现的频率。对每个文件，可以生成一个返回该文件统计结果的 Callable<Integer>。然后，将它们提交给 Executor。当所有任务完成时，得到一组 Future，对它们合并可得到结果。下面给出部分代码。',
        code('''String word = …;
// 指定一组文件
Set<Path> paths = …;
List<Callable<Integer>> tasks = new ArrayList<>();
for(Path p : paths){
    tasks.add( ()->{return p 中 word 变量出现的次数;});
}
List<Future<Integer>> results = executor.invokeAll(tasks);
long total = 0;
for(Future<Integer> result:results)
    total = total + result.get();'''), sources=[b])
    add(18, 7, '线程显示数字时钟', '编写程序，创建如图 18-5 所示的界面。使用一个单独的线程在标签中显示一个数字时钟，时间每隔一秒刷新一次。提示：线程的任务应该使用 javafx.concurrent.Task 类对象，并且将标签的 text 属性与任务的 message 属性绑定。',
        figure('当前时间窗口，标签以时:分:秒显示数字时钟，示例为 18:43:43。', '图 18-5 用单独线程显示时间', b, (598, 1056, 898, 1198)), sources=[b])

    a = 'Chapter19.png'
    add(19, 1, '域名查询 IP 地址', '编写程序，根据给定的主机域名，使用 Java 网络 API，查找该主机的所有 IP 地址。完成本实验，需要计算机连接到 Internet。假设给定主机名 www.baidu.com，程序输出结果如下：',
        code('主机名:www.baidu.com\nIP 地址:110.242.68.3\nIP 地址:110.242.68.4'), sources=[a])
    add(19, 2, 'Socket 计算圆面积', '使用 ServerSocket 类和 Socket 类编写一个字符界面的程序，在客户端接收用户从键盘输入一个圆的半径值，将它发送到服务器端，服务器计算圆的面积，并将结果发送回客户端。', sources=[a])
    add(19, 3, 'Socket 文件传输', '使用 ServerSocket 类和 Socket 类编写一个 GUI 程序，建立套接字通信管道，并将一个文件从一台计算机传到另一台计算机。', sources=[a])
    add(19, 4, 'Socket 聊天程序', '使用 ServerSocket 类和 Socket 类编写一个 GUI 程序，实现一个简单的聊天程序。程序界面如图 19-13 和图 19-14 所示。',
        figure('服务器端程序窗口：服务器端已启动，客户说:How are you?；下方有发送和退出按钮。', '图 19-13 服务器端程序的界面', a, (123, 483, 518, 661)),
        figure("客户端程序窗口：客户端已启动，服务器说:I'm fine,Thank you!；下方有发送和退出按钮。", '图 19-14 客户端程序的界面', a, (609, 483, 1008, 660)), sources=[a])
    add(19, 5, 'UDP 聊天程序', '使用数据报（UDP）协议实现习题 4 的聊天程序。', sources=[a])
    add(19, 6, '读取 URL 资源', '编写一个 GUI 程序，通过文本框输入一个 URL 地址，读取其连接到的资源内容，并在文本区中显示。', sources=[a])
