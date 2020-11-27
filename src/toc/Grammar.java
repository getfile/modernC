package toc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import util.FileUtil;

/** 文法处理
 */
public class Grammar {

	/** 文法规则表 key为非终结符, 对应多个产生式 */
	HashMap<String, ArrayList<String[]>> rules = new HashMap<>();
	ArrayList<String> rulesName = new ArrayList<>(); //有序的规则名, 按添加的顺序排列
	/** 终结符集 */
	HashMap<String, Boolean> terminals = new HashMap<>();
	/** 开始符 */
	String startKey;

	/** 解析规则表 */
	public Grammar rulesParse(String filename) {
		String content = FileUtil.getString(filename);
		if (content == null || content.length() == 0) {
			System.out.println("未发现文法的规则内容");
			return this;
		}

		ArrayList<String[]> ls = new ArrayList<>();

		//解析文法规则表
		String[] lines = content.split("\r\n");
		for (String line : lines) {
			if (line.length() == 0 || line.split("\\s+").length == 0) //过滤空行
				continue;
			if (line.charAt(0) == '/' && line.charAt(1) == '/') //过滤注释行
				continue;
			// 解析出key的规则项
			if (line.charAt(0) == '\t')
				ls.add(line.substring(1).split("\\s+"));
			// 解析出key
			else {
				String key = line.split("\\s+")[0];
				ls = new ArrayList<>();
				rules.put(key, ls);
				rulesName.add(key);
				if (startKey == null)
					startKey = key;
			}
		}

		// 解析终结符集
		int len = rulesName.size();
		for (int i = 0; i < len; i++) {
			ArrayList<String[]> rls = rules.get(rulesName.get(i));
			for (String[] r : rls)
				for (String s : r)
					if (!rules.containsKey(s))
						terminals.put(s, true);
		}

		return this;
	}

	/** 打印规则表 */
	public Grammar rulesStr() {
		System.out.println("\n-------------------------- rules");
		for (String key : rulesName) {
			System.out.println(key + " :");
			ArrayList<String[]> rls = rules.get(key);
			for (String[] r : rls)
				System.out.println("\t" + String.join(" ", r));
		}
		return this;
	}

	/** 打印终结符集 */
	public Grammar terminalsStr() {
		System.out.println("\n-------------------------- rules terminals");
		for (String key : terminals.keySet())
			System.out.print("\t" + key);
		System.out.println("\n");
		return this;
	}

	/** 构造单向规则表(为纯粹的树结构), 去掉循环路径 */
	public Grammar rulesChange() {
		class KeyFrame {
			String key;
			int ruleIdx;
			int tokenIdx;

			KeyFrame(String key, int idx, int tidx) {
				this.key = key;
				ruleIdx = idx;
				tokenIdx = tidx;
			}
		}

		HashMap<String, Boolean> keysMap = new HashMap<>();
		Stack<KeyFrame> keysStack = new Stack<>();
		keysStack.push(new KeyFrame(startKey, 0, 0));
		keysMap.put(startKey, true);

		while (keysStack.size() > 0) {
			KeyFrame kf = keysStack.peek();
			ArrayList<String[]> keyRules = rules.get(kf.key);
			if (keyRules.size() < 1) //该非终结符已经失效
			{
				keysStack.pop();
				keysMap.remove(kf.key);
				if (!keysStack.isEmpty())
					keysStack.peek().tokenIdx--;
				continue;
			}
			if (kf.ruleIdx >= keyRules.size()) //超过规则数
			{
				keysStack.pop();
				keysMap.remove(kf.key);
				continue;
			}
			String[] singleRule = keyRules.get(kf.ruleIdx);
			if (kf.tokenIdx >= singleRule.length)//下一条规则
			{
				kf.ruleIdx++;
				kf.tokenIdx = 0;
				continue;
			}
			String token = singleRule[kf.tokenIdx];
			if (!rules.containsKey(token)) //本词素为终结符, 下一个词素
			{
				kf.tokenIdx++;
				continue;
			}
			//本词素为非终结符
			if (keysMap.containsKey(token) || //非终结符已经在路径上(即轮回了)
					(rules.get(token).size() == 0)) //非终结符已经失效
			{
				keyRules.remove(kf.ruleIdx);
				kf.tokenIdx = 0;
				continue;
			}
			kf.tokenIdx++;
			keysStack.push(new KeyFrame(token, 0, 0));
			keysMap.put(token, true);
		}
		return this;
	}

	/* 根据文法规则, 随机生成一段代码(开始符) */
	public void genCodes(String filename, int level, String rootKey) {
		ArrayList<String> codes = new ArrayList<>();

		codes.add(rootKey == null ? startKey : rootKey);

		int idx = 0; //替换项位置
		// 扩展, 非终结符根据产生式随机替换
		for (int i = 0; i < level && idx < codes.size(); i++) {
			String key = codes.get(idx);
			// 替换重复符'*'
			if (key.length() > 1 && key.charAt(key.length() - 1) == '*') {
				key = key.substring(0, key.length() - 1);
				int times = (int) (Math.random() * 5);
				codes.remove(idx);
				for (int n = 0; n < times; n++)
					codes.add(idx, key);
				continue;
			}
			ArrayList<String[]> rls = rules.get(key);
			if (rls == null || rls.size() == 0) {
				idx++;
				continue;
			}
			// 随机选择一条规则
			int ruleIdx = (int) (Math.random() * rls.size());
			String[] rule = rls.get(ruleIdx);
			// 替换非终结符
			codes.remove(idx);
			codes.addAll(idx, Arrays.asList(rule));
		}
		// 收尾, 全部替换成终结符
		rulesChange();
		while (idx < codes.size()) {
			String key = codes.get(idx);
			// 删除重复的非终结符, 带'*'的词素
			if (key.length() > 1 && key.charAt(key.length() - 1) == '*') {
				codes.remove(idx);
				continue;
			}
			ArrayList<String[]> rls = rules.get(key);
			if (rls == null) {
				idx++;
				continue;
			}
			if (rls.size() == 0) {
				codes.remove(idx);
				continue;
			}
			// 随机选择一条规则
			int ruleIdx = (int) (Math.random() * rls.size());
			String[] rule = rls.get(ruleIdx);
			// 替换非终结符
			codes.remove(idx);
			codes.addAll(idx, Arrays.asList(rule));
		}
		FileUtil.setString(filename, String.join(" ", codes.toArray(new String[0])));
	}

}
