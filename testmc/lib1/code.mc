
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
//public表示对库外部可见; 没有public表示是不可见(默认)
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
func void Add(int a, int b);
IColor(int, int) Add;


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

	//函数overload, 情况1:参数数量相同, 类型不同; 情况2:参数数量不同, 类型相同
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


	AppSub as = AppSub()
	App d = a.b.c()>App.add(); //>App 表示前面的量转换为App类型
	App d = a.b.c()(App).add(); //(App) 表示前面的量转换为App类型


	App b = App();
	dele a;

	App[] list = []; //数组简化版初始化, 完整版为 Array<App>()
	Map<App> map = {}; //映射表简化版初始化, 完整版为 Map<App>(), 默认key为String类型

}