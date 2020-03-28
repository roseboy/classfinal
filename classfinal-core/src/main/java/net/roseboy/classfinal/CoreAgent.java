package net.roseboy.classfinal;


import net.roseboy.classfinal.util.*;

import java.io.Console;
import java.io.File;
import java.lang.instrument.Instrumentation;


/**
 * 监听类加载
 *
 * @author roseboy
 */
public class CoreAgent {
    /**
     * man方法执行前调用
     *
     * @param args 参数
     * @param inst inst
     */
    public static void premain(String args, Instrumentation inst) {
        Const.pringInfo();
        CmdLineOption options = new CmdLineOption();
        options.addOption("pwd", true, "密码");
        options.addOption("pwdname", true, "环境变量密码参数名");
        options.addOption("nopwd", false, "无密码启动");
        options.addOption("debug", false, "调试模式");
        options.addOption("del", true, "读取密码后删除密码");

        char[] pwd;

        //读取jar隐藏的密码，无密码启动模式(jar)
        pwd = JarDecryptor.readPassFromJar(new File(JarUtils.getRootPath(null)));

        if (args != null) {
            options.parse(args.split(" "));
            Const.DEBUG = options.hasOption("debug");
        }

        //参数标识 无密码启动
        if (options.hasOption("nopwd")) {
            pwd = new char[1];
            pwd[0] = '#';
        }

        //参数获取密码
        if (StrUtils.isEmpty(pwd)) {
            pwd = options.getOptionValue("pwd", "").toCharArray();
        }

        //参数没密码，读取环境变量中的密码
        if (StrUtils.isEmpty(pwd)) {
            String pwdname = options.getOptionValue("pwdname");
            if (StrUtils.isNotEmpty(pwdname)) {
                String p = System.getenv(pwdname);
                pwd = p == null ? null : p.toCharArray();
            }
        }

        //参数、环境变量都没密码，读取密码配置文件
        if (StrUtils.isEmpty(pwd)) {
            Log.debug("无法从GUI中获取密码，读取密码文件");
            pwd = readPasswordFromFile(options);
        }

        // 配置文件没密码，从控制台获取输入
        if (StrUtils.isEmpty(pwd)) {
            Log.debug("无法在参数中获取密码，从控制台获取");
            Console console = System.console();
            if (console != null) {
                Log.debug("控制台输入");
                pwd = console.readPassword("Password:");
            }
        }

        //不支持控制台输入，弹出gui输入
        if (StrUtils.isEmpty(pwd)) {
            Log.debug("无法从控制台中获取密码，GUI输入");
            InputForm input = new InputForm();
            boolean gui = input.showForm();
            if (gui) {
                Log.debug("GUI输入");
                pwd = input.nextPasswordLine();
                input.closeForm();
            }
        }

        //还是没有获取密码，退出
        if (StrUtils.isEmpty(pwd)) {
            Log.println("\nERROR: Startup failed, could not get the password.\n");
            System.exit(0);
        }

        //验证密码,jar包是才验证
        byte[] passHash = JarDecryptor.readEncryptedFile(new File(JarUtils.getRootPath(null)), Const.CONFIG_PASSHASH);
        if (passHash != null) {
            char[] p1 = StrUtils.toChars(passHash);
            char[] p2 = EncryptUtils.md5(StrUtils.merger(pwd, EncryptUtils.SALT));
            p2 = EncryptUtils.md5(StrUtils.merger(EncryptUtils.SALT, p2));
            if (!StrUtils.equal(p1, p2)) {
                Log.println("\nERROR: Startup failed, invalid password.\n");
                System.exit(0);
            }
        }

        //GO
        if (inst != null) {
            AgentTransformer tran = new AgentTransformer(pwd);
            inst.addTransformer(tran);
        }
    }

    /**
     * 从文件读取密码
     *
     * @param options 参数开关
     * @return 密码
     */
    public static char[] readPasswordFromFile(CmdLineOption options) {
        String path = JarUtils.getRootPath(null);
        if (!path.endsWith(".jar")) {
            return null;
        }
        String jarName = path.substring(path.lastIndexOf("/") + 1);
        path = path.substring(0, path.lastIndexOf("/") + 1);
        String configName = jarName.substring(0, jarName.length() - 3) + "classfinal.txt";
        File config = new File(path, configName);
        if (!config.exists()) {
            config = new File(path, "classfinal.txt");
        }

        String args = null;
        if (config.exists()) {
            args = IoUtils.readTxtFile(config);
        }

        if (StrUtils.isEmpty(args)) {
            return null;
        }

        //不包含空格文件存的就是密码
        if (!args.contains(" ")) {
            return args.trim().toCharArray();
        }

        options.parse(args.trim().split(" "));
        char[] pwd = options.getOptionValue("pwd", "").toCharArray();
        Const.DEBUG = options.hasOption("debug");

        //删除文件中的密码
        if (!"false".equalsIgnoreCase(options.getOptionValue("del"))
                && !"no".equalsIgnoreCase(options.getOptionValue("del"))) {
            args = "";
            IoUtils.writeTxtFile(config, args);
        }
        return pwd;
    }
}
