package net.roseboy.classfinal.util;

import java.util.ArrayList;
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
    public boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不是空的
     */
    public boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
