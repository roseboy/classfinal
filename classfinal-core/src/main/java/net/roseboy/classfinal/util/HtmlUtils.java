package net.roseboy.classfinal.util;

import java.io.File;

public class HtmlUtils {
    public static void main(String[] args) throws Exception {
        String txt = IoUtils.readTxtFile(new File("/Users/roseboy/work-yiyon/易用框架/yiyon-framework/jeee-importer/src/main/resources/templates/importer-upload-form-pro.ftl"));
        long t1 = System.currentTimeMillis();
        txt = removeComments(txt);
        txt = removeBlank(txt);
        //txt = removeBr(txt);

        long t2 = System.currentTimeMillis();
        System.out.println(txt);
        //System.out.println(t2 - t1);
    }

    /**
     * 去除代码中的注释
     *
     * @param code 代码
     * @return 代码
     */
    public static String removeBlank(String code) {
        StringBuilder result = new StringBuilder();

        int quot = 0;//单引号
        int quots = 0;//双引号
        boolean inScript = false;//进入script标签
        boolean inStyle = false;//进入style标签
        boolean inTextArea = false;//进入textaera标签
        char[] chars = code.replace("\r\n", "\n").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (inStyle) {
                if (isElementEnd(i, chars, "</style>")) {
                    inStyle = false;
                }
                if (chars[i] != ' ' && chars[i] != '\n') {
                    result.append(chars[i]);
                }
                continue;
            } else if (inScript) {
                if (isElementEnd(i, chars, "</script>")) {
                    inScript = false;
                }
                if (chars[i] != '\n') {
                    result.append(chars[i]);
                }
                continue;
            } else if (inTextArea) {
                if (isElementEnd(i, chars, "</textarea>")) {
                    inTextArea = false;
                }
                result.append(chars[i]);
                continue;
            }

            //判断是不是注释开始
            if (chars[i] == '"' && chars[i - 1] != '\\' && quot % 2 == 0) {//不是转义，不在单引号内的双引号
                quots++;
                result.append(chars[i]);
            } else if (chars[i] == '\'' && chars[i - 1] != '\\' && quots % 2 == 0) {//不是转义，不在双引号内的单引号
                quot++;
                result.append(chars[i]);
            } else if (quot % 2 == 0 && quots % 2 == 0) {//不在引号内
                // <script
                if (isElementBegin(i, chars, "<script>")) {
                    i = append(i, chars, result, '>');
                    inScript = true;
                }
                // <style
                else if (isElementBegin(i, chars, "<style>")) {
                    i = append(i, chars, result, '>');
                    inStyle = true;
                }
                // <textarea
                else if (isElementBegin(i, chars, "<textarea>")) {
                    i = append(i, chars, result, '>');
                    inTextArea = true;
                }
                //html标签内
                else if (chars[i] == '<') {
                    i = append(i, chars, result, '>');
                } else {
                    result.append(chars[i]);
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }


    /**
     * 去除代码中的注释
     *
     * @param code 代码
     * @return 代码
     */
    public static String removeComments(String code) {
        StringBuilder result = new StringBuilder();

        int quot = 0;//单引号
        int quots = 0;//双引号
        boolean jumpToNextLine = false;//跳过当前行
        boolean jumpToComment1 = false;//跳到注释类型1结束   /*  xxx */
        boolean jumpToComment2 = false;//跳到注释类型2结束   <!--  xxx -->
        char[] chars = code.replace("\r\n", "\n").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            //跳过注释中的内容
            if (jumpToNextLine) {
                if (chars[i] == '\n') {
                    jumpToNextLine = false;
                    result.append(chars[i]);
                }
                continue;
            } else if (jumpToComment1) {
                if (isElementEnd(i, chars, "*/")) {
                    jumpToComment1 = false;
                }
                continue;
            } else if (jumpToComment2) {
                if (isElementEnd(i, chars, "-->")) {
                    jumpToComment2 = false;
                }
                continue;
            }

            //判断是不是注释开始
            if (chars[i] == '"' && chars[i - 1] != '\\' && quot % 2 == 0) {//不是转义，不在单引号内的双引号
                quots++;
                result.append(chars[i]);
            } else if (chars[i] == '\'' && chars[i - 1] != '\\' && quots % 2 == 0) {//不是转义，不在双引号内的单引号
                quot++;
                result.append(chars[i]);
            } else if (quot % 2 == 0 && quots % 2 == 0) {//不在引号内
                //双斜杠注释
                if (isElementBegin(i, chars, "// ")) {
                    jumpToNextLine = true;
                }
                // /*注释*/
                else if (isElementBegin(i, chars, "/* ")) {
                    jumpToComment1 = true;
                }
                // <!--注释-->
                else if (isElementBegin(i, chars, "<!-- ")) {
                    jumpToComment2 = true;
                } else {
                    result.append(chars[i]);
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    private static boolean isElementBegin(int index, char[] chars, String element) {
        element = element.toLowerCase();
        for (int i = 0; i < element.length() - 1; i++) {
            if (Character.toLowerCase(chars[index + i]) != element.charAt(i)) {
                return false;
            }
        }
        if (element.charAt(element.length() - 1) == ' ' || chars[index + element.length() - 1] == ' '
                || chars[index + element.length() - 1] == element.charAt(element.length() - 1)) {
            return true;
        }
        return false;
    }

    private static boolean isElementEnd(int index, char[] chars, String element) {
        element = element.toLowerCase();
        for (int i = index; i > index - element.length(); i--) {
            if (Character.toLowerCase(chars[i]) != element.charAt(element.length() - index + i - 1)) {
                return false;
            }
        }
        return true;
    }

    private static int append(int i, char[] chars, StringBuilder result, char endChar) {
        while (chars[i] != endChar) {
            chars[i] = chars[i] == '\n' ? ' ' : chars[i];
            if (chars[i] == ' ' && chars[i - 1] == ' ') {
                i++;
                continue;
            }
            result.append(chars[i++]);
        }
        result.append(chars[i]);
        return i;
    }

    /**
     * 移除空白行和行前空格
     *
     * @param code 代码
     * @return 代码
     */
    public static String removeBlankLine(String code) {
        StringBuilder result = new StringBuilder();
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            result.append(line).append("\n");
        }
        return result.toString();
    }


}
