package net.roseboy.classfinal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 密码输入界面，模拟命令行输入
 */
public class InputForm {
    //输入提示
    private static final String tips = " 项目正在启动，请输入启动密码\r\n Password: ";
    //密码显示字符
    private static final char passChar = '*';
    //窗口
    private JDialog frame;
    //文本域
    private JTextArea text;
    //已输入字符长度
    private int keyIndex = 0;
    //已输入的字符，最长100
    private char[] password = new char[100];
    //是否有下一行
    boolean hasNextLine = false;

    /**
     * 获取输入的密码
     *
     * @return 密码char数组
     */
    public char[] nextPasswordLine() {
        while (!hasNextLine) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {

            }
        }

        int charsSize = 0;
        while (password[charsSize] != 0) {
            charsSize++;
        }
        char[] chars = new char[charsSize];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = password[i];
        }

        keyIndex = 0;
        password = new char[100];
        return chars;

    }

    /**
     * 显示窗口
     */
    public boolean showForm() {
        try {
            frame = new JDialog();
            frame.setTitle("项目启动密码 - ClassFinal");
            frame.setSize(560, 320);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setAlwaysOnTop(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            text = new JTextArea();
            text.setFont(new Font(null, 0, 18));
            text.setBackground(new Color(0, 0, 0));
            text.setForeground(new Color(0, 255, 0));
            text.setText(tips);
            text.addKeyListener(getKeyAdapter());
            text.enableInputMethods(false);
            frame.add(text);
            frame.setVisible(true);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 关闭窗口
     */
    public void closeForm() {
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * 键盘监听事件
     *
     * @return KeyAdapter
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
                    fakePass = "";
                    hasNextLine = true;
                }
                text.setText(tips + fakePass);
            }
        };
    }
}
