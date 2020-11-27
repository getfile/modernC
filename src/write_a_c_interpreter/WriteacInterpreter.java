package write_a_c_interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import util.FileUtil;

public class WriteacInterpreter {

	public static void main(String[] args) {
		// new WriteacInterpreter("src/write_a_c_interpreter/hello.c").parse();
		new WriteacInterpreter("src/write_a_c_interpreter/hello.c").parseExpr();
	}

	static HashMap<String, Token> reserve = new HashMap<>(); //保留字集
	static Token tokenEnd = new Token(TokenKind.End, "end");
	static Token tokenError = new Token(TokenKind.Error, null);
	// 初始化保留字集
	{
		reserve.put("enum", new Token(TokenKind.Enum, "enum"));
		reserve.put("int", new Token(TokenKind.Int, "int"));
		reserve.put("char", new Token(TokenKind.Char, "char"));
		reserve.put("if", new Token(TokenKind.If, "if"));
		reserve.put("else", new Token(TokenKind.Else, "else"));
		reserve.put("while", new Token(TokenKind.While, "while"));
		reserve.put("return", new Token(TokenKind.Return, "return"));
		reserve.put("=", new Token(TokenKind.Assign, "="));
		reserve.put("?", new Token(TokenKind.Cond, "?"));
		reserve.put("||", new Token(TokenKind.Lor, "||"));
		reserve.put("&&", new Token(TokenKind.Land, "&&"));
		reserve.put("|", new Token(TokenKind.Or, "|"));
		reserve.put("^", new Token(TokenKind.Xor, "^"));
		reserve.put("&", new Token(TokenKind.And, "&"));
		reserve.put("==", new Token(TokenKind.Eq, "=="));
		reserve.put("!=", new Token(TokenKind.Ne, "!="));
		reserve.put("<", new Token(TokenKind.Lt, "<"));
		reserve.put(">", new Token(TokenKind.Gt, ">"));
		reserve.put("<=", new Token(TokenKind.Le, "<="));
		reserve.put(">=", new Token(TokenKind.Ge, ">="));
		reserve.put("<<", new Token(TokenKind.Shl, "<<"));
		reserve.put(">>", new Token(TokenKind.Shr, ">>"));
		reserve.put("+", new Token(TokenKind.Add, "+"));
		reserve.put("-", new Token(TokenKind.Sub, "-"));
		reserve.put("*", new Token(TokenKind.Mul, "*"));
		reserve.put("/", new Token(TokenKind.Div, "/"));
		reserve.put("%", new Token(TokenKind.Mod, "%"));
		reserve.put("++", new Token(TokenKind.Inc, "++"));
		reserve.put("--", new Token(TokenKind.Dec, "--"));
		reserve.put("(", new Token(TokenKind.PareL, "("));
		reserve.put(")", new Token(TokenKind.PareR, ")"));
		reserve.put("[", new Token(TokenKind.BrackL, "["));
		reserve.put("]", new Token(TokenKind.BrackR, "]"));
		reserve.put("{", new Token(TokenKind.BraceL, "{"));
		reserve.put("}", new Token(TokenKind.BraceR, "}"));
		reserve.put(":", new Token(TokenKind.Colon, ":"));
		reserve.put("#", new Token(TokenKind.Sharp, "#"));
		reserve.put(".", new Token(TokenKind.Dot, "."));
		reserve.put(";", new Token(TokenKind.SemiColon, ";"));
		reserve.put(",", new Token(TokenKind.Comma, ","));
	}

	WriteacInterpreter(String src, boolean ok) {
		this.src = src;
		srcLength = src.length();
	}

	WriteacInterpreter(String filename) {
		src = FileUtil.getString(filename);
		srcLength = src.length();
	}

	String src;
	int srcLength;
	int srcIdx;
	int srcIdxPre;
	Ast root; //语法树的根节点;

