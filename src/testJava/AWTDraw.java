// https://blog.csdn.net/qq_42889476/article/details/88858337

package testJava;

import java.awt.*;
import java.awt.event.*;

public class AWTDraw {
	public static void main(String[] args) {
		// TestCardLayout tc = new TestCardLayout();
		// tc.create();

		PanelInFrame fr = new PanelInFrame("Frame with Panel");

		// FirstFrame fr = new FirstFrame("First contianer!!");
	}

	public static void threeButtons() {
		Frame f = new Frame();
		f.setLayout(new FlowLayout());
		Button button1 = new Button("Yes");
		Button button2 = new Button("No");
		Button button3 = new Button("Close");
		f.add(button1);
		f.add(button2);
		f.add(button3);
		f.setSize(300, 150);
		f.setVisible(true);
	}

	public static void fiveButtons() {

		Frame f = new Frame("BorderLayout");
		f.setLayout(new BorderLayout());
		f.add("North", new Button("North")); //第一个参数表示把按钮添加到容器的North区域
		f.add("South", new Button("South")); //第一个参数表示把按钮添加到容器的South区域
		f.add("West", new Button("West")); //第一个参数表示把按钮添加到容器的West区域
		f.add("East", new Button("East")); //第一个参数表示把按钮添加到容器的East区域
		f.add("Center", new Button("Center")); //第一个参数表示把按钮添加到容器的Center区域
		f.setSize(300, 300);
		f.setVisible(true);
	}

	public static void buttonGrid() {
		Frame f = new Frame("GridLayout");
		f.setLayout(new GridLayout(3, 2)); //容器平均分成3行2列
		f.add(new Button("1")); //添加到第1行第1格
		f.add(new Button("2")); //添加到第1行第2格
		f.add(new Button("3")); //添加到第2行第1格
		f.add(new Button("4")); //添加到第2行第2格
		f.add(new Button("5")); //添加到第3行第1格
		f.add(new Button("6")); //添加到第3行第2格
		f.setSize(300, 300);
		f.setVisible(true);
	}

}

class PanelInFrame extends Frame {

	public PanelInFrame(String str) {
		super(str);

		Panel pan = new Panel();
		pan.setSize(100, 100);
		pan.setBackground(Color.green);

		setSize(250, 250);
		setBackground(Color.blue);
		setLayout(null); //取消布局管理器
		add(pan);
		setVisible(true);
	}
}

class TestCardLayout implements ActionListener {
	private Panel p1, p2, p3;
	private Button b1, b2, b3;
	private Frame f;
	private CardLayout CLayout = new CardLayout();

	public void create() {
		b1 = new Button("第1个");
		b1.addActionListener(this); //注册监听器
		b2 = new Button("第2个");
		b2.addActionListener(this);
		b3 = new Button("第3个");
		b3.addActionListener(this);

		p1 = new Panel();
		p1.add(b1);
		p2 = new Panel();
		p2.add(b2);
		p3 = new Panel();
		p3.add(b3);

		f = new Frame("Test");
		f.setLayout(CLayout);
		f.add(p1, "第一层");
		f.add(p2, "第二层");
		f.add(p3, "第三层");
		f.setSize(300, 200);
		f.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		CLayout.next(f); //当按钮被单击时，实现下一张卡片的功能
	}
}

class FirstFrame extends Frame {

	public FirstFrame(String str) {
		super(str); //调用父类的构造方法
		setSize(240, 240); //设置Frame的大小
		setBackground(Color.yellow); //设置Frame的背景色
		setVisible(true); //设置Frame为可见
	}

}