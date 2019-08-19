package net.roseboy.classfinal;

import net.roseboy.classfinal.util.EncryptUtils;
import net.roseboy.classfinal.util.IoUtils;
import net.roseboy.classfinal.util.JarUtils;
import net.roseboy.classfinal.util.Log;

import java.io.File;

/**
 * java class解密
 *
 * @author roseboy
 */
public class JarDecryptor {

    /**
     * 根据名称解密出一个文件
     *
     * @param encryptFile 加密后的dat
     * @param className   文件名
     * @param password    密码
     * @return 解密后的字节
     */
    public byte[] doDecrypt(String encryptFile, String className, String password) {
        long t1 = System.currentTimeMillis();

        String classFile = "META-INF" + Const.FILE_SEPARATOR + Const.FILE_NAME + Const.FILE_SEPARATOR + className;
        File workDir = new File(encryptFile);
        byte[] bytes = null;

        if (workDir.isFile()) {
            bytes = JarUtils.getFileFromJar(workDir, classFile);
        } else {
            File file = new File(workDir, classFile);
            if (file.exists()) {
                bytes = IoUtils.readFileToByte(file);
            }
        }

        if (bytes == null) {
            return null;
        }

        bytes = EncryptUtils.de(bytes, password + className, 1);

        if (Const.DEBUG) {
            long t2 = System.currentTimeMillis();
            Log.debug("解密文件: " + className + " (" + (t2 - t1) + " ms)");
        }
        return bytes;

    }
}
