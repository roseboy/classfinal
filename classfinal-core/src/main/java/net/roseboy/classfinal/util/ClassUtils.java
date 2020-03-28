package net.roseboy.classfinal.util;

import javassist.*;
import javassist.bytecode.*;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 字节码操作工具类
 *
 * @author roseboy
 */
public class ClassUtils {

    /**
     * 清空方法
     *
     * @param pool      javassist的ClassPool
     * @param classname 要修改的class全名
     * @return 返回方法体的字节
     */
    public static byte[] rewriteAllMethods(ClassPool pool, String classname) {
        String name = null;
        try {
            CtClass cc = pool.getCtClass(classname);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod m : methods) {
                name = m.getName();
                //不是构造方法，在当前类，不是父lei
                if (!m.getName().contains("<") && m.getLongName().startsWith(cc.getName())) {
                    //m.setBody(null);//清空方法体
                    CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
                    //接口的ca就是null,方法体本来就是空的就是-79
                    if (ca != null && ca.getCodeLength() != 1 && ca.getCode()[0] != -79) {
                        ClassUtils.setBodyKeepParamInfos(m, null, true);
                        if ("void".equalsIgnoreCase(m.getReturnType().getName()) && m.getLongName().endsWith(".main(java.lang.String[])") && m.getMethodInfo().getAccessFlags() == 9) {
                            m.insertBefore("System.out.println(\"\\nStartup failed, invalid password.\\n\");");
                        }

                    }

                }
            }
            return cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException("[" + classname + "(" + name + ")]" + e.getMessage());
        }
    }

    /**
     * 修改方法体，并且保留参数信息
     *
     * @param m       javassist的方法
     * @param src     java代码
     * @param rebuild 是否重新构建
     * @throws CannotCompileException 编译异常
     */
    public static void setBodyKeepParamInfos(CtMethod m, String src, boolean rebuild) throws CannotCompileException {
        CtClass cc = m.getDeclaringClass();
        if (cc.isFrozen()) {
            throw new RuntimeException(cc.getName() + " class is frozen");
        }
        CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        } else {
            CodeIterator iterator = ca.iterator();
            Javac jv = new Javac(cc);

            try {
                int nvars = jv.recordParams(m.getParameterTypes(), Modifier.isStatic(m.getModifiers()));
                jv.recordParamNames(ca, nvars);
                jv.recordLocalVariables(ca, 0);
                jv.recordReturnType(Descriptor.getReturnType(m.getMethodInfo().getDescriptor(), cc.getClassPool()), false);
                //jv.compileStmnt(src);
                //Bytecode b = jv.getBytecode();
                Bytecode b = jv.compileBody(m, src);
                int stack = b.getMaxStack();
                int locals = b.getMaxLocals();
                if (stack > ca.getMaxStack()) {
                    ca.setMaxStack(stack);
                }

                if (locals > ca.getMaxLocals()) {
                    ca.setMaxLocals(locals);
                }
                int pos = iterator.insertEx(b.get());
                iterator.insert(b.getExceptionTable(), pos);
                if (rebuild) {
                    m.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
                }
            } catch (NotFoundException var12) {
                throw new CannotCompileException(var12);
            } catch (CompileError var13) {
                throw new CannotCompileException(var13);
            } catch (BadBytecode var14) {
                throw new CannotCompileException(var14);
            }
        }
    }

    /**
     * 加载jar包路径
     *
     * @param pool  javassist的ClassPool
     * @param paths lib路径，
     */
    public static void loadClassPath(ClassPool pool, List<String> paths) {
        for (String path : paths) {
            loadClassPath(pool, new File(path));
        }
    }

    /**
     * 加载jar包路径
     *
     * @param pool javassist的ClassPool
     * @param dir  lib路径或jar文件
     */
    public static void loadClassPath(ClassPool pool, File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }

        if (dir.isDirectory()) {
            List<File> jars = new ArrayList<>();
            IoUtils.listFile(jars, dir, ".jar");
            for (File jar : jars) {
                try {
                    pool.insertClassPath(jar.getAbsolutePath());
                } catch (NotFoundException e) {
                    //ignore
                }
            }
        } else if (dir.getName().endsWith(".jar")) {
            try {
                pool.insertClassPath(dir.getAbsolutePath());
            } catch (NotFoundException e) {
                //ignore
            }
        }
    }

    /**
     * 给方法插入代码并返回bytecode的字节数组
     *
     * @param classMethod 类名#方法名
     * @param javaCode    代码
     * @param line        行数
     * @param libDir      classpath
     * @param thisJar     本项目的jar路径
     * @return 修改后的字节数组
     * @throws Exception Exception
     */
    public static byte[] insertCode(String classMethod, String javaCode, int line, File libDir, File thisJar) throws Exception {
        String className = classMethod.split("#")[0];
        String methodName = classMethod.split("#")[1];
        ClassPool pool = ClassPool.getDefault();
        loadClassPath(pool, libDir);
        if (thisJar != null && thisJar.exists()) {
            loadClassPath(pool, thisJar);
        }
        byte[] bytes;
        CtClass cc = pool.getCtClass(className);
        if (methodName.startsWith("<") && methodName.contains(">")) {
            methodName = methodName.replace("<", "").replace(">", "");
            CtConstructor[] ms = cc.getConstructors();
            for (CtConstructor mt : ms) {
                if (mt.getLongName().endsWith(methodName)) {
                    mt.insertAt(line, javaCode);
                }
            }
        } else {
            CtMethod mt = cc.getDeclaredMethod(methodName);
            mt.insertAt(line, javaCode);
        }
        bytes = cc.toBytecode();
        return bytes;
    }

}