	boolean isAlpha(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	boolean isWhite(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	/** 回退一个标识 */
	void lexisBack() {
		srcIdx = srcIdxPre;
	}

	/** 解析一个标识 */
	Token lexis() {
		int i;
		while (true) {
			if (srcIdx >= srcLength)
				return tokenEnd;

			srcIdxPre = srcIdx;

			char c = src.charAt(srcIdx);
			if (isWhite(c)) {
				srcIdx++;
				continue;
			}
			//解析行注释标记
			else if (c == '/' && src.charAt(srcIdx + 1) == '/') {
				for (i = srcIdx + 2; i < srcLength; i++)
					if (src.charAt(i) == '\n') {
						i++;
						break;
					}
				srcIdx = i;
			}
			//解析块注释标记
			else if (c == '/' && src.charAt(srcIdx + 1) == '*') {
				for (i = srcIdx + 2; i < srcLength; i++)
					if (i < srcLength - 1 && src.charAt(i) == '*' && src.charAt(i + 1) == '/') {
						i += 2;
						break;
					}
				srcIdx = i;
			}
			//解析标识符标记
			else if (c == '_' || isAlpha(c)) {
				for (i = srcIdx + 1; i < srcLength; i++) {
					c = src.charAt(i);
					if (c != '_' && !isAlpha(c) && !isNumber(c))
						break;
				}
				String key = src.substring(srcIdx, i);
				srcIdx = i;
				if (reserve.containsKey(key))
					return reserve.get(key);
				return new Token(TokenKind.Identifier, key);
			}
			//解析数字标记
			else if (isNumber(c)) {
				int from = srcIdx;
				for (; srcIdx < srcLength; srcIdx++) //整数部分
					if (!isNumber(src.charAt(srcIdx)))
						break;
				if (srcIdx >= srcLength)
					return new Token(TokenKind.NumberLiteral, src.substring(from));

				if (src.charAt(srcIdx) == '.') //小数部分
					for (srcIdx++; srcIdx < srcLength; srcIdx++)
						if (!isNumber(src.charAt(srcIdx)))
							break;
				if (srcIdx >= srcLength)
					return new Token(TokenKind.NumberLiteral, src.substring(from));

				if (src.charAt(srcIdx) == 'e') { //指数部分
					srcIdx++;
					if (src.charAt(srcIdx) == '+' || src.charAt(srcIdx) == '-')
						srcIdx++;
					for (srcIdx++; srcIdx < srcLength; srcIdx++)
						if (!isNumber(src.charAt(srcIdx)))
							break;
					if (srcIdx >= srcLength)
						return new Token(TokenKind.NumberLiteral, src.substring(from));
				}
				return new Token(TokenKind.NumberLiteral, src.substring(from, srcIdx));
			}
			//解析字符串标记
			else if (c == '"') {
				for (i = srcIdx + 1; i < srcLength; i++) {
					c = src.charAt(i);
					if (c == '"')
						break;
					if (c == '\\')
						i++;
				}
				String s = src.substring(srcIdx + 1, i);
				srcIdx = i + 1;
				return new Token(TokenKind.StringLiteral, s);
			}
			//解析标点符号标记
			else {
				String k = src.substring(srcIdx, srcIdx + 2); //双字符标点符号
				if (reserve.containsKey(k)) {
					srcIdx += 2;
					return reserve.get(k);
				}
				k = c + ""; //单字符标点符号
				if (reserve.containsKey(k)) {
					srcIdx++;
					return reserve.get(k);
				}
				tokenError.value = "无法识别的标记 " + c; //无法识别的标点符号
				tokenError.idx = srcIdx;
				return tokenError;
			}
		}

	}

	Ast syntax() {
		return null;
	}

	void astProgram() {
		root = new AstProgram();
		do {
			Token t = lexis();
			if (t == tokenEnd || t == tokenError)
				break;
			root.add(astGlobalDecl());
		} while (true);
	}

	Ast astGlobalDecl() {
		Token t = lexis();
		// if (t.kind == TokenKind.Enum) {
		// 	AstEnum ast;
		// 	t = lexis();
		// 	if (t.kind == TokenKind.BraceL)
		// 		ast = new AstEnum(null);
		// 	else {
		// 		ast = new AstEnum(t);
		// 		lexis();
		// 	}
		// 	//get values...
		// 	return ast;
		// }
		if (t.kind == TokenKind.Identifier) {
			t = lexis();
			Token id;
			if (t.kind == TokenKind.Mul)
				id = lexis();
			else
				id = t;
			Token vf = lexis();
			// func 声明
			if (vf.kind == TokenKind.PareL) {

			}
			// var 声明
			else {

			}

		}
		return null;
	}

	/** 解析复合语句 */
	Ast astStatms() {
		Token t = lexis();
		if (t.kind == TokenKind.SemiColon)
			return new Ast();

		else if (t.kind == TokenKind.BraceL) {
			ArrayList<Ast> ss = new ArrayList<>();
			while (true) {
				ss.add(astStatms());
				t = lexis();
				if (t.kind == TokenKind.BraceR)
					break;
				lexisBack();
			}
			return new AstStatms(ss);

		} else if (t.kind == TokenKind.If) {
			Ast astif = new AstStatmIf(astExpression());
			astif.add(astStatms());
			return astif;

		} else if (t.kind == TokenKind.While) {
			Ast astwhile = new AstStatmWhile(astExpression());
			astwhile.add(astStatms());
			return astwhile;

		} else if (t.kind == TokenKind.Return) {
			return new AstStatmReturn(astExpression());

		} else {
			Ast e = astExpression();
			t = lexis();
			if (t.kind == TokenKind.SemiColon)
				return new AstStatmExp(e);
			lexisBack();
		}

		lexisBack();
		return new AstError(tokenError, "未解析出语句");
	}

	/**解析二元运算符 + - */
	Ast astExpression() {
		Ast left = astOpBinary();
		while (true) {
			Token t = lexis();
			if (t.kind == TokenKind.Add)
				left = new AstBinaryOp(Op.Add, left, astOpBinary());
			else if (t.kind == TokenKind.Sub)
				left = new AstBinaryOp(Op.Sub, left, astOpBinary());
			else {
				lexisBack();
				break;
			}
		}
		return left;
	}

	/** 解析二元运算符 * / */
	Ast astOpBinary() {
		Ast left = astOpUnary();
		while (true) {
			int oldidx = srcIdx;
			Token t = lexis();
			if (t.kind == TokenKind.Mul)
				left = new AstBinaryOp(Op.Mul, left, astOpUnary());
			else if (t.kind == TokenKind.Div)
				left = new AstBinaryOp(Op.Div, left, astOpUnary());
			else {
				srcIdx = oldidx;
				break;
			}
		}
		return left;
	}

	/** 解析一元运算符 - */
	Ast astOpUnary() {
		Token t = lexis();
		if (t.kind == TokenKind.Sub)
			return new AstUnaryOp(astFactor());

		lexisBack();
		return astFactor();
	}

	/** 解析()表达式或数值 */
	Ast astFactor() {
		Ast a = null;
		Token t = lexis();
		if (t.kind == TokenKind.PareL) {//'('
			a = astExpression();
			lexis(); //')'
		} else if (t.kind == TokenKind.NumberLiteral)
			a = new AstNumLiteral(t);
		else if (t.kind == TokenKind.Identifier)
			a = new AstIdentifier(t);
		else
			a = new AstError(t, "解析表达式出错");
		return a;
	}

	void parse() {
		srcIdx = 0;
		do {
			Token t = lexis();
			t.print();
			if (t == tokenEnd || t == tokenError)
				break;
		} while (true);
	}

	void parseExpr() {
		src = "10-2*-((-3.1415926)+-4)*-5-x/-9";
		srcLength = src.length();
		srcIdx = 0;
		astExpression().print("\t");
	}

}

/** 标记/词素类型 */
enum TokenKind {
	Error, //
	NumberLiteral, StringLiteral, Identifier, Punctuation, Comment, End, //
	Enum, Char, Int, //
	Return, If, Else, While, Sizeof, //
	Assign, Cond, Lor, Land, Or, Xor, And, Eq, Ne, Lt, Gt, Le, Ge, Shl, Shr, Add, Sub, Mul, Div, Mod, Inc, Dec, Brak,
	// =    ?:    ||   &&    |   ^    &    ==  !=  <   >   <=  >=  <<   >>   +    -    *    /    %    ++   --   [	 优先级从低到高 
	PareL, PareR, BrackL, BrackR, BraceL, BraceR, Colon, Sharp, Dot, SemiColon, Comma
	// (   )      [       ]       {       }       :      #      .    ;          ,
}

/* 标记/词素 */
class Token {
	TokenKind kind;
	String value;
	int idx; //位置

	Token(TokenKind kind, String val) {
		this.kind = kind;
		value = val;
	}

	void print() {
		System.out.println("类型: " + kind + " \t位置:" + idx + " \t" + value);
	}

	String str() {
		return ("类型: " + kind + " \t位置:" + idx + " \t" + value);
	}
}

/* 语法树节点 */
class Ast {
	Ast id;

	public Ast add(Ast node) {
		return this;
	}

	void print(String tab) {
		System.out.println(".");
	}

}

class AstProgram extends Ast {

}

class AstEnum extends Ast {
	AstEnum(Token t) {
	}

	AstVar[] vars;
}

class AstVar extends Ast {
	Ast type;
	Ast id;
	Ast value;

	AstVar(Token t) {
	}
}

class AstFunc extends Ast {
	AstFunc(Token t) {
	}

	Ast type;
	Ast params;
	Ast codes;
}

/** 复合语句 */
class AstStatms extends Ast {
	ArrayList<AstVar> vars = new ArrayList<>();
	ArrayList<Ast> statms = new ArrayList<>();

	AstStatms(ArrayList<Ast> ss) {
		for (Ast a : ss)
			statms.add(a);
	}

	public Ast add(Ast stm) {
		statms.add(stm);
		return this;
	}

	void print(String tab) {
		for (Ast s : statms)
			s.print(tab);
	}
}

class AstStatmIf extends AstStatms {
	Ast exp;

	AstStatmIf(Ast exp) {
		super(null);
		this.exp = exp;
	}

	public Ast add(Ast statm) {
		statms.add(statm);
		return this;
	}

	void print(String tab) {
		System.out.println("if");
		// tab += "\t";
		exp.print(tab);
		super.print(tab);
	}
}

class AstStatmWhile extends AstStatms {
	Ast exp;

	AstStatmWhile(Ast exp) {
		super(null);
		this.exp = exp;
	}

	public Ast add(Ast statm) {
		statms.add(statm);
		return this;
	}

	void print(String tab) {
		System.out.println("while");
		// tab += "\t";
		exp.print(tab);
		super.print(tab);
	}
}

class AstStatmReturn extends Ast {
	Ast exp;

	AstStatmReturn(Ast exp) {
		this.exp = exp;
	}

	void print(String tab) {
		System.out.println("return");
		// tab += "\t";
		exp.print(tab);
	}
}

class AstStatmExp extends Ast {
	Ast exp;

	AstStatmExp(Ast e) {
		exp = e;
	}

	void print(String tab) {
		exp.print(tab);
	}
}

enum Op {
	Add, Sub, Mul, Div
}

class AstBinaryOp extends Ast {
	Op op;
	Ast left, right;

	AstBinaryOp(Op op, Ast left, Ast right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

	void print(String tab) {
		tab += "\t";
		left.print(tab);
		System.out.println(tab + op.toString());
		right.print(tab);
	}
}

class AstUnaryOp extends Ast {
	Ast exp;

	AstUnaryOp(Ast a) {
		exp = a;
	}

	void print(String tab) {
		System.out.println(tab + "-");
		exp.print(tab);
	}
}

/** 标识符节点 */
class AstIdentifier extends Ast {

	Token id;

	AstIdentifier(Token t) {
		id = t;
	}

	void print(String tab) {
		System.out.println(tab + id.value);
	}
}

/** 数值常量节点 */
class AstNumLiteral extends Ast {
	Token num;

	AstNumLiteral(Token t) {
		num = t;
	}

	void print(String tab) {
		System.out.println(tab + num.value);
	}
}

/** 字符串常量节点 */
class AstStrLiteral extends Ast {
	Token str;

	AstStrLiteral(Token t) {
		str = t;
	}

}

class AstError extends Ast {
	Token t;
	String info;

	AstError(Token t, String info) {
		this.t = t;
		this.info = info;
	}

	void print(String tab) {
		System.out.println("error: " + info + " " + (t != null ? t.str() : ""));
	}
}