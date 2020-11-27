package build_a_simple_interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import util.FileUtil;

// https://www.cnblogs.com/xiehy/archive/2010/02/04/1663825.html 运算符优先级
// 算数运算符>关系运算符>逻辑运算符>赋值运算符
enum TokenType {
	Error, None, As, //错误类型, 无效类型, 类型转换
	NumberLiteral, StringLiteral, Identifier, Punctuation, Comment, End, //
	Import, Class, Interface, Enum, Extends, Implements, //
	If, Else, While, Do, For, Continue, Break, Return, Sizeof, //
	//并列运算符
	Comma, // ,   
	//赋值运算符
	Assign, AssignAdd, AssignSub, AssignMul, AssignDiv, AssignMod, Question, // = += -= *= /= %= ? (从右到左)
	//二元运算符
	Lor, Land, Bor, Bxor, Band, Eq, Ne, Lt, Gt, Le, Ge, Shl, Shr, Add, Sub, Mul, Div, Mod,
	//|| &&    |    ^     &     ==  !=  <   >   <=  >=  <<   >>   +    -    *    /    %
	//一元运算符
	Inc, Dec, Lnot, Binv, // ++ -- ! ~  (从右到左) 
	//成员运算符
	BrackL, ParenL, Dot, // [ ( .   
	//优先级从低到高 
	ParenR, BrackR, BraceL, BraceR, Colon, Sharp, SemiColon, Quote, QuoteSingle, Slope,
	//  )   ]       {       }       :      #      ;          "      '            \     
}

class Lexer {

	// 操作集
	static TokenType[] assignOps = { TokenType.Assign, TokenType.AssignAdd, TokenType.AssignSub, TokenType.AssignMul,
			TokenType.AssignDiv, TokenType.AssignMod };
	static TokenType[] binaryOps = { TokenType.Assign, TokenType.AssignAdd, TokenType.AssignSub, TokenType.AssignMul,
			TokenType.AssignDiv, TokenType.AssignMod, TokenType.Lor, TokenType.Land, TokenType.Bor, TokenType.Bxor,
			TokenType.Band, TokenType.Eq, TokenType.Ne, TokenType.Lt, TokenType.Gt, TokenType.Le, TokenType.Ge,
			TokenType.Shl, TokenType.Shr, TokenType.Add, TokenType.Sub, TokenType.Mul, TokenType.Div, TokenType.Mod };
	static TokenType[] unaryPreOps = { TokenType.Lnot, TokenType.Binv, TokenType.Sub, TokenType.Inc, TokenType.Dec };
	static TokenType[] unaryPostOps = { TokenType.Inc, TokenType.Dec };
	static TokenType[] operands = { TokenType.NumberLiteral, TokenType.StringLiteral, TokenType.Identifier };

