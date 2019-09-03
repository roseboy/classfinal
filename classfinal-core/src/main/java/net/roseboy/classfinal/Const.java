package net.roseboy.classfinal;

/**
 * 常量
 *
 * @author roseboy
 */
public class Const {

    //加密出来的文件名
    public static final String FILE_NAME = "classes";

    //lib下的jar解压的目录名后缀
    public static final String LIB_JAR_DIR = "__temp__";

    //默认加密方式
    public static final int ENCRYPT_TYPE = 1;

    //密码标记
    public static final String CONFIG_PASS = "org.springframework.config.Pass";
    //机器码标记
    public static final String CONFIG_CODE = "org.springframework.config.Code";
    //本项目需要打包的代码
    public static final String[] CLASSFINAL_FILES = {"CoreAgent.class", "InputForm.class","InputForm$1.class",
            "JarDecryptor.class", "AgentTransformer.class", "Const.class", "CmdLineOption.class",
            "EncryptUtils.class", "IoUtils.class", "JarUtils.class", "Log.class", "StrUtils.class",
            "SysUtils.class"};

    //调试模式
    public static boolean DEBUG = false;

    public static void pringInfo() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("=                                                       =");
        System.out.println("=      Java Class Encryption Tool v1.1.6   by Mr.K      =");
        System.out.println("=                                                       =");
        System.out.println("=========================================================");
        System.out.println();
    }

}
