
import sys.stdio;
import sys.stdlib;

//如果有此函数, 表示此库为执行库. 执行库只能拥有一个入口函数
//没有此函数, 表示库为共享库(动态链接库)
void main(String[] args)
{
	#start //开始c代码, c代码不解析而是直接放入结果文件中

	#end	//结束c代码
}

//类实现了封装, 继承, 多态
//只有两种访问控制权限(public和private),用于控制库内外和类内外的访问权限
//public表示对库外部可见; 没有public表示是不可见(默认private)
public class Obj
{
	Obj add(Obj b){
		return a+b;
	}

	//public表示对类外部可见; 没有public表示只对内部和子类可见, 对外部不可见(默认)
	public void func(){}
}

//接口用于类型约束和推理
interface IColor{
	void doJob();
}

//函数类型定义
//返回类型, 函数名, 参数类型
func void Add(int a, int b);
funcdef IColor(int, int) Add;

// lambda表达式(匿名函数) https://www.zhihu.com/question/20125256/answer/141817632
// 它的第一个重要意义是可以在表达式当中直接定义一个函数，而不需要将定义函数和表达式分开，这样有助于将逻辑用更紧凑的方式表达出来。
// 它的第二个重要意义是引入了闭包。基本上来说常见的支持lambda表达式的语言里，不存在不支持闭包的lambda表达式；从函数式编程的角度来说，支持闭包也是很重要的。闭包是指将当前作用域中的变量通过值或者引用的方式封装到lambda表达式当中，成为表达式的一部分，它使你的lambda表达式从一个普通的函数变成了一个带隐藏参数的函数。
// 它的第三个重要意义（如果有的话）是允许函数作为一个对象来进行传递。某些语言由于历史原因，只有匿名函数可以作为对象传递，而具名函数不可以，比如PHP。

calculate( (int x, int y){return x+y;} ) //输入计算函数


//类型的意义: 指定内存空间的占用量
//变量的意义: 代表某内存空间

//函数算类型吗, 函数类型更像接口; 内存占用空间固定(值为函数地址)
//函数定义, 函数类型定义

//c++智能指针的语言化

//风格设计: id名称在前, 类型在后, 更符合思考方式吗? 
//风格设计: 取消语句后的;号
class Node
{
	age:uint8 shared|unique //共享对象|唯一对象
	name:String

	age(a: byte=0, color: uint32=0xffffff, tall: int32): void { }
	age(a:uint8):void { age = a;}
	age():uint8 { return age; }

}

//类成员默认是私有的, 公有的必须要明确写上public
public class App
{
	int8 num2;
	uint8 num3
	int16 num4;
	uint16 num5
	int32 num6;
	uint32 num7
	int64 num6;
	uint64 num7
	float32 num8;
	float64 num9;

	byte num; //相当于uint8
	char num; //相当于int8
	
	Obj o;

	uint8 age;

	boolean bool;
	String str;

	App(){ } //构造器
	App_(){ } //析构器

	New(){} //构造器
	Del(){} //析构器

	// age属性
	void age(uint8 age){ this.age=age;}
	uint8 age(){ return age; }

	

	void doSomething() {}
	void stopSomething() {}
	Obj dowork(Obj a, Obj b) {
		return a.add( b );
	}

	//函数overload, 返回类型+参数类型来确定唯一的函数签名
	void do(int a){} //1版本
	void do(int a, int b=0){} //2版本
	void do(int a, String b=null){} //4版本
	void do(int a, int b=0, int c){} //3版本
}

class AppSub {
	//函数override
	over void stopSomething() {}
	//函数overwrite
	void do(int a){}
}



void main ()
{
	App a; //默认是null
	a= App() //相当与 new App(), 省略new

	//重载函数调用
	a.do(1, _) //应该是哪个版本?

	App c=<a //=< 表示值转移, 转移后a值为null
	App c=a //= 表示值复制, 转移后a值不变

	App d= a?.doAnything() //?表示如果a为null, 则不执行后面的代码, 否则执行
	//?( ?[ ?. 还是 ?单独
	//(a? b: c) a?+=5;

	AppSub as = AppSub()
	App d = a.b.c()>App.add(); //>App 表示前面的量转换为App类型
	App d = a.b.c()(App).add(); //(App) 表示前面的量转换为App类型


	App b = App();
	dele a;

	App[] list = []; //数组简化版初始化, 完整版为 Array<App>()
	App[] list = [a, b, c]; //枚举量初始化

	Map<App> map = {}; //映射表简化版初始化, 完整版为 Map<App>(), 默认key为String类型
	Map<App> map = {a:b, c:d}; //映射量初始化
}

void doSomeError()
{
	...
}{
	错误处理代码
}


//异步方式
//如果有一个完成了就执行do后的语句
oneOf{
	function(){}
	function(){}
	...
}do{
	...
}

//如果所有完成了才执行do后的语句
allOf{
	function(){return true or return false;}
	function(){}
	...
}do{
	...
}

//任务之间的依赖, 不能循环依赖
//没有依赖关系的任务可以并发