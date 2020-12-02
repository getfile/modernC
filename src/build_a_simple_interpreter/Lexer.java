package build_a_simple_interpreter;

import java.util.ArrayList;
import java.util.Arrays;

public class Lexer {

	char[] source;
	int idx;

	int lineIdx = 1; //当前词素所在行
	String tokenValue; //当前词素值
	Token token; //当前词素类型

	Lexer(String src) {
		new Tokens();
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
	public Token nextToken() {
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
				if (Tokens.preset.containsKey(tokenValue))
					return Tokens.preset.get(tokenValue);
				return Token.Identifier;
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
				return Token.NumberLiteral;
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
				return Token.StringLiteral;
			}
			//解析标点符号标记
			else {
				tokenValue = "" + c + peek(); //双字符标点符号
				if (Tokens.preset.containsKey(tokenValue)) {
					next();
					next();
					return Tokens.preset.get(tokenValue);
				}
				tokenValue = "" + c; //单字符标点符号
				if (Tokens.preset.containsKey(tokenValue)) {
					next();
					return Tokens.preset.get(tokenValue);
				}

				tokenValue = "无法识别的标记: " + c; //无法识别的标点符号
				System.out.println(lineIdx + ": " + tokenValue);
				return Token.Error;
			}
		}
		tokenValue = "eof";
		return Token.End;
	}

	/** 查看下一个词素 */
	void peekToken() {
		int old = idx, oldl = lineIdx;
		token = nextToken();
		idx = old;
		lineIdx = oldl;
	}

	/** 下一个词素是否为指定类型 */
	boolean isbe(Token tt) {
		int old = idx, oldl = lineIdx;
		token = nextToken();
		idx = old;
		lineIdx = oldl;
		return (token == tt);
	}

	/** 下下一个词素是否为指定类型 */
	boolean isbe(Token tt, int off) {
		int old = idx, oldl = lineIdx;
		nextToken();
		token = nextToken();
		idx = old;
		lineIdx = oldl;
		return (token == tt);
	}

	/** 下一个词素是否为指定类型之一 */
	boolean isOneof(Token[] list) {
		int old = idx, oldl = lineIdx;
		token = nextToken();
		idx = old;
		lineIdx = oldl;
		for (Token tt : list)
			if (tt == token)
				return true;
		return false;
	}

	/** 下一个词素必须为指定类型, 不是则报错 */
	void mustbe(Token tt) {
		token = nextToken();
		if (token != tt) {
			tokenValue = lineIdx + ": " + "不是期待的词素类型 " + tt.toString();
			token = Token.Error;
			System.out.println(tokenValue + "\n" + token);
			throw new Error();
		}
	}

	/** 下一个词素或许为指定类型, 不是则回退 */
	boolean maybe(Token tt) {
		int old = idx, oldl = lineIdx;
		token = nextToken();
		if (token == tt)
			return true;
		idx = old;
		lineIdx = oldl;
		return false;
	}

	/** 下一个词素或许为指定类型之一, 不是则回退 */
	boolean maybeOneof(Token[] list) {
		int old = idx, oldl = lineIdx;
		token = nextToken();
		for (Token tt : list)
			if (tt == token)
				return true;
		idx = old;
		lineIdx = oldl;
		return false;
	}

	Program root;

	// {importDef}	{typeDef}
	/** 语法解析: 生成语法树 */
	public Ast parse() {
		ArrayList<Import> imports = new ArrayList<>();
		ArrayList<ClassDef> classes = new ArrayList<>();
		ArrayList<EnumDef> enums = new ArrayList<>();
		idx = 0;
		lineIdx = 1;
		root = new Program();
		do {
			if (maybe(Token.Import))
				imports.add(parseImport());
			else if (maybe(Token.Class))
				classes.add(parseClassDef());
			else if (maybe(Token.Enum))
				enums.add(parseEnumDef());
			else
				nextToken();
		} while (token != Token.End && token != Token.Error);

		root.imports = imports.toArray(new Import[0]);
		root.classes = classes.toArray(new ClassDef[0]);
		root.enums = enums.toArray(new EnumDef[0]);
		return root;
	}

	// import id {. id} ;
	/* 解析-类型导入 */
	Import parseImport() {
		ArrayList<Operand> ids = new ArrayList<>();

		mustbe(Token.Identifier);
		ids.add(new Operand(token, tokenValue));
		while (maybe(Token.Dot)) {
			mustbe(Token.Identifier);
			ids.add(new Operand(token, tokenValue));
		}
		mustbe(Token.SemiColon);

		Import i = new Import();
		i.ids = ids.toArray(new Operand[0]);
		return i;
	}

	// class id [extends id] [implements id {, id}] { {varDef|funcDef} }
	/** 解析-类定义 */
	ClassDef parseClassDef() {
		ClassDef c = new ClassDef();
		ArrayList<VarDef> vars = new ArrayList<>();
		ArrayList<FuncDef> funcs = new ArrayList<>();

		mustbe(Token.Identifier);
		c.id = new Operand(token, tokenValue); //类标识
		//父类标识
		if (maybe(Token.Extends)) {
			mustbe(Token.Identifier);
			c.parentId = new Operand(token, tokenValue);
		}
		//接口标识
		if (maybe(Token.Implements)) {
			ArrayList<Operand> ls = new ArrayList<>();
			mustbe(Token.Identifier);
			ls.add(new Operand(token, tokenValue));
			while (maybe(Token.Dot)) {
				mustbe(Token.Identifier);
				ls.add(new Operand(token, tokenValue));
			}
			c.interfaceIds = ls.toArray(new Operand[0]);
		}
		mustbe(Token.BraceL);
		while (!maybe(Token.BraceR)) {
			int old = idx;
			mustbe(Token.Identifier);
			mustbe(Token.Identifier);
			//解析函数定义
			if (maybe(Token.ParenL)) {
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

	// enum id { (id = num ,) }
	/** 解析枚举定义 */
	EnumDef parseEnumDef() {
		EnumDef e = new EnumDef();
		mustbe(Token.Identifier);
		e.id = new Operand(token, tokenValue);
		ArrayList<EnumItem> items = new ArrayList<>();
		mustbe(Token.BraceL);
		while (!maybe(Token.BraceR)) {
			EnumItem item = new EnumItem();
			mustbe(Token.Identifier);
			item.id = new Operand(token, tokenValue);
			if (maybe(Token.Assign))
				item.value = parseOpBinary();
			mustbe(Token.Comma); //每项后必须有逗号, 包括最后一项
			items.add(item);
		}
		e.items = items.toArray(new EnumItem[0]);
		return e;
	}

	// type id [= exp] {, id [= exp]} ;
	/** 解析-变量定义 */
	VarDef[] parseVardef() {
		ArrayList<VarDef> vs = new ArrayList<>();

		mustbe(Token.Identifier);
		Operand type = new Operand(token, tokenValue);
		do {
			VarDef vd = new VarDef();
			vd.type = type;
			mustbe(Token.Identifier);
			vd.id = new Operand(token, tokenValue);
			if (maybe(Token.Assign)) {
				vd.value = parseOpBinary();
			}
			vs.add(vd);
		} while (maybe(Token.Comma));

		mustbe(Token.SemiColon);
		return vs.toArray(new VarDef[0]);
	}

	// type id ( [parameter] ) block
	/** 解析-函数定义 */
	FuncDef parseFuncDef() {
		FuncDef fd = new FuncDef();
		ArrayList<VarDef> ps = new ArrayList<>();

		mustbe(Token.Identifier);
		fd.type = new Operand(token, tokenValue);
		mustbe(Token.Identifier);
		fd.id = new Operand(token, tokenValue);
		mustbe(Token.ParenL);
		if (!maybe(Token.ParenR)) { //是否无参数
			// 参数解析 typeid id = exp { , typeid id [= exp]} 
			do {
				VarDef vd = new VarDef();
				mustbe(Token.Identifier);
				vd.type = new Operand(token, tokenValue);
				mustbe(Token.Identifier);
				vd.id = new Operand(token, tokenValue);
				if (maybe(Token.Assign))
					vd.value = parseOpBinary();
				ps.add(vd);
			} while (maybe(Token.Comma));
			fd.parameters = ps.toArray(new VarDef[0]);
			mustbe(Token.ParenR);
		}
		fd.block = parseBlock();
		return fd;
	}

	// { {varDef|stmt|block|exp} }
	/** 解析-代码块 */
	Block parseBlock() {
		Block b = new Block();
		ArrayList<Ast> vsbe = new ArrayList<>();

		if (maybe(Token.BraceL))
			while (!maybe(Token.BraceR)) {
				Object item = parseStmts();
				if (item instanceof VarDef[])
					for (VarDef v : (VarDef[]) item)
						vsbe.add(v);
				else
					vsbe.add((Ast) item);
			}
		else {
			Object item = parseStmts();
			if (item instanceof VarDef[])
				for (VarDef v : (VarDef[]) item)
					vsbe.add(v);
			else
				vsbe.add((Ast) item);
		}
		b.vsbe = vsbe.toArray(new Ast[0]);
		return b;
	}

	/** 解析每条语句 */
	Object parseStmts() {
		peekToken();
		if (token == Token.BraceL)
			return parseBlock();
		else if (token == Token.If)
			return parseStmtIf();
		else if (token == Token.While)
			return parseStmtWhile();
		else if (token == Token.Do)
			return parseStmtDowhile();
		else if (token == Token.For)
			return parseStmtFor();
		else if (token == Token.Continue) {
			mustbe(Token.Continue);
			mustbe(Token.SemiColon);
			return new StmtContinue();
		} else if (token == Token.Break) {
			mustbe(Token.Break);
			mustbe(Token.SemiColon);
			return new StmtBreak();
		} else if (token == Token.Return) {
			nextToken();
			Ast exp = null;
			if (!isbe(Token.SemiColon)) {
				exp = parseExp();
				mustbe(Token.SemiColon);
			}
			return new StmtReturn(exp);
		}
		//空语句
		else if (token == Token.SemiColon) {
			mustbe(Token.SemiColon);
			return new StmtNull();
		}
		//连续两个为id, 认为是变量定义
		else if (token == Token.Identifier && isbe(Token.Identifier, 1)) {
			return parseVardef(); //返回数组
		}
		//表达式语句
		StmtExp stmtexp = new StmtExp();
		stmtexp.exp = parseExp();
		mustbe(Token.SemiColon);
		return stmtexp;
	}

	StmtIf parseStmtIf() {
		StmtIf ifs = new StmtIf();
		mustbe(Token.If);
		mustbe(Token.ParenL);
		ifs.cond = parseExp();
		mustbe(Token.ParenR);
		ifs.isTrue = parseBlock();
		if (maybe(Token.Else))
			ifs.isFalse = parseBlock();
		return ifs;
	}

	StmtWhile parseStmtWhile() {
		StmtWhile whi = new StmtWhile();
		mustbe(Token.While);
		mustbe(Token.ParenL);
		whi.cond = parseExp();
		mustbe(Token.ParenR);
		whi.block = parseBlock();
		return whi;
	}

	StmtDowhile parseStmtDowhile() {
		StmtDowhile dowhi = new StmtDowhile();
		mustbe(Token.Do);
		dowhi.block = parseBlock();
		mustbe(Token.While);
		mustbe(Token.ParenL);
		dowhi.cond = parseExp();
		mustbe(Token.ParenR);
		maybe(Token.SemiColon); //dowhile()后的分号可写可不写
		return dowhi;
	}

	// for(变量定义 或者 表达式 ; 表达式 ; 表达式) 与 for(变量定义 : 枚举变量)
	// loop(变量标识符; 初始值(默认0) : 终止值 : 步进值(默认1))
	StmtFor parseStmtFor() {
		StmtFor sfor = new StmtFor();
		mustbe(Token.For);
		mustbe(Token.ParenL);
		peekToken();
		if (token == Token.Identifier && isbe(Token.Identifier, 1))
			sfor.vars = parseVardef();
		else {
			sfor.init = parseExp();
			mustbe(Token.SemiColon);
		}
		sfor.cond = parseExp();
		mustbe(Token.SemiColon);
		sfor.step = parseExp();
		mustbe(Token.ParenR);
		sfor.block = parseBlock();
		return sfor;
	}

	// item {binaryOp item}
	/** 解析出并列操作符(左到右) */
	Ast parseExp() {
		Ast item = parseOpBinary();
		if (!isbe(Token.Comma))
			return item;

		ArrayList<Ast> list = new ArrayList<>();
		list.add(item);
		while (maybe(Token.Comma))
			list.add(parseOpBinary());

		OpComma exp = new OpComma();
		exp.items = list.toArray(new Ast[0]);
		return exp;
	}

	/** 解析出二元操作符(左到右) */
	Ast parseOpBinary() {
		ArrayList<Ast> stack = new ArrayList<>();
		stack.add(parseOpUnary());
		if (maybeOneof(Tokens.binaryOps)) {
			stack.add(new Op(token));
			stack.add(parseOpUnary());
		} else
			return stack.get(0);

		int endidx;
		OpBinary bin;
		while (maybeOneof(Tokens.binaryOps)) {
			endidx = stack.size() - 1;
			Op op1 = (Op) stack.get(endidx - 1);
			Op op2 = new Op(token);
			//如果op1的优先权高 或者 op1和op2相同并且是左结合 - 则优先合并栈中对象
			if (op1.high(op2) || op1.same(op2) && !op1.isOneof(Tokens.assignOps)) {
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
		if (maybeOneof(Tokens.unaryPreOps)) {
			OpUnaryPre op = new OpUnaryPre();
			op.op = token;
			op.operand = parseItem();
			return op;
		}
		Ast i = parseItem();
		//是否后缀
		if (maybeOneof(Tokens.unaryPostOps)) {
			OpUnaryPost op = new OpUnaryPost();
			op.op = token;
			op.operand = i;
			return op;
		}
		return i;
	}

	/** 解析出对象操作符 () [] . */
	Ast parseItem() {
		Ast f = parseFactor();
		while (true) {
			//id ( exp )
			if (maybe(Token.ParenL)) {
				OperandCall call = new OperandCall();
				call.id = f;
				call.paras = parseExp();
				mustbe(Token.ParenR);
				f = call;
			}
			//id [ exp ]
			else if (maybe(Token.BrackL)) {
				OperandIdx idx = new OperandIdx();
				idx.id = f;
				idx.sub = parseOpBinary();
				mustbe(Token.BrackR);
				f = idx;
			}
			//id . id
			else if (maybe(Token.Dot)) {
				mustbe(Token.Identifier);
				OperandMember mem = new OperandMember();
				mem.id = f;
				mem.idMember = new Operand(token, tokenValue);
				f = mem;
			} else
				break;
		}
		return f;
	}

	/** 解析出操作数 */
	Ast parseFactor() {
		//num / str / id
		if (maybeOneof(Tokens.operands))
			return new Operand(token, tokenValue);
		// ( exp )
		if (maybe(Token.ParenL)) {
			Ast item = parseExp();
			// ( exp ? exp : exp )		//三元 条件运算符
			if (maybe(Token.Question)) {
				OpTri tri = new OpTri();
				tri.cond = item;
				tri.isTrue = parseOpBinary();
				mustbe(Token.Colon);
				tri.isFalse = parseOpBinary();
				mustbe(Token.ParenR);
				return tri;
			}
			mustbe(Token.ParenR);
			return item;
		}
		return null;
	}

	void showTokens() {
		Token tt;
		do {
			tt = nextToken();
			System.out.println(tokenValue + " \t:" + tt.toString() + " \t " + idx + "," + lineIdx);
		} while (tt != Token.End && tt != Token.Error);
		idx = 0;
		lineIdx = 1;
	}

	/** c代码文件生成 */
	void Trnaslaate() {
	}
}

abstract class Ast {
	/** 词素映射到字符串 */
	String opCode(Token op) {
		return Tokens.presetInv.get(op);
	}

	/** 打印出语法树 */
	void tree(String tab) {
	}

	/** 还原出源码 */
	String code(String tab) {
		return "";
	}
}

/** 类型转换或者函数调用操作符() */
class OperandCall extends Ast {
	Ast id;
	Ast paras; //参数表达式, (当参数为类型id时(id的首字母大写)为类型转换操作, 否则就是函数调用)

	void tree(String tab) {
		id.tree(tab);
		System.out.println(tab + "(");
		paras.tree(tab + "\t");
		System.out.println(tab + ")");
	}

	String code(String tab) {
		return id.code(tab) + "(" + paras.code(tab) + ")";
	}
}

/** 下标操作符[] */
class OperandIdx extends Ast {
	Ast id;
	Ast sub; //下标表达式

	void tree(String tab) {
		id.tree(tab);
		System.out.println(tab + "[");
		sub.tree(tab + "\t");
		System.out.println(tab + "]");
	}

	String code(String tab) {
		return id.code(tab) + "[" + sub.code(tab) + "]";
	}
}

/** 成员操作符. */
class OperandMember extends Ast {
	Ast id;
	Ast idMember;

	void tree(String tab) {
		id.tree(tab);
		System.out.println(tab + "." + idMember.code(tab));
	}

	String code(String tab) {
		return id.code(tab) + "." + idMember.code(tab);
	}
}

/** 操作数 */
class Operand extends Ast {
	String value;
	Token type; //操作数类型

	Operand(Token type, String str) {
		this.type = type;
		value = str;
	}

	void tree(String tab) {
		System.out.println(tab + value);
	}

	String code(String tab) {
		if (type == Token.StringLiteral)
			return "\"" + value + "\"";
		return value;
	}
}

/** 操作符 */
class Op extends Ast {
	public Token op;

	Op(Token op) {
		this.op = op;
	}

	boolean high(Op op) {
		return Tokens.priority.get(this.op) > Tokens.priority.get(op.op);
	}

	boolean same(Op op) {
		return Tokens.priority.get(this.op) == Tokens.priority.get(op.op);
	}

	boolean isOneof(Token[] list) {
		for (Token tt : list)
			if (tt == op)
				return true;
		return false;
	}
}

/** 并列操作符, */
class OpComma extends Ast {
	Ast[] items;

	void tree(String tab) {
		for (Ast item : items)
			item.tree(tab);
	}

	String code(String tab) {
		String out = "";
		int i = 0;
		for (Ast item : items)
			out += item.code(tab) + (++i == items.length ? "" : ", ");
		return out;
	}
}

/** 三元操作符?: */
class OpTri extends Ast {
	Ast cond, isTrue, isFalse;

	void tree(String tab) {
		cond.tree(tab + "\t");
		System.out.println("?");
		isTrue.tree(tab + "\t");
		System.out.println(":");
		isFalse.tree(tab + "\t");
	}

	String code(String tab) {
		return "(" + cond.code(tab) + "?" + isTrue.code(tab) + ":" + isFalse.code(tab) + ")";
	}
}

/** 二元操作符 和 赋值操作符 */
class OpBinary extends Ast {
	Token op; //操作符
	Ast left;
	Ast right;

	OpBinary(Ast left) {
		this.left = left;
	}

	void tree(String tab) {
		if (left != null)
			left.tree(tab + "\t");
		if (op != null) {
			System.out.println(tab + op.toString());
			right.tree(tab + "\t");
		}
	}

	String code(String tab) {
		return left.code(tab) + opCode(op) + right.code(tab);
	}
}

/** 一元操作符(前缀) */
class OpUnaryPre extends Ast {
	Token op; //操作符
	Ast operand;

	void tree(String tab) {
		System.out.println(tab + op);
		operand.tree(tab);
	}

	String code(String tab) {
		return opCode(op) + operand.code(tab);
	}
}

/** 一元操作符(后缀/无) */
class OpUnaryPost extends Ast {
	Token op; //操作符
	Ast operand;

	void tree(String tab) {
		operand.tree(tab);
		if (op != Token.None)
			System.out.println(tab + "\t" + op);
	}

	String code(String tab) {
		return operand.code(tab) + opCode(op);
	}
}

class Program extends Ast {
	Import[] imports;
	ClassDef[] classes;
	InterfaceDef[] interfaces;
	EnumDef[] enums;

	void tree(String tab) {
		System.out.println("\nprogram:");
		if (imports != null)
			for (Import i : imports)
				i.tree(tab);
		if (classes != null)
			for (ClassDef c : classes)
				c.tree(tab);
		if (interfaces != null)
			for (InterfaceDef i : interfaces)
				i.tree(tab);
		if (enums != null)
			for (EnumDef e : enums)
				e.tree(tab);
	}

	String code(String tab) {
		String out = "file: path\n";
		if (imports != null)
			for (Import i : imports)
				out += i.code(tab) + "\n";
		if (classes != null)
			for (ClassDef c : classes)
				out += c.code(tab) + "\n";
		if (interfaces != null)
			for (InterfaceDef i : interfaces)
				out += i.code(tab) + "\n";
		if (enums != null)
			for (EnumDef e : enums)
				out += e.code(tab) + "\n";
		return out;
	}
}

class Import extends Ast {
	Operand[] ids;

	void tree(String tab) {
		int i = 0;
		System.out.print("import ");
		for (Operand id : ids)
			System.out.print(id.value + (++i == ids.length ? ";" : "."));
		System.out.println();
	}

	String code(String tab) {
		String out = "import ";
		int i = 0;
		for (Operand id : ids)
			out += (id.value + (++i == ids.length ? ";" : "."));
		return out;
	}
}

/** 代码块, 允许嵌套 */
class Block extends Ast {
	Ast[] vsbe; //局部变量, 语句,代码块,表达式

	void tree(String tab) {
		System.out.println(tab + "{");
		for (Ast s : vsbe)
			s.tree(tab + "\t");
		System.out.println(tab + "}");
	}

	String code(String tab) {
		String out = "\n" + tab + "{\n";
		for (Ast a : vsbe)
			if (a instanceof VarDef)
				out += tab + "\t" + a.code(tab + "\t") + ";\n";
			else
				out += a.code(tab + "\t") + "\n";
		out += "\n" + tab + "}\n";
		return out;
	}
}

class ClassDef extends Ast {
	Operand id;
	Operand parentId; //父类型id
	Operand[] interfaceIds; //接口类型id
	VarDef[] vars;
	FuncDef[] funcs;

	void tree(String tab) {
		System.out.println(tab + "class " + id.value);
		if (parentId != null)
			System.out.println(tab + "\textends " + parentId.value);
		System.out.println(tab + "{");
		for (VarDef v : vars)
			v.tree(tab + "\t");
		for (FuncDef f : funcs)
			f.tree(tab + "\t");
		System.out.println(tab + "}");
	}

	String code(String tab) {
		String out = "\nclass " + id.value + (parentId != null ? " extends " + parentId.value : "") + " {\n";
		for (VarDef v : vars)
			out += tab + "\t" + v.code(tab + "\t") + ";\n";
		System.out.println();
		for (FuncDef f : funcs)
			out += f.code(tab + "\t") + "\n\n";
		out += "}";
		return out;
	}
}

class InterfaceDef extends Ast {
	FuncDel[] funcs;
}

class EnumDef extends Ast {
	Operand id;
	EnumItem[] items;

	String code(String tab) {
		String out = tab + "enum " + id.value + " {\n";
		for (EnumItem i : items)
			out += tab + "\t" + i.id.value + (i.value != null ? " = " + i.value.code(tab) : "") + ",\n";
		out += tab + "}\n";
		return out;
	}
}

class EnumItem {
	Operand id;
	Ast value;
}

/** 变量定义 */
class VarDef extends Ast {
	Operand id;
	Operand type; //变量类型
	Ast value; //初始化的表达式

	void tree(String tab) {
		System.out.println(tab + type.value + " " + id.value + (value != null ? "=" : ""));
		if (value != null)
			value.tree(tab + "\t");
	}

	String code(String tab) {
		return type.value + " " + id.value + (value != null ? "=" + value.code(tab) : "");
	}
}

/** 函数定义 */
class FuncDef extends Ast {
	Operand id;
	VarDef[] parameters; //函数参数
	Operand type; //返回类型
	Block block;

	void tree(String tab) {
		System.out.println(tab + type.value + " " + id.value + "(");
		for (VarDef v : parameters)
			v.tree(tab + "\t");
		System.out.println(tab + ")");
		block.tree(tab);
	}

	String code(String tab) {
		String out = tab + type.value + " " + id.value + "(";
		int i = 0;
		for (VarDef v : parameters)
			out += v.code(tab) + (++i == parameters.length ? "" : ", ");
		out += ")" + block.code(tab);
		return out;
	}
}

/** 函数类型定义 */
class FuncDel extends Ast {
}

class StmtIf extends Ast {
	Ast cond;
	Block isTrue, isFalse;

	String code(String tab) {
		return tab + "if(" + cond.code(tab) + ")" + isTrue.code(tab)
				+ (isFalse != null ? "else" + isFalse.code(tab) : "");
	}
}

class StmtWhile extends Ast {
	Ast cond;
	Block block;

	String code(String tab) {
		return tab + "while(" + cond.code(tab) + ")" + block.code(tab);
	}
}

class StmtDowhile extends Ast {
	Ast cond;
	Block block;

	String code(String tab) {
		return tab + "do" + block.code(tab) + tab + "while(" + cond.code(tab) + ")\n";
	}
}

class StmtFor extends Ast {
	Ast init, cond, step;
	VarDef[] vars; //所有变量应该为相同类型
	Block block;

	String code(String tab) {
		String out = tab + "for(";
		if (init == null) {
			out += vars[0].type.value + " ";
			int i = 0;
			for (VarDef v : vars)
				out += v.id.value + (v.value != null ? "=" + v.value.code(tab) : "") + (++i == vars.length ? "" : ",");
		} else
			out += init.code(tab);
		out += "; " + cond.code(tab) + "; " + step.code(tab) + ")" + block.code(tab);
		return out;
	}
}

class StmtBreak extends Ast {
	String code(String tab) {
		return tab + "break;";
	}
}

class StmtContinue extends Ast {
	String code(String tab) {
		return tab + "continue;";
	}
}

class StmtReturn extends Ast {
	Ast exp;

	StmtReturn(Ast exp) {
		this.exp = exp;
	}

	String code(String tab) {
		return tab + "return " + (exp != null ? exp.code(tab) : "") + ";";
	}
}

/** 表达式语句 带分号的表达式 */
class StmtExp extends Ast {
	Ast exp;

	String code(String tab) {
		return tab + exp.code(tab) + ";";
	}
}

/** 空语句 */
class StmtNull extends Ast {
	String code(String tab) {
		return tab + ";";
	}
}
