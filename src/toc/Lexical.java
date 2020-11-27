package toc;

/** 词法处理
 */
import java.util.ArrayList;
import java.util.HashMap;

/** 词素类型 */
enum TokenKind {
	Identifier, //标识符
	String, //字符串常量
	Number, //数字常量
	Punctuator, //标点符号
	Keyword, //关键字
	Error, //
	End //终点
}

/** 词素 */
class Token {
	TokenKind kind = TokenKind.Punctuator;
	String value = "";
	int line = 0;

	Token(String v, TokenKind k, int l) {
		this.value = v;
		this.kind = k;
		this.line = l;
	}

	boolean isIdentifier() {
		return this.kind == TokenKind.Identifier;
	}

	boolean isString() {
		return this.kind == TokenKind.String;
	}

	boolean isNumber() {
		return this.kind == TokenKind.Number;
	}

	boolean isPunc() {
		return this.kind == TokenKind.Punctuator;
	}

	boolean isEnd() {
		return this.kind == TokenKind.End;
	}

	/** 是否是指定的内容 */
	boolean is(String s) {
		return this.value == s;
	}

	/** 是否是指定内容的标识符 */
	boolean isIDStr(String str) {
		return this.kind == TokenKind.Identifier && value == str;
	}

	/* 是否是指定内容的标点符号 */
	boolean isPuncStr(String str) {
		return this.kind == TokenKind.Punctuator && value == str;
	}

	/* 期待类型为指定类型 */
	boolean expectKind(TokenKind kind) {
		return this.kind == kind;
	}

	void str() {
		System.out.println("kind:" + this.kind + "\tline:" + this.line + "\t" + this.value);
	}

}

/** 词法解析器 */
public class Lexical {

	HashMap<String, Integer> priority = new HashMap<>(); //操作符优先级

	String src; //源码
	int srcIdx; //源码索引
	int srcLength; //源码长度

	int line; //行数

	ArrayList<Token> tokens = new ArrayList<>(); //词素集
	int tokenIdx = 0;

	Lexical(String c) {
		src = c;
		srcLength = c.length();
	}

	/** 获取字符,并移动指针 (当前字符的后面第n个字符)*/
	public char charTake(int offs) {
		srcIdx += offs;
		if (srcIdx < srcLength)
			return src.charAt(srcIdx);
		return 0;
	}

	/** 获取字符,不移动指针 (当前字符的后面第n个字符)*/
	public char charPeek(int offs) {
		int idx = srcIdx + offs;
		if (idx < srcLength)
			return this.src.charAt(idx);
		return 0;
	}

	/** 解析出空白字符 */
	void lexWhitespace() {
		char c = this.charPeek(0);
		while (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
			if (c == '\n')
				this.line++;
			c = this.charTake(1);
		}
	}

	/** 解析出注释字符 */
	boolean lexComment() {
		char c = this.charPeek(0);
		if (c == '/' && this.charPeek(1) == '/') //行注释 //
			while (true) {
				c = this.charTake(1);
				if (c == '\n' || c == 0) {
					if (c == '\n')
						this.line++;
					this.charTake(1);
					return true;
				}
			}
		else if (c == '/' && this.charPeek(1) == '*') { //块注释 /*
			this.charTake(1);
			while (true) {
				c = this.charTake(1);
				if (c == '\n')
					this.line++;
				if (c == 0)
					return true;
				if (c != '*')
					continue;
				if (this.charPeek(1) == 0) {
					this.charTake(2);
					return true;
				}
				if (this.charPeek(1) == '/') {
					this.charTake(2);
					return true;
				}
			}
		}
		return false;
	}

	/** 解析出数值字符串 */
	boolean lexNumber() {
		char c = this.charPeek(0);
		int ids = this.srcIdx;
		if (c < '0' || c > '9' || c == 0)
			return false; //首字符必须为数字, 否则非数字类型

		c = this.charPeek(1);
		if (c == 'b') { //二进制解析
			this.charTake(1);
			do {
				c = this.charTake(1);
			} while (c == '0' || c == '1');
		} else if (c == 'o') { //八进制解析
			this.charTake(1);
			do {
				c = this.charTake(1);
			} while (c >= '0' && c <= '7');
		} else if (c == 'x') { //十六进制解析
			this.charTake(1);
			while (true) {
				c = this.charTake(1);
				if (c >= '0' && c <= '9')
					continue;
				if (c >= 'a' && c <= 'f')
					continue;
				if (c >= 'A' && c <= 'F')
					continue;
				break;
			}
		} else { //十进制解析
			do {
				c = this.charTake(1);
			} while (c >= '0' && c <= '9');
			if (c == '.') //小数部分
				do {
					c = this.charTake(1);
				} while (c >= '0' && c <= '9');
			if (c == 'e') { //指数部分
				if (this.charPeek(1) == '+' || this.charPeek(1) == '-')
					this.charTake(1);
				do {
					c = this.charTake(1);
				} while (c >= '0' && c <= '9');
			}
		}
		Token t = new Token(this.src.substring(ids, this.srcIdx), TokenKind.Number, this.line);
		this.tokens.add(t);
		return true;
	}

