package net.roseboy.classfinal;

import net.roseboy.classfinal.util.JarUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * 监听类加载
 * <p>
 * 配置 -javaagent:this.jar='-data classes1.dat,classes2.dat -pwd 123123,000000'
 * 启动jar  java -javaagent:this.jar='-data aa.jar -pwd 0000000' -jar aa.jar
 * <p>
 * java -javaagent:/Users/roseboy/code-space/agent/target/agent-1.0.jar='-data /Users/roseboy/work-yiyon/易用框架/yiyon-server-liuyuan/yiyon-package-liuyuan/target/yiyon-package-liuyuan-1.0.0-encrypted.jar -pwd 000000' -jar yiyon-package-liuyuan-1.0.0-encrypted.jar
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
        Constants.printDog();

        Options options = new Options();
        options.addOption("data", true, "加密后的文件(多个用,分割)");
        options.addOption("pwd", true, "密码(多个用,分割)");

        String file = null;
        String pwd = null;
        if (args != null) {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args.split(" "));
            file = cmd.getOptionValue("data");
            pwd = cmd.getOptionValue("pwd");
        }

        if (file == null || file.length() == 0 || pwd == null || pwd.length() == 0) {
            return;
        }

        String[] files = file.split(",");
        String[] pwds = pwd.split(",");

        if (files.length != pwds.length) {
            throw new RuntimeException("加密文件和密码个数不一致");
        }

        for (int i = 0; i < files.length; i++) {
            //jar解压出classes.dat
            if (files[i].endsWith(".jar")) {
                File classesDat = new File(files[i].substring(0, files[i].length() - 4) + "." + Constants.FILE_NAME);
                files[i] = JarUtils.releaseFileFromJar(new File(files[i]), "META-INF" + Constants.FILE_SEPARATOR + Constants.FILE_NAME, classesDat);
            }
            //war tomcat会自动解压，在META-INF下找classes.dat
            else if (files[i].endsWith(".war")) {
                files[i] = files[i].substring(0, files[i].length() - 4) + Constants.FILE_SEPARATOR + "META-INF" + Constants.FILE_SEPARATOR + Constants.FILE_NAME;
            }
        }

        AgentTransformer tran = new AgentTransformer(files, pwds);
        inst.addTransformer(tran);
    }

}