package net.roseboy.classfinal.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
     * 字符串是否包含数组中的任1元素
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

    /**
     * 转换成字符串
     *
     * @param chars 字符数组
     * @return 字符串
     */
    public static String toCharArrayCode(char[] chars) {
        List<Integer> list = new ArrayList<>();
        for (char c : chars) {
            list.add((int) c);
        }
        return list.toString().replace("[", "{").replace("]", "}");
    }

    /**
     * 在字符串的某个位置插入字符串
     *
     * @param arrayStr  字符串数组
     * @param insertStr 要插入的字串
     * @param pos       位置开始标识
     * @return 插入后的字串
     */
    public static String insertStringArray(String[] arrayStr, String insertStr, String pos) {
        StringBuffer newStr = new StringBuffer();
        boolean isInsert = false;
        for (int i = 0; i < arrayStr.length; i++) {
            newStr.append(arrayStr[i]).append("\r\n");
            if (arrayStr[i].startsWith(pos)) {
                newStr.append(insertStr).append("\r\n");
                isInsert = true;
            }
        }
        if (!isInsert) {
            newStr.append(insertStr).append("\r\n");
        }
        return newStr.toString();
    }

    /**
     * 通配符匹配
     *
     * @param match      匹配字符串
     * @param testString 待匹配字符窜
     * @return 是否匹配
     */
    public static boolean isMatch(String match, String testString) {
        String regex = match.replaceAll("\\?", "(.?)")
                .replaceAll("\\*+", "(.*?)");
        return Pattern.matches(regex, testString);
    }

    /**
     * 判断是否是匹配
     *
     * @param matches    匹配的
     * @param testString 要判断
     * @return 是否属于
     */
    public static boolean isMatchs(List<String> matches, String testString) {
        return isMatchs(matches, testString, false);
    }

    public static boolean isMatchs(List<String> matches, String testString, boolean dv) {
        if (matches == null || matches.size() == 0) {
            return dv;
        }

        for (String m : matches) {
            if (StrUtils.isMatch(m, testString) || testString.startsWith(m) || testString.endsWith(m)) {
                return true;
            }
        }
        return false;
    }

}
