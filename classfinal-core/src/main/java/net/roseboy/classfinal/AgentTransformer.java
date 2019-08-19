package net.roseboy.classfinal;

import net.roseboy.classfinal.util.Log;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;


/**
 * AgentTransformer
 * jvm加载class时回调
 *
 * @author roseboy
 */
public class AgentTransformer implements ClassFileTransformer {
    //密码
    private String pwd = null;
    //解密
    JarDecryptor decryptor = null;

    /**
     * 构造方法
     *
     * @param pwd 密码
     */
    public AgentTransformer(String pwd) {
        this.pwd = pwd;
        decryptor = new JarDecryptor();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) {
            return classfileBuffer;
        }
        String encryptFile = loader.getResource("/../../").getPath();

        //jar文件
        if (encryptFile.startsWith("file:")) {
            encryptFile = encryptFile.substring(5);
            if (encryptFile.contains(".jar!")) {
                encryptFile = encryptFile.substring(0, encryptFile.indexOf(".jar!") + 4);
            }
        }

        className = className.replace(Constants.FILE_SEPARATOR, ".");
        className = className.replace(File.separator, ".");

        byte[] bytes = decryptor.doDecrypt(encryptFile, className, pwd);
        //CAFEBABE,表示解密成功
        if (bytes != null && bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66) {
            return bytes;
        }
        return classfileBuffer;

    }
}
