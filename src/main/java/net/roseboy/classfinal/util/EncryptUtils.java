package net.roseboy.classfinal.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 简单加密解密
 *
 * @author roseboy
 * @date 2019-07-30
 */
public class EncryptUtils {
    private static final String SALT = "whoisyourdaddy#$@#@";

    /**
     * 加密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return
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
     * @return
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
     * @return
     */
    public static byte[] en(byte[] msg, String key) {
        return en(msg, 0, msg.length - 1, key);
    }

    /**
     * 解密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return
     */
    public static byte[] de(byte[] msg, String key) {
        return de(msg, 0, msg.length - 1, key);
    }


    /**
     * md5加密
     *
     * @param str
     * @return
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
     * @param bt1
     * @param bt2
     * @return
     */
    public static byte[] merger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    /**
     * 加密并压缩
     *
     * @param classNames  需要加密的所有class文件
     * @param encryptPath class目录
     * @param targetFile  加密后生成的zip
     * @param password    密码
     * @param isZip       是否压缩成zip，否的话加密后的文件和原class在统一目录下
     */
    public static void encryptFiles(String encryptPath, String targetFile, List<String> classNames, String password, boolean isZip) {
        //不压缩zip
        if (!isZip) {
            for (String classname : classNames) {
                String classPath = encryptPath + File.separator + classname.replace(".", File.separator) + ".class";
                File sourceFile = new File(classPath);
                byte[] bytes = IoUtils.readFileToByte(sourceFile);
                bytes = en(bytes, password);
                IoUtils.writeFile(new File(encryptPath + File.separator + classname.replace(".", File.separator) + ".clazz"), bytes);
            }
            return;
        }

        ZipOutputStream zos = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(targetFile));
            zos = new ZipOutputStream(out);
            for (String classname : classNames) {
                String classPath = encryptPath + File.separator + classname.replace(".", File.separator) + ".class";
                File sourceFile = new File(classPath);

                zos.putNextEntry(new ZipEntry(classname));
                byte[] bytes = IoUtils.readFileToByte(sourceFile);
                bytes = en(bytes, password);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zos, out);
        }
    }

    public static void encryptFiles(String encryptPath, String targetFile, List<String> classNames, String password) {
        encryptFiles(encryptPath, targetFile, classNames, password, true);
    }


    /**
     * 根据名称解密出一个文件
     *
     * @param encryptPath classes目录
     * @param fileName    加密生成的zip
     * @param password    密码
     * @param isZip       是否压缩
     * @return
     */
    public static byte[] decryptFile(String encryptPath, String fileName, String password, boolean isZip) {
        if (!isZip) {
            File file = new File(encryptPath + File.separator + fileName + ".clazz");
            if (!file.exists()) {
                return null;
            }
            byte[] bytes = IoUtils.readFileToByte(file);
            bytes = de(bytes, password);
            return bytes;
        }

        ZipFile zipFile = null;
        try {
            File zip = new File(encryptPath);
            if (!zip.exists()) {
                return null;
            }
            zipFile = new ZipFile(zip);
            ZipEntry zipEntry = zipFile.getEntry(fileName);
            if (zipEntry == null) {
                return null;
            }
            InputStream is = zipFile.getInputStream(zipEntry);
            byte[] bytes = IoUtils.toByteArray(is);
            bytes = de(bytes, password);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zipFile);
        }
        return null;
    }

    public static byte[] decryptFile(String encryptPath, String fileName, String password) {
        return decryptFile(encryptPath, fileName, password, true);
    }

    public static void main(String[] args) throws Exception {

    }


}
