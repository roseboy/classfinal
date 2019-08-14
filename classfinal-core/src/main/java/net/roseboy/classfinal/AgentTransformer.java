package net.roseboy.classfinal;

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

    //加密后生成的文件路径
    private String[] files = null;
    //密码
    private String[] pwds = null;
    //解密
    JarDecryptor decryptor = null;

    /**
     * 构造方法
     * @param files 加密后产生的dat文件，多个
     * @param pwds  密码，多个，与fiels一一对应
     */
    public AgentTransformer(String[] files, String[] pwds) {
        this.files = files;
        this.pwds = pwds;
        decryptor = new JarDecryptor(files, pwds);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (files == null || files.length == 0 || className == null) {
            return classfileBuffer;
        }

        byte[] classByte = decryptor.doDecrypt(className);
        if (classByte != null) {
            return classByte;
        }
        return classfileBuffer;

    }

}
