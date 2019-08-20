package net.roseboy.classfinal;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
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
        if (className == null || protectionDomain == null || loader == null) {
            return classfileBuffer;
        }

        Permission permission = protectionDomain.getPermissions().elements().nextElement();
        String encryptFile = permission.getName();
        //war包解压后的WEB-INF/classes目录
        if (encryptFile.endsWith("-") && encryptFile.contains("WEB-INF") && encryptFile.contains("classes")) {
            encryptFile = encryptFile.substring(0, encryptFile.indexOf("WEB-INF"));
        }
        //war包解压后WEB-INF/lib
        else if (encryptFile.endsWith(".jar") && encryptFile.contains("WEB-INF") && encryptFile.contains("lib")) {
            encryptFile = encryptFile.substring(0, encryptFile.indexOf("WEB-INF"));
        }
        //tomcat home 下的bin或lib
        else if (encryptFile.contains("lib") || encryptFile.contains("bin")) {
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
}
