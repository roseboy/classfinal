package net.roseboy.classfinal.util;

import javassist.*;
import javassist.bytecode.*;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import net.roseboy.classfinal.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字节码操作工具类
 *
 * @author roseboy
 * @date 2019-08-01
 */
public class ClassUtils {

    /**
     * 清空方法
     *
     * @param pool
     * @param classname
     */
    public static byte[] rewriteMethod(ClassPool pool, String classname) {
        String name = "";
        try {
            CtClass cc = pool.getCtClass(classname);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod m : methods) {
                name = m.getName();
                //不是构造方法，在当前类，不是父lei
                if (!m.getName().contains("<") && m.getLongName().startsWith(cc.getName())) {
                    //System.out.println(m.getLongName());
                    //m.setBody(null);//清空方法体
                    CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
                    //接口的ca就是null,方法体本来就是空的就是-79
                    if (ca != null && ca.getCodeLength() != 1 && ca.getCode()[0] != -79) {
                        ClassUtils.setBodyKeepParamInfos(m, null, true);
                    }

                }
            }
            return cc.toBytecode();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("ERROR:[" + classname + "(" + name + ")]" + e.getMessage());
        }
        return null;
    }

    /**
     * 清空方法体，并且保留参数信息
     *
     * @param m
     * @param src
     * @param rebuild
     * @throws CannotCompileException
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
     * @param pool
     * @param paths
     * @throws NotFoundException
     */
    public static void loadClassPath(ClassPool pool, String[] paths) throws NotFoundException {
        for (String path : paths) {
            List<File> jars = new ArrayList<>();
            File dir = new File(path);
            if (dir.isDirectory()) {
                IOUtils.listFile(jars, dir, ".jar");
                for (File jar : jars) {
                    pool.insertClassPath(jar.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 判断是否是某个包名
     *
     * @param encryptPackage
     * @param className
     * @return
     */
    public static boolean isPackage(String encryptPackage, String className) {
        if (encryptPackage == null || encryptPackage.length() == 0) {
            return false;
        }

        String[] packages = encryptPackage.split(",");
        for (String pkg : packages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据类名和jar名还原真是路径
     *
     * @param jar
     * @param className
     * @return
     */
    public static String realPath(String jar, String className, String warOrJar) {
        String path;
        String INF = "jar".equals(warOrJar) ? "BOOT-INF" : "WEB-INF";
        if ("ROOT".equals(jar)) {
            path = "";
        } else if ("CLASSES".equals(jar)) {
            path = INF + File.separator + "classes";
        } else {
            path = INF + File.separator + "lib" + File.separator + jar + Main.LIB_JAR_DIR;
        }
        if (className == null || className.length() == 0) {
            return path;
        }
        path = path + (path.length() == 0 ? "" : File.separator) + className.replace(".", File.separator) + ".class";
        return path;
    }

    /**
     * 是否垃圾文件
     *
     * @param file
     * @return
     */
    public static boolean isDel(File file) {
        String[] dels = {".DS_Store", "Thumbs.db"};
        for (String f : dels) {
            if (file.getAbsolutePath().endsWith(f)) {
                return true;
            }
        }
        return false;
    }
}
