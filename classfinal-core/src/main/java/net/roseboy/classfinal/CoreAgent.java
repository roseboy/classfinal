package net.roseboy.classfinal;

import net.roseboy.classfinal.util.*;

import java.io.Console;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

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
        options.addOption("debug", false, "调试模式");
        options.addOption("del", true, "读取密码后删除密码");

        char[] pwd = null;
        if (args != null) {
            options.parse(args.split(" "));
            pwd = options.getOptionValue("pwd", "").toCharArray();
            Const.DEBUG = options.hasOption("debug");
        }

        // 参数没密码，从控制台获取输入
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

        //不支持gui，读取密码配置文件
        if (StrUtils.isEmpty(pwd)) {
            Log.debug("无法从GUI中获取密码，读取密码文件");
            pwd = readPasswordFromFile(options);
        }

        //还是没有获取密码，退出
        if (StrUtils.isEmpty(pwd)) {
            Log.println("\nERROR: Startup failed, could not get the password.\n");
            System.exit(0);
        }

        if (inst != null) {
            AgentTransformer tran = new AgentTransformer(pwd);
            inst.addTransformer(tran);
        }
    }

    /**
     * 从文件读取密码
     *
     * @return 密码
     */
    public static char[] readPasswordFromFile(CmdLineOption options) {
        String path = ClassUtils.getRootPath();
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
            Log.println("\nCould not get the password.");
            Log.println("You can write the password(-pwd 123456 -del true) into the '" + path + "classfinal.txt' or '" + path + configName + "'.");
            return null;
        }
        if (!args.contains(" ")) {
            return args.toCharArray();
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
