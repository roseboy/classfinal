package net.roseboy.classfinal.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串工具
 *
 * @author roseboy
 */
public class StrUtils {
    /**
     * 逗号分割的字符串转list
     *
     * @param strs 逗号分割的字串
     * @return list
     */
    public static List<String> toList(String strs) {
        List<String> list = new ArrayList<>();
        if (strs != null && strs.length() > 0) {
            String[] ss = strs.split(",");
            for (String s : ss) {
                if (s.trim().length() > 0) {
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否是空的
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否是空的
     */
    public static boolean isEmpty(char[] str) {
        return str == null || str.length == 0;
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不是空的
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不是空的
     */
    public static boolean isNotEmpty(char[] str) {
        return !isEmpty(str);
    }

    /**
     * 合并byte[]
     *
     * @param bts 字节数组
     * @return 合并后的字节
     */
    public static char[] merger(char[]... bts) {
        int lenght = 0;
        for (char[] b : bts) {
            lenght += b.length;
        }

        char[] bt = new char[lenght];
        int lastLength = 0;
        for (char[] b : bts) {
            System.arraycopy(b, 0, bt, lastLength, b.length);
            lastLength += b.length;
        }
        return bt;
    }

    /**
     * 字节转char数组
     *
     * @param bytes 字节数组
     * @return chars
     */
    public static char[] toChars(byte[] bytes) {
        byte[] bytes0 = new byte[bytes.length];
        System.arraycopy(bytes, 0, bytes0, 0, bytes.length);
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    /**
     * 字符数组转成字节数组
     *
     * @param chars 字符数组
     * @return 字节数组
     */
    public static byte[] toBytes(char[] chars) {
        char[] chars0 = new char[chars.length];
        System.arraycopy(chars, 0, chars0, 0, chars.length);
        CharBuffer charBuffer = CharBuffer.wrap(chars0);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    /**
     * char数组比较
     *
     * @param char1 char1
     * @param char2 char2
     * @return 是否相等
     */
    public static boolean equal(char[] char1, char[] char2) {
        if (char1.length != char2.length) {
            return false;
        }

        for (int i = 0; i < char1.length; i++) {
            if (char1[i] != char2[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * 字符串是否包含数组中的任意元素
     *
     * @param array 数组
     * @param str   包含的字串
     * @return 是否
     */
    public static boolean containsArray(String str, String[] array) {
        for (String e : array) {
            if (str.contains(e)) {
                return true;
            }
        }
        return false;
    }
}
