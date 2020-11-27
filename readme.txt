modernC(mc)语言说明
---------------------------------------------
原理就是 modernC代码 先转换成 c代码, 再调用gcc编译c代码, 生成exe/lib文件

modernC项目 会构造出 cmake项目
modernC文件 会生成多个 c文件+h文件. 并加入cmake项目中

一个 mc项目可生成多个动态库和多个可执行文件
一个 mc文件可包含多个可见类和多个不可见类

平台/编译器自动识别/手动指定, debug/release指定, 32/64指定

mc除了整形,布尔形变量外其余都是指针变量

封装实现, 多态实现, 拷贝/移动实现




项目结构
---------------------------------------------
projectName(folder)
	libName1(folder)					生成动态库
		src1.mc
		pathName1(folder)
			src2.mc
			pathName2(folder)
				src3.mc
				...
			...
		...

	libName2(folder)					生成动态库
	...

	exeName1(folder)					生成执行
		src.mc


mc.json 所在的路径即为项目的根路径
项目根据mc.json 会生成cmakelists.txt
每个.mc文件都会转换成.c文件和.h文件
都放在对应的路径下


文件结构
---------------------------------------------
	import libName.fileName.typeName;					导入其他类型格式: 库名.路径名...文件名.类型名
	import libName.pathName... .fileName.typeName;
	...

	public class className1 { }							外部可见类型
	public class className2 { }
	...
	class className3 { }								外部不可见类型
	...