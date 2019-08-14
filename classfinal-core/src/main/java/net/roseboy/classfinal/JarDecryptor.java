package net.roseboy.classfinal;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.roseboy.classfinal.util.ClassUtils;
import net.roseboy.classfinal.util.EncryptUtils;
import net.roseboy.classfinal.util.IoUtils;
import net.roseboy.classfinal.util.JarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * java class解密
 *
 * @author roseboy
 */
public class JarDecryptor {

    //加密后的jar或war或加密产生的calsses.dat文件
    private String[] files = null;//加密后生成的文件路径
    //解密密码
    private String[] pwds = null;//密码

    /**
     * 构造方法
     */
    public JarDecryptor() {
        super();
    }

    /**
     * 构造方法
     *
     * @param files 加密后产生的dat文件，多个
     * @param pwds  密码，多个，与fiels一一对应
     */
    public JarDecryptor(String[] files, String[] pwds) {
        super();
        this.files = files;
        this.pwds = pwds;
    }

    /**
     * 解密出一个文件的字节
     *
     * @param className class全名
     * @return 文件解密后的字节
     */
    public byte[] doDecrypt(String className) {
        //遍历所有的文件
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            String pwd = pwds[i];

            byte[] bytes = JarDecryptor.decryptFile(file, className, pwd);
            if (bytes != null) {
                //CAFEBABE,表示解密成功
                if (bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66) {
                    return bytes;
                }
            }
        }
        return null;
    }

    /**
     * 根据名称解密出一个文件
     *
     * @param encryptFile 加密后的dat
     * @param fileName    文件名
     * @param password    密码
     * @return 解密后的字节
     */
    public static byte[] decryptFile(String encryptFile, String fileName, String password) {
        byte[] bytes = JarUtils.getFileFromJar(new File(encryptFile), fileName);
        if (bytes == null) {
            return null;
        }
        bytes = EncryptUtils.de(bytes, password);
        return bytes;

    }
}
