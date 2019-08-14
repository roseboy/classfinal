package net.roseboy.classfinal.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 简单加密解密
 *
 * @author roseboy
 * @date 2019-08-15
 */
public class EncryptUtils {
    //盐
    private static final String SALT = "whoisyourdaddy#$@#@";

    /**
     * 加密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 加密后的字节
     */
    public static byte[] en(byte[] msg, int start, int end, String key) {
        byte[] keys = merger(md5byte(key + SALT), md5byte(SALT + key));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 解密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 解密后的字节
     */
    public static byte[] de(byte[] msg, int start, int end, String key) {
        byte[] keys = merger(md5byte(key + SALT), md5byte(SALT + key));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 加密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 加密后的字节
     */
    public static byte[] en(byte[] msg, String key) {
        return en(msg, 0, msg.length - 1, key);
    }

    /**
     * 解密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 解密后的字节
     */
    public static byte[] de(byte[] msg, String key) {
        return de(msg, 0, msg.length - 1, key);
    }


    /**
     * md5加密
     *
     * @param str 字符串
     * @return md5字串
     */
    public static byte[] md5byte(String str) {
        byte[] b = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            b = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 合并byte[]
     *
     * @param bt1 字节1
     * @param bt2 字节2
     * @return 合并后的字节
     */
    public static byte[] merger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

}
