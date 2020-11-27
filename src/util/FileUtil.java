package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** 文件类 */
public class FileUtil {

	public static String getString(String filename) {
		byte[] datas = getData(filename);
		if (datas == null)
			return "";
		return new String(datas);
	}

	public static void setString(String filename, String content) {
		byte[] datas = content.getBytes();
		setData(filename, datas);
	}

	public static byte[] getData(String filename) {
		File file = new File(filename);

		if (!file.exists()) {
			System.out.println("指定文件不存在");
			return null;
		}
		if (!file.isFile()) {
			System.out.println("指定名称不是文件");
			return null;
		}

		try {
			InputStream reader = new FileInputStream(file);
			byte[] datas = new byte[reader.available()];
			reader.read(datas);
			reader.close();
			return datas;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

	public static void setData(String filename, byte[] datas) {
		File file = new File(filename);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (Exception e) {
				System.out.println("无法创建文件 " + filename);
				return;
			}
		try {
			OutputStream writer = new FileOutputStream(file, false);
			writer.write(datas);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.out.println("文件写入失败 " + filename);
			return;
		}
	}

}
