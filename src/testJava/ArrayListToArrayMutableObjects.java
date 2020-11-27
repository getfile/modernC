package testJava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayListToArrayMutableObjects {

	public static void main(String[] args) {
		Person p1 = new Person("Maxsu");
		Person p2 = new Person("Lisa");

		List<Person> pList = new ArrayList<>();
		pList.add(p1);
		pList.add(p2);

		Person[] pArray = pList.toArray(new Person[0]);

		System.out.println("Original List = " + pList);
		System.out.println("Created Array from ArrayList = " + Arrays.toString(pArray));

		//let's change the list and array
		pList.get(0).setName("David");
		pArray[1].setName("Ram");
		pList.clear();

		System.out.println("Modified List = " + pList);
		System.out.println("Modified Array = " + Arrays.toString(pArray));

		// String line = "\tif exp \tthen [stmtSeq { } \nuen:ce ] end	: \f \t	";
		String line = "";
		String[] ss = line.split("\\s+");
		System.out.println("\n========================== " + ss.length);
		for (String s : ss)
			System.out.println(s + "\t:" + s.length());

	}

}

class Person {
	private String name;

	public Person(String n) {
		this.name = n;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/java/java-arraylist-to-array.html
