package build_a_simple_interpreter;

import java.util.HashMap;

public class Project {
	public static void main2(String[] args) {
		HashMap<String, String> flags = new HashMap<>();
		for (String flag : args)
			flags.put(flag, flag);

		if (flags.containsKey("new")) { //创建项目结构(生成 mc.json, cmakelists.txt文件)
		}
		if (flags.containsKey("build")) { //mc文件检索,转换成c
		}
		if (flags.containsKey("run")) { //c编译成exe/lib
		}

	}

}
