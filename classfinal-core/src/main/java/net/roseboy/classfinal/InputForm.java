package net.roseboy.classfinal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 密码输入界面，模拟命令行输入
 */
public class InputForm {
    //输入提示
    private static final String tips = " 项目正在启动，请输入启动密码\r\n Password: ";
    //密码显示字符
    private static final char passChar = '*';
    //窗口
    private final JFrame frame = new JFrame();
    //文本域
    JTextArea text = new JTextArea();
    //已输入字符长度
    int keyIndex = 0;
    //已输入的字符，最长100
    char[] password = new char[100];

    public static void main(String[] args) {
        new InputForm();
    }

    // 构造函数
    public InputForm() {
        frame.setTitle("项目启动密码 - ClassFinal");
        frame.setSize(560, 320);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(getWindowAdapter());
        frame.setAlwaysOnTop(true);

        text.setFont(new Font(null, 0, 18));
        text.setBackground(new Color(0, 0, 0));
        text.setForeground(new Color(0, 255, 0));
        text.setText(tips);
        text.addKeyListener(getKeyAdapter());
        text.enableInputMethods(false);
        frame.add(text);
        frame.setVisible(true);
    }

    /**
     * 窗口事件监听
     * @return
     */
    private WindowAdapter getWindowAdapter() {
        return new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent we) {
                //禁止最小化
                frame.setState(JFrame.NORMAL);
            }
        };
    }


    /**
     * 键盘监听事件
     * @return
     */
    private KeyAdapter getKeyAdapter() {
        return new KeyAdapter() {
            String fakePass = "";
            @Override
            public void keyReleased(KeyEvent e) {
                if (keyIndex < 100 && e.getKeyChar() > 32 && e.getKeyChar() < 127) {
                    password[keyIndex] = e.getKeyChar();
                    keyIndex++;
                    fakePass += passChar;
                } else if (keyIndex > 0 && e.getKeyCode() == 8) {//退格
                    keyIndex--;
                    password[keyIndex] = 0;
                    fakePass = fakePass.substring(1);
                } else if (e.getKeyCode() == 10) {//ENTER
                    System.out.println(new String(password).trim());
                    keyIndex = 0;
                    password = new char[100];
                    fakePass = "";
                }
                text.setText(tips + fakePass);
            }
        };
    }
}
