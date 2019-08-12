package net.roseboy.classfinal.agent;

import net.roseboy.util.EncryptUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;


/**
 * AgentTransformer
 * jvm加载class时回调
 *
 * @author roseboy
 * @date 2019-08-02
 */
public class AgentTransformer implements ClassFileTransformer {

    private String[] files = null;//加密后生成的文件路径
    private String[] pwds = null;//密码

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (getFiles() == null || getFiles().length == 0) {
            return classfileBuffer;
        }

        //遍历所有的文件
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            String pwd = pwds[i];

            byte[] bytes = EncryptUtils.decryptFile(file, className.replace(File.separator, "."), pwd);
            if (bytes != null) {
                //CAFEBABE,表示解密成功
                if (bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66) {
                    //System.out.println(className.replace("/", "."));
                    return bytes;
                }
            }
        }

        return classfileBuffer;

    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public String[] getPwds() {
        return pwds;
    }

    public void setPwds(String[] pwds) {
        this.pwds = pwds;
    }
}