	public static HashMap<TokenType, Integer> priority = new HashMap<>();
	{
		priority.put(TokenType.Assign, 5);
		priority.put(TokenType.AssignAdd, 5);
		priority.put(TokenType.AssignSub, 5);
		priority.put(TokenType.AssignMul, 5);
		priority.put(TokenType.AssignDiv, 5);
		priority.put(TokenType.AssignMod, 5);
		priority.put(TokenType.Lor, 10);
		priority.put(TokenType.Land, 20);
		priority.put(TokenType.Bor, 30);
		priority.put(TokenType.Bxor, 40);
		priority.put(TokenType.Band, 50);
		priority.put(TokenType.Eq, 60);
		priority.put(TokenType.Ne, 60);
		priority.put(TokenType.Lt, 70);
		priority.put(TokenType.Gt, 70);
		priority.put(TokenType.Le, 70);
		priority.put(TokenType.Ge, 70);
		priority.put(TokenType.Shl, 80);
		priority.put(TokenType.Shr, 80);
		priority.put(TokenType.Add, 90);
		priority.put(TokenType.Sub, 90);
		priority.put(TokenType.Mul, 100);
		priority.put(TokenType.Div, 100);
		priority.put(TokenType.Mod, 100);
	}
	static HashMap<String, TokenType> preset = new HashMap<>();
	{
		preset.put("import", TokenType.Import);
		preset.put("class", TokenType.Class);
		preset.put("Interface", TokenType.Interface);
		preset.put("enum", TokenType.Enum);
		preset.put("extends", TokenType.Extends);
		preset.put("implements", TokenType.Implements);
		preset.put("if", TokenType.If);
		preset.put("else", TokenType.Else);
		preset.put("while", TokenType.While);
		preset.put("do", TokenType.Do);
		preset.put("for", TokenType.For);
		preset.put("continue", TokenType.Continue);
		preset.put("break", TokenType.Break);
		preset.put("return", TokenType.Return);
		preset.put("sizeof", TokenType.Sizeof);
		preset.put("=", TokenType.Assign);
		preset.put("?", TokenType.Question);
		preset.put("||", TokenType.Lor);
		preset.put("&&", TokenType.Land);
		preset.put("|", TokenType.Bor);
		preset.put("^", TokenType.Bxor);
		preset.put("&", TokenType.Band);
		preset.put("==", TokenType.Eq);
		preset.put("!=", TokenType.Ne);
		preset.put("<", TokenType.Lt);
		preset.put(">", TokenType.Gt);
		preset.put("<=", TokenType.Le);
		preset.put(">=", TokenType.Ge);
		preset.put("<<", TokenType.Shl);
		preset.put(">>", TokenType.Shr);
		preset.put("+", TokenType.Add);
		preset.put("-", TokenType.Sub);
		preset.put("*", TokenType.Mul);
		preset.put("/", TokenType.Div);
		preset.put("%", TokenType.Mod);
		preset.put("++", TokenType.Inc);
		preset.put("--", TokenType.Dec);
		preset.put("(", TokenType.ParenL);
		preset.put(")", TokenType.ParenR);
		preset.put("[", TokenType.BrackL);
		preset.put("]", TokenType.BrackR);
		preset.put("{", TokenType.BraceL);
		preset.put("}", TokenType.BraceR);
		preset.put(":", TokenType.Colon);
		preset.put("#", TokenType.Sharp);
		preset.put(".", TokenType.Dot);
		preset.put(";", TokenType.SemiColon);
		preset.put(",", TokenType.Comma);
		preset.put("\"", TokenType.Quote);
		preset.put("'", TokenType.QuoteSingle);
		preset.put("\\", TokenType.Slope);
		preset.put("~", TokenType.Binv);
	}
	char[] source;
	int idx;

	int lineIdx = 1; //当前词素所在行
	String tokenValue; //当前词素值
	TokenType token; //当前词素类型

	Lexer(String src) {
		source = src.toCharArray();
	}

	/** 获取下一个字符 */
	char next() {
		if (++idx >= source.length)
			return 0;
		char c = source[idx];
		if (c == '\n')
			lineIdx++;
		return c;
	}

	/** 查看下一个字符 */
	char peek() {
		if (idx + 1 >= source.length)
			return 0;
		return source[idx + 1];
	}

