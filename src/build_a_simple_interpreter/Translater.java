package build_a_simple_interpreter;

import util.FileUtil;

public class Translater {
	public static void main(String[] args) {
		// String src = "	/*stop //	*/	void main(){int a=1; string s = \"he\'l\\l\'o\"; float f = 3.1415;	}	";
		// String src = "12312 3.1415  78e12312 9e-1213 9.e12 6.8e90 0e.";
		// String src = FileUtil.getString("src/build_a_simple_interpreter/BuildaSimpleInterpreter.java");
		// String src = FileUtil.getString("src/toc/rulesMc.txt");
		// String src = FileUtil.getString("testmc/code.mc");
		String src = FileUtil.getString("testmc/lib1/code1.mc");
		// String src = FileUtil.getString("testmc/lib1/code2.mc");

		Lexer lexer = new Lexer(src);
		// lexer.showTokens();
		// lexer.parseExp().tree("");
		// System.out.println(lexer.parseExp().code(""));
		System.out.println(lexer.parse().code(""));
	}

}
