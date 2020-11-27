package toc;

/** 语法处理
 */
public class Syntax {

	void parse() {
	}

	void parseImport() {
	}

	void parseClass() {
	}

	void parseVar() {
	}

	void parseFunc() {
	}

}

class Ast {
	Ast scope; // 作用域
	Ast parent;// 父对象
	Ast kind; // 变量还是类型
	Ast type; // 如果是变量, 则指定它的类型

}

/** 引用对象 */
class AstImport extends Ast {
}

/** 定义类型对象 */
class AstClass extends Ast {
}

/** 定义变量对象 */
class AstVar extends Ast {
}

/** 定义函数对象 */
class AstFunc extends Ast {
}
