package net.roseboy.classfinal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.lang.instrument.Instrumentation;

/**
 * 监听类加载
 * <p>
 * 启动jar  java -javaagent:this.jar='-pwd 0000000' -jar aa.jar
 * <p>
 *
 * @author roseboy
 */
public class Agent {

    /**
     * man方法执行前调用
     *
     * @param args 参数
     * @param inst inst
     * @throws Exception Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        Const.printDog();

        Options options = new Options();
        options.addOption("pwd", true, "密码");
        options.addOption("debug", false, "调试模式");

        String pwd = null;
        if (args != null) {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args.split(" "));
            pwd = cmd.getOptionValue("pwd");
            Const.DEBUG = cmd.hasOption("debug");
        }

        AgentTransformer tran = new AgentTransformer(pwd);
        inst.addTransformer(tran);
    }


}