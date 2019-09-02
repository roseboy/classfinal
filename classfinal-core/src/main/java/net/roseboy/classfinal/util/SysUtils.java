package net.roseboy.classfinal.util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 系统工具类
 */
public class SysUtils {


    /**
     * 运行命令
     *
     * @param cmd  命令
     * @param line 返回第几行结果，0返回所有
     * @return 结果
     */
    public static String runCmd(String cmd, int line) {
        Process process;
        Scanner sc = null;
        StringBuffer sb = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.getOutputStream().close();
            sc = new Scanner(process.getInputStream());
            int i = 0;
            while (sc.hasNextLine()) {
                i++;
                String str = sc.nextLine();
                if (line <= 0) {
                    sb.append(str).append("\r\n");
                } else if (i == line) {
                    return str.trim();
                }
            }
            sc.close();
        } catch (Exception e) {


        } finally {
            IoUtils.close(sc);
        }
        return sb.toString();
    }

    /**
     * 运行cmd命令
     *
     * @param cmd    命令
     * @param substr 关键字
     * @return 包含关键字的行数
     */
    public static String runCmd(String cmd, String substr) {
        Process process;
        Scanner sc = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.getOutputStream().close();
            sc = new Scanner(process.getInputStream());
            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                if (str != null && str.contains(substr)) {
                    return str.trim();
                }
            }
            sc.close();
        } catch (Exception e) {

        } finally {
            IoUtils.close(sc);
        }
        return null;
    }

    /**
     * 获取mac地址
     *
     * @return mac 列表
     */
    public static List<String> getMacList() {
        ArrayList<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            java.util.Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface iface = en.nextElement();
                List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
                for (InterfaceAddress addr : addrs) {
                    InetAddress ip = addr.getAddress();
                    if (ip.isLinkLocalAddress()) {//本地的不要
                        continue;
                    }
                    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                    if (network == null) {
                        continue;
                    }
                    byte[] mac = network.getHardwareAddress();
                    if (mac == null) {
                        continue;
                    }

                    sb.delete(0, sb.length());
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    if (!list.contains(sb.toString())) {
                        list.add(sb.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取cpu序列号
     *
     * @return 序列号
     */
    public static String getCPUSerialNumber() {
        String sysName = System.getProperty("os.name");
        if (sysName.contains("Windows")) {//win
            String str = runCmd("wmic cpu get ProcessorId", 2);
            return str;
        } else if (sysName.contains("Linux")) {
            String str = runCmd("dmidecode |grep -A16 \"Processor Information$\"", "ID");
            if (str != null) {
                return str.substring(str.indexOf(":")).trim();
            }
        } else if (sysName.contains("Mac")) {
            String str = runCmd("system_profiler SPHardwareDataType", "Serial Number");
            if (str != null) {
                return str.substring(str.indexOf(":") + 1).trim();
            }
        }
        return "";
    }


    /**
     * 获取硬盘序列号
     *
     * @return 硬盘序列号
     */
    public static String getHardDiskSerialNumber() {
        String sysName = System.getProperty("os.name");
        if (sysName.contains("Windows")) {//win
            String str = runCmd("wmic path win32_physicalmedia get serialnumber", 2);
            return str;
        } else if (sysName.contains("Linux")) {
            String str = runCmd("dmidecode |grep -A16 \"System Information$\"", "Serial Number");
            if (str != null) {
                return str.substring(str.indexOf(":")).trim();
            }
        } else if (sysName.contains("Mac")) {
            String str = runCmd("system_profiler SPStorageDataType", "Volume UUID");
            if (str != null) {
                return str.substring(str.indexOf(":") + 1).trim();
            }
        }
        return "";
    }

    /**
     * 生成机器码
     *
     * @return 机器码
     */
    public static char[] makeMarchinCode() {
        char[] c1 = EncryptUtils.md5(getMacList().toString().toCharArray());
        char[] c2 = EncryptUtils.md5(getCPUSerialNumber().toCharArray());
        char[] c3 = EncryptUtils.md5(getHardDiskSerialNumber().toCharArray());
        char[] chars = StrUtils.merger(c1, c2, c3);
        for (int i = 0; i < chars.length; i++) {
            chars[i] = Character.toUpperCase(chars[i]);
        }
        return chars;
    }
}
