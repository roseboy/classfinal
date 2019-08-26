package net.roseboy.classfinal;

import net.roseboy.classfinal.util.Log;
import net.roseboy.classfinal.util.StrUtils;

import java.lang.instrument.ClassFileTransformer;
import java.net.URISyntaxException;
import java.security.Permission;
import java.security.ProtectionDomain;


/**
 * AgentTransformer
 * jvm加载class时回调
 *
 * @author roseboy
 */
public class AgentTransformer implements ClassFileTransformer {
    //密码
    private char[] pwd = null;
    //解密
    JarDecryptor decryptor = null;

    /**
     * 构造方法
     *
     * @param pwd 密码
     */
    public AgentTransformer(char[] pwd) {
        this.pwd = pwd;
        decryptor = new JarDecryptor();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || protectionDomain == null || loader == null) {
            return classfileBuffer;
        }

        String encryptFile = projectPath(protectionDomain);
        //Log.debug(className + "==>" + encryptFile);
        if (StrUtils.isEmpty(encryptFile)) {
            return classfileBuffer;
        }

        className = className.replace("/", ".").replace("\\", ".");

        byte[] bytes = decryptor.doDecrypt(encryptFile, className, pwd);
        //CAFEBABE,表示解密成功
        if (bytes != null && bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66) {
            return bytes;
        }
        return classfileBuffer;

    }

    /**
     * 获取项目运行的路径
     *
     * @param protectionDomain protectionDomain
     * @return 路径
     */
    private String projectPath(ProtectionDomain protectionDomain) {
        String path = null;
        try {
            path = protectionDomain.getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {

        }
        if (path == null) {
            path = protectionDomain.getCodeSource().getLocation().getPath();
        }
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }

        //war包解压后的WEB-INF/classes目录 或 war包解压后WEB-INF/lib
        if (path.contains("WEB-INF")) {
            return path.substring(0, path.indexOf("WEB-INF"));
        }
        //spring-boot项目
        else if (path.contains("BOOT-INF")) {
            return path.substring(0, path.indexOf("BOOT-INF") - 2);
        }
        //普通jar
        else if (path.endsWith(".jar")) {
            return path;
        }
        return null;

    }
}
