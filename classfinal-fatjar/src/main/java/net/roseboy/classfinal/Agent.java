package net.roseboy.classfinal;

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
        CoreAgent.premain(args, inst);
    }

    /**
     * 项目启动后动态加载代理
     *
     * @param args args
     * @param inst inst
     */
    public static void agentmain(String args, Instrumentation inst) {

    }

//    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
//        VirtualMachine vm = VirtualMachine.attach("22051");//args[0]传入的是jvm的pid号
//        vm.loadAgent("/Users/roseboy/code-space/classfinal/classfinal-fatjar/target/classfinal-fatjar-1.1.5.jar");
//    }

}