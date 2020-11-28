package build_a_simple_interpreter;

import java.util.HashMap;

public class Tokens {
	// 操作集
	static Token[] assignOps = { Token.Assign, Token.AssignAdd, Token.AssignSub, Token.AssignMul, Token.AssignDiv,
			Token.AssignMod };
	static Token[] binaryOps = { Token.Assign, Token.AssignAdd, Token.AssignSub, Token.AssignMul, Token.AssignDiv,
			Token.AssignMod, Token.Lor, Token.Land, Token.Bor, Token.Bxor, Token.Band, Token.Eq, Token.Ne, Token.Lt,
			Token.Gt, Token.Le, Token.Ge, Token.Shl, Token.Shr, Token.Add, Token.Sub, Token.Mul, Token.Div, Token.Mod };
	static Token[] unaryPreOps = { Token.Lnot, Token.Binv, Token.Sub, Token.Inc, Token.Dec };
	static Token[] unaryPostOps = { Token.Inc, Token.Dec };
	static Token[] operands = { Token.NumberLiteral, Token.StringLiteral, Token.Identifier };

	/** 操作符优先级 */
	public static HashMap<Token, Integer> priority = new HashMap<>();
	{
		priority.put(Token.Assign, 5);
		priority.put(Token.AssignAdd, 5);
		priority.put(Token.AssignSub, 5);
		priority.put(Token.AssignMul, 5);
		priority.put(Token.AssignDiv, 5);
		priority.put(Token.AssignMod, 5);
		priority.put(Token.Lor, 10);
		priority.put(Token.Land, 20);
		priority.put(Token.Bor, 30);
		priority.put(Token.Bxor, 40);
		priority.put(Token.Band, 50);
		priority.put(Token.Eq, 60);
		priority.put(Token.Ne, 60);
		priority.put(Token.Lt, 70);
		priority.put(Token.Gt, 70);
		priority.put(Token.Le, 70);
		priority.put(Token.Ge, 70);
		priority.put(Token.Shl, 80);
		priority.put(Token.Shr, 80);
		priority.put(Token.Add, 90);
		priority.put(Token.Sub, 90);
		priority.put(Token.Mul, 100);
		priority.put(Token.Div, 100);
		priority.put(Token.Mod, 100);
	}

	/** 保留字 */
	public static HashMap<String, Token> preset = new HashMap<>();
	{
		preset.put("import", Token.Import);
		preset.put("class", Token.Class);
		preset.put("Interface", Token.Interface);
		preset.put("enum", Token.Enum);
		preset.put("extends", Token.Extends);
		preset.put("implements", Token.Implements);
		preset.put("if", Token.If);
		preset.put("else", Token.Else);
		preset.put("while", Token.While);
		preset.put("do", Token.Do);
		preset.put("for", Token.For);
		preset.put("continue", Token.Continue);
		preset.put("break", Token.Break);
		preset.put("return", Token.Return);
		preset.put("sizeof", Token.Sizeof);
		preset.put("=", Token.Assign);
		preset.put("?", Token.Question);
		preset.put("||", Token.Lor);
		preset.put("&&", Token.Land);
		preset.put("|", Token.Bor);
		preset.put("^", Token.Bxor);
		preset.put("&", Token.Band);
		preset.put("==", Token.Eq);
		preset.put("!=", Token.Ne);
		preset.put("<", Token.Lt);
		preset.put(">", Token.Gt);
		preset.put("<=", Token.Le);
		preset.put(">=", Token.Ge);
		preset.put("<<", Token.Shl);
		preset.put(">>", Token.Shr);
		preset.put("+", Token.Add);
		preset.put("-", Token.Sub);
		preset.put("*", Token.Mul);
		preset.put("/", Token.Div);
		preset.put("%", Token.Mod);
		preset.put("++", Token.Inc);
		preset.put("--", Token.Dec);
		preset.put("(", Token.ParenL);
		preset.put(")", Token.ParenR);
		preset.put("[", Token.BrackL);
		preset.put("]", Token.BrackR);
		preset.put("{", Token.BraceL);
		preset.put("}", Token.BraceR);
		preset.put(":", Token.Colon);
		preset.put("#", Token.Sharp);
		preset.put(".", Token.Dot);
		preset.put(";", Token.SemiColon);
		preset.put(",", Token.Comma);
		preset.put("\"", Token.Quote);
		preset.put("'", Token.QuoteSingle);
		preset.put("\\", Token.Slope);
		preset.put("~", Token.Binv);
	}

	/** 反向保留字 */
	public static HashMap<Token, String> presetInv = new HashMap<>();
	{
		presetInv.put(Token.Import, "import");
		presetInv.put(Token.Class, "class");
		presetInv.put(Token.Interface, "Interface");
		presetInv.put(Token.Enum, "enum");
		presetInv.put(Token.Extends, "extends");
		presetInv.put(Token.Implements, "implements");
		presetInv.put(Token.If, "if");
		presetInv.put(Token.Else, "else");
		presetInv.put(Token.While, "while");
		presetInv.put(Token.Do, "do");
		presetInv.put(Token.For, "for");
		presetInv.put(Token.Continue, "continue");
		presetInv.put(Token.Break, "break");
		presetInv.put(Token.Return, "return");
		presetInv.put(Token.Sizeof, "sizeof");
		presetInv.put(Token.Assign, "=");
		presetInv.put(Token.Question, "?");
		presetInv.put(Token.Lor, "||");
		presetInv.put(Token.Land, "&&");
		presetInv.put(Token.Bor, "|");
		presetInv.put(Token.Bxor, "^");
		presetInv.put(Token.Band, "&");
		presetInv.put(Token.Eq, "==");
		presetInv.put(Token.Ne, "!=");
		presetInv.put(Token.Lt, "<");
		presetInv.put(Token.Gt, ">");
		presetInv.put(Token.Le, "<=");
		presetInv.put(Token.Ge, ">=");
		presetInv.put(Token.Shl, "<<");
		presetInv.put(Token.Shr, ">>");
		presetInv.put(Token.Add, "+");
		presetInv.put(Token.Sub, "-");
		presetInv.put(Token.Mul, "*");
		presetInv.put(Token.Div, "/");
		presetInv.put(Token.Mod, "%");
		presetInv.put(Token.Inc, "++");
		presetInv.put(Token.Dec, "--");
		presetInv.put(Token.ParenL, "(");
		presetInv.put(Token.ParenR, ")");
		presetInv.put(Token.BrackL, "[");
		presetInv.put(Token.BrackR, "]");
		presetInv.put(Token.BraceL, "{");
		presetInv.put(Token.BraceR, "}");
		presetInv.put(Token.Colon, ":");
		presetInv.put(Token.Sharp, "#");
		presetInv.put(Token.Dot, ".");
		presetInv.put(Token.SemiColon, ";");
		presetInv.put(Token.Comma, ",");
		presetInv.put(Token.Quote, "\"");
		presetInv.put(Token.QuoteSingle, "'");
		presetInv.put(Token.Slope, "\\");
		presetInv.put(Token.Binv, "~");
	}

	public static void main(String[] args) {
		new Tokens();

		System.out.println(priority.size());
		System.out.println(preset.size());
		// for (String k : preset.keySet())
		// 	System.out.println(k);

		// System.out.println(preset.get("++"));
	}
}
