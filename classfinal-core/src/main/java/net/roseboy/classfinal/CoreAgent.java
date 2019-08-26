package net.roseboy.classfinal;

import net.roseboy.classfinal.util.CmdLineOption;

import java.io.Console;
import java.lang.instrument.Instrumentation;

/**
 * 监听类加载
 * <p>
 * 启动jar  java -javaagent:this.jar='-pwd 0000000' -jar aa.jar
 * <p>
 *
 * @author roseboy
 */
public class CoreAgent {
    /**
     * man方法执行前调用
     *
     * @param args 参数
     * @param inst inst
     * @throws Exception Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        Const.printDog();
        CmdLineOption options = new CmdLineOption();
        options.addOption("pwd", true, "密码");
        options.addOption("debug", false, "调试模式");

        char[] pwd = null;
        if (args != null) {
            options.parse(args.split(" "));
            pwd = options.getOptionValue("pwd", "").toCharArray();
            Const.DEBUG = options.hasOption("debug");
        }

        if (pwd == null || pwd.length == 0) {
            Console console = System.console();
            if (console == null) {
                InputForm input = new InputForm();
                input.showForm();
                pwd = input.nextPasswordLine();
                input.closeForm();
                //System.out.println("\nStartup failed,invalid password.\n");
                //System.exit(0);
            } else {
                pwd = console.readPassword("Password:");
            }
        }

        if (inst != null) {
            AgentTransformer tran = new AgentTransformer(pwd);
            inst.addTransformer(tran);
        }
    }
}
