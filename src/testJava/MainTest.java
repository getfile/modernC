package testJava;

import java.util.ArrayList;

interface IAA {
	void add(int a, int b);

	void sub(int a, int b);
}

interface IBB extends IAA {
	void add(int a, int b);

	void sub(int a);
}

class Sum implements IAA, IBB {
	public void add(int a, int b) {
		int c = a + b;
		System.out.println(c);
	}

	public void sub(int a, int b) {
		System.out.println(a - b);
	}

	public void sub(int a) {
	}
}

public class MainTest {
	public static void main(String[] args) {

		IAA a = new Sum();
		IBB b = new Sum();
		a.add(1, 2);
		b.add(1, 2);

		System.out.println("---------------------1");
		Object obj1 = getItem(1);
		System.out.println(obj1);
		System.out.println(obj1 instanceof String);
		System.out.println(obj1 instanceof String[]);
		System.out.println("---------------------2");
		Object obj2 = getItem(2);
		System.out.println(obj2);
		System.out.println(obj2 instanceof String[]);
		System.out.println(obj2 instanceof String);

		System.out.println("---------------------3");
		ArrayList<String> all = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Object obj = getItem((int) (Math.random() * 3) + 1);
			if (obj instanceof String)
				all.add((String) obj);
			else if (obj instanceof String[]) {
				for (String s : (String[]) obj)
					all.add(s);
			} else
				all.add((String) obj);
		}
		for (String s : all)
			System.out.println(s);

	}

	static Object getItem(int i) {
		if (i == 1)
			return "hello";
		if (i == 2) {
			String[] items = { "monday", "tuesday" };
			return items;
		}
		return null;
	}
}
