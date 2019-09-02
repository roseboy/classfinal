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
}