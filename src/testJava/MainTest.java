package testJava;

public class MainTest {
	public static void main(String[] args) {

		int a = 10;
		System.out.println();
		System.out.println(a);

		// String line = "\t                ";
		String line = "       \t\t         \t   \t";
		// String line = "";
		// String line = " ";
		String[] ll = line.split("\\s+");
		System.out.println(ll.length);
		for (String i : ll)
			System.out.println(i + " " + i.length());
	}
}
