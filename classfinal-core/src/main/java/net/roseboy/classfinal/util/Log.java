package net.roseboy.classfinal.util;

import net.roseboy.classfinal.Const;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 控制台打印日志工具
 *
 * @author roseboy
 */
public class Log {
    public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 输出debug信息
     *
     * @param msg 信息
     */
    public static void debug(Object msg) {
        if (Const.DEBUG) {
            String log = datetimeFormat.format(new Date()) + " [DEBUG] " + msg;

            System.out.println(log);
        }
    }

    /**
     * 输出
     *
     * @param obj 内容
     */
    public static void println(String obj) {
        System.out.println(obj);
    }

    /**
     * 输出
     *
     * @param obj 内容
     */
    public static void print(String obj) {
        System.out.print(obj);
    }

    /**
     * 输出
     */
    public static void println() {
        System.out.println();
    }
}