	/** 解析出字符字符串 */
	boolean lexString() {
		char c = this.charPeek(0);
		if (c != '"' && c != '\'')
			return false; //首字符必须是'或", 否则非字符串类型
		char cs = c;
		int ids = this.srcIdx;
		while (true) {
			c = this.charTake(1);
			if (c == 0)
				break; //出错: 超出长度
			if (c == cs)
				break;
			if (c == '\\')
				this.charTake(1); //如果是转义符,跳过下一个字符
		}
		this.charTake(1);
		Token t = new Token(this.src.substring(ids + 1, this.srcIdx - 1), TokenKind.String, this.line); //解析出字符串
		this.tokens.add(t);
		return true;
	}

	/** 解析出标识符字符串 */
	boolean lexIdentifier() {
		int ids = this.srcIdx;
		char c = this.charPeek(0);
		if (c != '_' && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z'))
			return false; //首字符必须为 _或字母, 否则非标识符类型
		while (true) {
			c = this.charTake(1);
			if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
				continue;
			break;
		}
		Token t = new Token(this.src.substring(ids, this.srcIdx), TokenKind.Identifier, this.line);
		this.tokens.add(t);
		return true;
	}

	/** 解析运算符, 必须在排除空白符,注释,字符串,数字,标识符后解析 */
	boolean lexPunc() {
		char c = this.charPeek(0), cn = this.charPeek(1);
		String key = "" + c + cn;
		Token t = null;
		if (cn != 0 && this.priority.containsKey(key)) { //两字符
			this.charTake(2);
			t = new Token(key, TokenKind.Punctuator, this.line);
		} else if (c != 0 && this.priority.containsKey(c + "")) { //一字符
			this.charTake(1);
			t = new Token(c + "", TokenKind.Punctuator, this.line);
		}

		this.charTake(1);
		t = new Token(c + "", TokenKind.Punctuator, this.line);
		this.tokens.add(t);
		return true;
	}

	Lexical parse() {
		this.tokens.clear();
		this.tokenIdx = 0;
		while (srcIdx < srcLength) {
			this.lexWhitespace();
			if (this.lexComment())
				continue;
			if (this.lexString())
				continue;
			if (this.lexNumber())
				continue;
			if (this.lexIdentifier())
				continue;
			if (this.lexPunc())
				continue;
		}
		return this;
	}

	/** 显示所有词素 */
	Lexical lexInfo() {
		for (Token t : this.tokens)
			t.str();
		return this;
	}

	/* 期待下一个词素为指定数值(str为空时, 为任意数值) */
	boolean expectNumber(String str, String error) {
		Token t = peek();
		if (t != null && t.isNumber())
			if (str == null || (str != null && t.is(str))) {
				next();
				return true;
			}
		if (error != null)
			System.out.println(error);
		return false;
	}

	/* 期待下一个词素为指定字符串(n为空时, 为任意数值) */
	boolean expectString(String str, String error) {
		Token t = peek();
		if (t != null && t.isNumber())
			if (str == null || (str != null && t.is(str))) {
				next();
				return true;
			}
		if (error != null)
			System.out.println(error);
		return false;
	}

	/** 期待下一个词素为指定标识符(str为null时, 为任意标识符) */
	boolean expectId(String str, String error) {
		Token t = peek();
		if (t != null && t.isIdentifier())
			if (str == null || (str != null && t.is(str))) {
				next();
				return true;
			}
		if (error != null)
			System.out.println(error);
		return false;
	}

	/** 期待下一个词素为指定标识符之一(strs不能为空) */
	boolean expectIds(String[] strs, String error) {
		Token t = peek();
		if (t != null && t.isIdentifier())
			for (String s : strs)
				if (t.is(s)) {
					next();
					return true;
				}
		if (error != null)
			System.out.println(error);
		return false;
	}

	/** 期待下一个词素为指定标点符(str为空时, 为任意标点符) */
	boolean expectPunc(String str, String error) {
		Token t = peek();
		if (t != null && t.isPunc())
			if (str == null || (str != null && t.is(str))) {
				next();
				return true;
			}
		if (error != null)
			System.out.println(error);
		return true;
	}

	/** 期待下一个词素为指定标点符之一(strs不能为空) */
	boolean expectPuncs(String[] strs, String error) {
		Token t = peek();
		if (t != null && t.isPunc())
			for (String s : strs)
				if (t.is(s)) {
					next();
					return true;
				}
		if (error != null)
			System.out.println(error);
		return true;
	}

	/** 后退一个词素 */
	void back() {
		tokenIdx--;
	}

	/** 获取下一个词素 */
	Token next() {
		tokenIdx++;
		if (tokenIdx < tokens.size())
			return tokens.get(tokenIdx);
		return null;
	}

	/** 查看下一个词素 */
	Token peek() {
		int idx = tokenIdx + 1;
		if (idx < tokens.size())
			return tokens.get(idx);
		return null;
	}

	/** 查看当前词素 */
	Token peekCurrent() {
		return (tokenIdx < tokens.size() ? tokens.get(tokenIdx) : null);
	}

	/** 查看某位置的词素 */
	Token peekPos(int offs) {
		int idx = tokenIdx + offs;
		if (idx < tokens.size())
			return tokens.get(idx);
		return null;
	}

}