	boolean isAlpha(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	boolean isWhite(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	/** 获取下一个词素(词法解析) */
	public TokenType nextToken() {
		char c = (idx < source.length ? source[idx] : 0);
		int start;
		while (c != 0) {
			//解析空白部分
			if (isWhite(c)) {
				c = next();
				continue;
			}
			//解析行注释
			else if (c == '/' && peek() == '/') {
				while (c != 0 && c != '\n')
					c = next();
				continue;
			}
			//解析块注释
			else if (c == '/' && peek() == '*') {
				while (c != 0 && (c != '*' || peek() != '/'))
					c = next();
				continue;
			}
			//解析标识符标记
			else if (c == '_' || isAlpha(c)) {
				start = idx;
				while (c == '_' || isAlpha(c) || isNumber(c))
					c = next();
				tokenValue = String.valueOf(Arrays.copyOfRange(source, start, idx));
				if (preset.containsKey(tokenValue))
					return preset.get(tokenValue);
				return TokenType.Identifier;
			}
			//解析数字标记
			else if (isNumber(c)) {
				start = idx;
				while (isNumber(c)) //整数部分
					c = next();
				if (c == '.') //小数部分
				{
					c = next();
					while (isNumber(c))
						c = next();
				}
				if (c == 'e') { //指数部分
					c = next();
					if (c == '+' || c == '-')
						c = next();
					while (isNumber(c))
						c = next();
				}
				tokenValue = String.valueOf(Arrays.copyOfRange(source, start, idx));
				return TokenType.NumberLiteral;
			}
			//解析字符串标记
			else if (c == '"') {
				c = next();
				start = idx;
				while (c != 0 && c != '"') {
					if (c == '\\')
						next();
					c = next();
				}
				tokenValue = String.valueOf(Arrays.copyOfRange(source, start, idx));
				next();
				return TokenType.StringLiteral;
			}
			//解析标点符号标记
			else {
				tokenValue = "" + c + peek(); //双字符标点符号
				if (preset.containsKey(tokenValue)) {
					next();
					next();
					return preset.get(tokenValue);
				}
				tokenValue = "" + c; //单字符标点符号
				if (preset.containsKey(tokenValue)) {
					next();
					return preset.get(tokenValue);
				}

				tokenValue = "无法识别的标记: " + c; //无法识别的标点符号
				return TokenType.Error;
			}
		}
		tokenValue = "eof";
		return TokenType.End;
	}

	/** 查看下一个词素 */
	void peekToken() {
		int old = idx;
		token = nextToken();
		idx = old;
	}

	/** 下一个词素是否为指定类型 */
	boolean isbe(TokenType tt) {
		int old = idx;
		token = nextToken();
		idx = old;
		return (token == tt);
	}

	/** 下一个词素是否为指定类型之一 */
	boolean isOneof(TokenType[] list) {
		int old = idx;
		token = nextToken();
		idx = old;
		for (TokenType tt : list)
			if (tt == token)
				return true;
		return false;
	}

	/** 下一个词素必须为指定类型, 不是则报错 */
	void mustbe(TokenType tt) {
		token = nextToken();
		if (token != tt) {
			tokenValue = "不是期待的词素类型 " + tt.toString();
			token = TokenType.Error;
			System.out.println(lineIdx + ": " + tokenValue + "\n" + token);
		}
	}

	/** 下一个词素或许为指定类型, 不是则回退 */
	boolean maybe(TokenType tt) {
		int old = idx;
		token = nextToken();
		if (token == tt)
			return true;
		idx = old;
		return false;
	}

	/** 下一个词素或许为指定类型之一, 不是则回退 */
	boolean maybeOneof(TokenType[] list) {
		int old = idx;
		token = nextToken();
		for (TokenType tt : list)
			if (tt == token)
				return true;
		idx = old;
		return false;
	}

	Program root;

	// {importDef}	{typeDef}
	/** 语法解析: 生成语法树 */
	public Ast parse() {
		ArrayList<Import> imports = new ArrayList<>();
		ArrayList<ClassDef> classes = new ArrayList<>();
		idx = 0;
		root = new Program();
		do {
			if (maybe(TokenType.Import))
				imports.add(parseImport());
			else if (maybe(TokenType.Class))
				classes.add(parseClass());
			else
				nextToken();
		} while (token != TokenType.End && token != TokenType.Error);

		root.imports = imports.toArray(new Import[0]);
		root.classes = classes.toArray(new ClassDef[0]);
		return root;
	}

	// import id {. id} ;
	/* 解析-类型导入 */
	Import parseImport() {
		ArrayList<String> ids = new ArrayList<>();

		mustbe(TokenType.Identifier);
		ids.add(tokenValue);
		while (maybe(TokenType.Dot)) {
			mustbe(TokenType.Identifier);
			ids.add(tokenValue);
		}
		mustbe(TokenType.SemiColon);

		Import i = new Import();
		i.ids = ids.toArray(new String[0]);
		return i;
	}

	// class id [extends id] [implements id {, id}] { {varDef|funcDef} }
	/** 解析-类定义 */
	ClassDef parseClass() {
		ClassDef c = new ClassDef();
		ArrayList<VarDef> vars = new ArrayList<>();
		ArrayList<FuncDef> funcs = new ArrayList<>();

		mustbe(TokenType.Identifier);
		c.id = tokenValue; //类标识
		//父类标识
		if (maybe(TokenType.Extends)) {
			mustbe(TokenType.Identifier);
			c.parentId = tokenValue;
		}
		//接口标识
		if (maybe(TokenType.Implements)) {
			ArrayList<String> ls = new ArrayList<>();
			mustbe(TokenType.Identifier);
			ls.add(tokenValue);
			while (maybe(TokenType.Dot)) {
				mustbe(TokenType.Identifier);
				ls.add(tokenValue);
			}
			c.interfaceIds = ls.toArray(new String[0]);
		}
		mustbe(TokenType.BraceL);
		while (!maybe(TokenType.BraceR)) {
			int old = idx;
			mustbe(TokenType.Identifier);
			mustbe(TokenType.Identifier);
			//解析函数定义
			if (maybe(TokenType.ParenL)) {
				idx = old;
				funcs.add(parseFuncDef());
			}
			//解析变量定义
			else {
				idx = old;
				for (VarDef v : parseVardef())
					vars.add(v);
			}
		}
		c.vars = vars.toArray(new VarDef[0]);
		c.funcs = funcs.toArray(new FuncDef[0]);
		return c;
	}

	// type id [= exp] {, id [= exp]} ;
	/** 解析-变量定义 */
	VarDef[] parseVardef() {
		ArrayList<VarDef> vs = new ArrayList<>();

		mustbe(TokenType.Identifier);
		String type = tokenValue;
		do {
			VarDef vd = new VarDef();
			vd.type = type;
			mustbe(TokenType.Identifier);
			vd.id = tokenValue;
			if (maybe(TokenType.Eq)) {
				vd.value = parseExp();
			}
			vs.add(vd);
		} while (maybe(TokenType.Comma));

		mustbe(TokenType.SemiColon);
		return vs.toArray(new VarDef[0]);
	}

	// type id ( [parameter] ) block
	/** 解析-函数定义 */
	FuncDef parseFuncDef() {
		FuncDef fd = new FuncDef();
		ArrayList<VarDef> ps = new ArrayList<>();

		mustbe(TokenType.Identifier);
		fd.type = tokenValue;
		mustbe(TokenType.Identifier);
		fd.id = tokenValue;
		mustbe(TokenType.ParenL);
		if (!maybe(TokenType.ParenR)) {
			// 参数解析
			do {
				VarDef vd = new VarDef();
				mustbe(TokenType.Identifier);
				vd.type = tokenValue;
				mustbe(TokenType.Identifier);
				vd.id = tokenValue;
				if (maybe(TokenType.Eq)) {
					vd.value = parseExp();
				}
				ps.add(vd);
			} while (maybe(TokenType.Comma));
			fd.parameters = ps.toArray(new VarDef[0]);
			mustbe(TokenType.ParenR);
		}
		fd.block = parseBlock();
		return fd;
	}

	// { {varDef|stmt|block|exp} }
	/** 解析-代码块 */
	Block parseBlock() {
		Block b = new Block();
		ArrayList<Ast> vsbe = new ArrayList<>();

		mustbe(TokenType.BraceL);
		while (!maybe(TokenType.BraceR)) {
			peekToken();
			if (token == TokenType.BraceL)
				vsbe.add(parseBlock());
			else if (token == TokenType.If)
				vsbe.add(parseStmtIf());
			else
				vsbe.add(parseExp());
		}

		b.vsbe = vsbe.toArray(new Ast[0]);
		return b;
	}

	Stmt parseStmtIf() {
		return null;
	}

	// item {binaryOp item}
	/** 解析出并列操作符(左到右) */
	Ast parseExp() {
		Ast item = parseOpBinary();
		if (!isbe(TokenType.Comma))
			return item;

		ArrayList<Ast> list = new ArrayList<>();
		list.add(item);
		while (maybe(TokenType.Comma))
			list.add(parseOpBinary());

		OpComma exp = new OpComma();
		exp.items = list.toArray(new Ast[0]);
		return exp;
	}

	/** 解析出二元操作符(左到右) */
	Ast parseOpBinary() {
		ArrayList<Ast> stack = new ArrayList<>();
		stack.add(parseOpUnary());
		if (maybeOneof(binaryOps)) {
			stack.add(new Op(token));
			stack.add(parseOpUnary());
		} else
			return stack.get(0);

		int endidx;
		OpBinary bin;
		while (maybeOneof(binaryOps)) {
			endidx = stack.size() - 1;
			Op op1 = (Op) stack.get(endidx - 1);
			Op op2 = new Op(token);
			//如果op1的优先权高 或者 op1和op2相同并且是左结合 - 则优先合并栈中对象
			if (op1.high(op2) || op1.same(op2) && !op1.isOneof(assignOps)) {
				bin = new OpBinary(stack.get(endidx - 2));
				bin.op = op1.op;
				bin.right = stack.remove(endidx);
				stack.remove(endidx - 1);
				stack.remove(endidx - 2);
				stack.add(bin); //出栈三个, 进栈一个
			}
			stack.add(op2);
			stack.add(parseOpUnary());
		}
		while (stack.size() > 1) {
			endidx = stack.size() - 1;
			bin = new OpBinary(stack.get(endidx - 2));
			bin.right = stack.remove(endidx);
			bin.op = ((Op) stack.remove(endidx - 1)).op;
			stack.remove(endidx - 2);
			stack.add(bin);
		}
		return stack.get(0);
	}

	/** 解析出一元操作符 */
	Ast parseOpUnary() {
		//是否前缀
		if (maybeOneof(unaryPreOps)) {
			OpUnaryPre op = new OpUnaryPre();
			op.op = token;
			op.operand = parseItem();
			return op;
		}
		Ast i = parseItem();
		//是否后缀
		if (maybeOneof(unaryPostOps)) {
			OpUnaryPost op = new OpUnaryPost();
			op.op = token;
			op.operand = i;
			return op;
		}
		return i;
	}

	/** 解析出成员操作符 (类型) () [] . */
	Ast parseItem() {
		Ast f = parseFactor();
		//id ( exp )
		if (maybe(TokenType.ParenL)) {
			OperandCall call = new OperandCall();
			call.id = f;
			call.paras = parseExp();
			mustbe(TokenType.ParenR);
			return call;
		}
		//id [ exp ]
		if (maybe(TokenType.BrackL)) {
			OperandIdx idx = new OperandIdx();
			idx.id = f;
			idx.sub = parseOpBinary();
			mustbe(TokenType.BrackR);
			return idx;
		}
		//id . id
		if (maybe(TokenType.Dot)) {
			mustbe(TokenType.Identifier);
			OperandMember mem = new OperandMember();
			mem.id = f;
			mem.idMember = tokenValue;
			return mem;
		}
		return f;
	}

	/** 解析出操作数 */
	Ast parseFactor() {
		//num / str / id
		if (maybeOneof(operands))
			return new Operand(token, tokenValue);
		// ( exp )
		if (maybe(TokenType.ParenL)) {
			Ast item = parseExp();
			// ( exp ? exp : exp )		//三元 条件运算符
			if (maybe(TokenType.Question)) {
				OpTri tri = new OpTri();
				tri.cond = item;
				tri.isTrue = parseOpBinary();
				mustbe(TokenType.Colon);
				tri.isFalse = parseOpBinary();
				mustbe(TokenType.ParenR);
				return tri;
			}
			mustbe(TokenType.ParenR);
			return item;
		}
		return null;
	}

	/** c代码文件生成 */
	void Trnaslaate() {
	}
}

abstract class Ast {
	void print(String tab) {
	};
}

/** 操作符 */
class Op extends Ast {
	public TokenType op;

	Op(TokenType op) {
		this.op = op;
	}

	boolean high(Op op) {
		return Lexer.priority.get(this.op) > Lexer.priority.get(op.op);
	}

	boolean same(Op op) {
		return Lexer.priority.get(this.op) == Lexer.priority.get(op.op);
	}

	boolean isOneof(TokenType[] list) {
		for (TokenType tt : list)
			if (tt == op)
				return true;
		return false;
	}
}

/** 类型转换或者函数调用操作符() */
class OperandCall extends Ast {
	Ast id;
	Ast paras; //参数表达式, (当参数为类型id时(id的首字母大写)为类型转换操作, 否则就是函数调用)

	void print(String tab) {
		id.print(tab);
		System.out.println(tab + "(");
		paras.print(tab + "\t");
		System.out.println(tab + ")");
	}
}

/** 下标操作符[] */
class OperandIdx extends Ast {
	Ast id;
	Ast sub; //下标表达式

	void print(String tab) {
		System.out.println(tab + id + "[");
		sub.print(tab + "\t");
		System.out.println(tab + "]");
	}
}

/** 成员操作符. */
class OperandMember extends Ast {
	Ast id;
	String idMember;

	void print(String tab) {
		id.print(tab);
		System.out.println(tab + "." + idMember);
	}
}

/** 操作数 */
class Operand extends Ast {
	String value;
	TokenType type; //操作数类型

	Operand(TokenType type, String str) {
		this.type = type;
		value = str;
	}

	void print(String tab) {
		System.out.println(tab + value);
	}
}

/** 并列操作符, */
class OpComma extends Ast {
	Ast[] items;

	void print(String tab) {
		for (Ast item : items)
			item.print(tab);
	}
}

/** 三元操作符?: */
class OpTri extends Ast {
	Ast cond, isTrue, isFalse;

	void print(String tab) {
		cond.print(tab + "\t");
		System.out.println("?");
		isTrue.print(tab + "\t");
		System.out.println(":");
		isFalse.print(tab + "\t");
	}
}

/** 二元操作符 和 赋值操作符 */
class OpBinary extends Ast {
	TokenType op; //操作符
	Ast left;
	Ast right;

	OpBinary(Ast left) {
		this.left = left;
	}

	void print(String tab) {
		if (left != null)
			left.print(tab + "\t");
		if (op != null) {
			System.out.println(tab + op.toString());
			right.print(tab + "\t");
		}
	}
}

/** 一元操作符(前缀) */
class OpUnaryPre extends Ast {
	TokenType op; //操作符
	Ast operand;

	void print(String tab) {
		System.out.println(tab + op);
		operand.print(tab);
	}
}

/** 一元操作符(后缀/无) */
class OpUnaryPost extends Ast {
	TokenType op; //操作符
	String typeid; //类型转换的类型值
	Ast operand;

	void print(String tab) {
		operand.print(tab);
		if (op != TokenType.None)
			System.out.println(tab + "\t" + op);
	}
}

class Program extends Ast {
	Import[] imports;
	ClassDef[] classes;
	InterfaceDef[] interfaces;
	EnumDef[] enums;

	void print(String tab) {
		System.out.println("\nprogram:");
		if (imports != null)
			for (Import i : imports)
				i.print(tab);
		if (classes != null)
			for (ClassDef c : classes)
				c.print(tab);
		if (interfaces != null)
			for (InterfaceDef i : interfaces)
				i.print(tab);
		if (enums != null)
			for (EnumDef e : enums)
				e.print(tab);
	}
}

class Import extends Ast {
	String[] ids;

	void print(String tab) {
		int i = 0;
		System.out.print("import ");
		for (String id : ids)
			System.out.print(id + (++i == ids.length ? ";" : "."));
		System.out.println();
	}
}

/** 代码块, 允许嵌套 */
class Block extends Ast {
	Ast[] vsbe; //局部变量, 语句,代码块,表达式

	void print(String tab) {
		System.out.println(tab + "{");
		for (Ast s : vsbe)
			s.print(tab + "\t");
		System.out.println(tab + "}");
	}
}

class ClassDef extends Ast {
	String id;
	String parentId;
	String[] interfaceIds;
	VarDef[] vars;
	FuncDef[] funcs;

	void print(String tab) {
		System.out.println(tab + "class " + id);
		if (parentId != null)
			System.out.println(tab + "\textends " + parentId);
		System.out.println(tab + "{");
		for (VarDef v : vars)
			v.print(tab + "\t");
		for (FuncDef f : funcs)
			f.print(tab + "\t");
		System.out.println(tab + "}");
	}
}

class InterfaceDef extends Ast {
	FuncDel[] funcs;
}

class EnumDef extends Ast {
}

/** 变量定义 */
class VarDef extends Ast {
	String id;
	String type; //变量类型
	Ast value; //初始化的表达式

	void print(String tab) {
		System.out.println(tab + type + " " + id + (value != null ? "=" : ""));
		if (value != null)
			value.print(tab + "\t");
	}
}

class FuncDef extends Ast {
	String id;
	VarDef[] parameters; //函数参数
	String type; //返回类型
	Block block;

	void print(String tab) {
		System.out.println(tab + type + " " + id + "(");
		for (VarDef v : parameters)
			v.print(tab + "\t");
		System.out.println(tab + ")");
		block.print(tab);
	}
}

class FuncDel extends Ast {
}

class Stmt extends Ast {
}

/** 文件管理器(.mc文件解析,转换到.c文件) */
public class BuildaSimpleInterpreter {
	public static void main(String[] args) {
		// String src = "	/*stop //	*/	void main(){int a=1; string s = \"he\'l\\l\'o\"; float f = 3.1415;	}	";
		// String src = "12312 3.1415  78e12312 9e-1213 9.e12 6.8e90 0e.";
		// String src = FileUtil.getString("testmc/code.mc");
		// String src = FileUtil.getString("src/build_a_simple_interpreter/BuildaSimpleInterpreter.java");
		// String src = FileUtil.getString("src/toc/rulesMc.txt");
		String src = FileUtil.getString("testmc/lib1/code2.mc");

		Lexer lexer = new Lexer(src);

		TokenType tt;
		do {
			tt = lexer.nextToken();
			System.out.println(lexer.tokenValue + " \t:" + tt.toString() + " \t " + lexer.idx + "," + lexer.lineIdx);
		} while (tt != TokenType.End && tt != TokenType.Error);

		lexer.idx = 0;
		lexer.parseExp().print("");
	}
}

/* 项目管理器 */
class Project {
	public static void main(String[] args) {
		HashMap<String, String> flags = new HashMap<>();
		for (String flag : args)
			flags.put(flag, flag);

		if (flags.containsKey("init")) { //项目路径/ mc.json文件/ cmakelists.txt文件生成
		}
		if (flags.containsKey("build")) { //mc文件检索,转换成c
		}
		if (flags.containsKey("run")) { //c编译成exe/lib
		}

	}
}
