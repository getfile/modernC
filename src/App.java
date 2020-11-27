import toc.Grammar;

public class App {
	public static void main(String[] args) {
		System.out.println("app >>>");

		// Grammar g = new Grammar().rulesParse("src/toc/rulesJava.txt");
		// Grammar g = new Grammar().rulesParse("src/toc/rulesMiniJava.txt");
		// Grammar g = new Grammar().rulesParse("src/toc/rulesTiny.txt");
		// Grammar g = new Grammar().rulesParse("src/toc/rulesToc.txt");
		// Grammar g = new Grammar().rulesParse("src/toc/rulesMc.txt");
		Grammar g = new Grammar().rulesParse("src/toc/rulesJavaCompiler.txt");

		g.rulesStr().terminalsStr();
		g.rulesChange().rulesStr();
		g.genCodes("target/ruleToCode.java", 1000, null);

	}
}
