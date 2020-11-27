// https://blog.csdn.net/randompeople/article/details/78066442
package testJava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Order {

	public static void main(String[] args) throws IOException {
		//6.空白行问题，调用下面写文件函数和查看空行数函数
		writeR();
		System.out.println("文件 d:/out.txt 空行数为" + staticWhileLine("d:/out.txt"));
	}

	//查看文件空白行数目的函数
	public static int staticWhileLine(String fileName) {
		FileReader fr = null;
		String line = null;
		int count = 0;
		int total = 0;
		int temp = 0;
		//这三个正则  "\\s*" "\\s{0}"和  上来一行啥也不写，直接换行是这个 ""，用""正则表达式能把一行中啥也没有匹配出来，\\s{1} 匹配不出来""这个空串，也就是""不在\s里面
		String regexContainN = "\\s*"; //单纯的\n ， \r 也能被匹配到   //匹配任意空白行的正则表达式,包含开头就换行的(readLine()出来的结果是"" )
		String regexNotN = "^[\\s&&[^\\n]]\\s*$"; //注意readline()出来的字符串中没有\n或\r，所以这个正则排除第一个字符是\n的字符串，没意义，因为readline() 出来的,根本不可能出现\n 开头的字符串。这个正则能匹配出开头必须有\t 等东西的空白行字符串，但是匹配不出来""这种行。
		String regexForDigit = "\\d"; //这才是正则，故\\n 是正则，而\n 只是字符串换行符
		try {
			fr = new FileReader(new File(fileName));
			BufferedReader br = new BufferedReader(fr);
			//用单个字符的AUSCII码匹配，能匹配出只含 \n 或\r 行的方法 /
			//			while((temp = br.read())!=-1 ){ //read 读字节，读出来了文件中的\r
			//				System.out.println(temp);//是13，也就是\r的ausii 码，0xD.如果写文件是，\n，则读出来是10 ，0xA
			//				total++;
			//				if(temp==10) // 此处13代表 \r，10代表\n
			//				{
			//					count++;
			//					System.out.println(line+total); //这个readLine方法底层调用的是Reader的read(char [])
			//				}
			//			}
			//任意空白行则用regexContainN 即\\s* 去匹配，如果不想包含""空串，可以用regexNotN 去匹配 
			while ((line = br.readLine()) != null) { //readLine 这里是读取 \r 或者\n 之前的部分，不包括\r \n，所以这里你用\\n 去匹配 line，匹配不出来什么
				total++;
				System.out.println(line);//是\r
				if (line.matches(regexContainN)) //
				{
					count++;
					System.out.println(line + total); //这个readLine方法底层调用的是Reader的read(char [])
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;//空白行数为
	}

	//这个函数是写文件，\r 或者\n, 这两者效果相同，结果文件如图1
	public static void writeR() throws IOException {

		FileWriter bw = null;
		try {
			bw = new FileWriter("D:/out.txt");
			for (int i = 0; i < 50000; i++) //此处的i取后16位，2的16次方能表示65536个字符，所以此处写50000都能显示。这里相当于我们把unicode大多数国家的字符都写了进去
				bw.write("\n");// 只写\r是会在文件中换行的
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}