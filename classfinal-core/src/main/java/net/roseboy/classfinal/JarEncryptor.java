package net.roseboy.classfinal;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.roseboy.classfinal.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * java class加密
 *
 * @author roseboy
 */
public class JarEncryptor {

    //要加密的jar或war
    private String jarPath = null;
    //要加密的包，多个用逗号隔开
    private List<String> packages = null;
    //-INF/lib下要加密的jar
    private List<String> includeJars = null;
    //排除的类名
    private List<String> excludeClass = null;
    //密码
    private String password = null;
    //jar还是war
    private String jarOrWar = null;
    //工作目录
    private String targetDir = null;
    //加密的文件数量
    private Integer encryptFileCount = null;

    /**
     * 构造方法
     */
    public JarEncryptor() {
        super();
    }

    /**
     * 构造方法
     *
     * @param jarPath      要加密的jar或war
     * @param packages     要加密的包，多个用逗号隔开
     * @param includeJars  -INF/lib下要加密的jar
     * @param excludeClass 排除的类名
     * @param password     密码
     */
    public JarEncryptor(String jarPath, String password, List<String> packages, List<String> includeJars, List<String> excludeClass) {
        super();
        this.jarPath = jarPath;
        this.packages = packages;
        this.includeJars = includeJars;
        this.excludeClass = excludeClass;
        this.password = password;
    }

    /**
     * 加密jar的主要过程
     *
     * @return 解密后生成的文件的绝对路径
     */
    public String doEncryptJar() {
        if (!jarPath.endsWith(".jar") && !jarPath.endsWith(".war")) {
            throw new RuntimeException("jar/war文件格式有误");
        }
        if (password == null || password.length() == 0) {
            throw new RuntimeException("密码不能为空");
        }
        this.jarOrWar = jarPath.substring(jarPath.lastIndexOf(".") + 1);
        //临时work目录
        this.targetDir = jarPath.replace("." + jarOrWar, Const.LIB_JAR_DIR);

        //[1]释放所有文件，内部jar只释放需要加密的jar
        List<String> allFile = JarUtils.unJar(jarPath, targetDir, includeJars);
        if (Const.DEBUG) {
            for (String file : allFile) {
                Log.debug("释放文件：" + file);
            }
        }

        //[2]按照jar包名分组,只要需要加密的class文件
        Map<String, List<String>> jarClasses = groupByJarName(allFile);
        if (Const.DEBUG) {
            for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
                Log.debug("文件分组：" + entry.getKey() + "  [" + entry.getValue().size() + "]");
            }
        }

        //[3]将正常的class加密，压缩另存
        List<String> encryptClass = encryptClass2(jarClasses);
        encryptFileCount = encryptClass.size();
        if (Const.DEBUG) {
            for (String s : encryptClass) {
                Log.debug("加密文件：" + s);
            }
        }

        //[4]修改class方法体，并保存文件
        clearClassMethod(jarClasses);

        //[5]打包回去
        String result = packageJar(jarClasses);

        return result;
    }


    /**
     * 加密jar的主要过程
     *
     * @param jarPath      要加密的jar或war
     * @param packages     要加密的包
     * @param includeJars  -INF/lib下要加密的jar
     * @param excludeClass 排除的类名
     * @param password     密码
     * @return 加密后文件的路径
     */
    public String doEncryptJar(String jarPath, String password, List<String> packages, List<String> includeJars, List<String> excludeClass) {
        this.jarPath = jarPath;
        this.packages = packages;
        this.includeJars = includeJars;
        this.excludeClass = excludeClass;
        this.password = password;
        return this.doEncryptJar();
    }


    /**
     * 按照jar名分组并且过滤掉不符合要求的class
     *
     * @param allFile jar内的所有文件绝对路径
     * @return jar分组的类名
     */
    private Map<String, List<String>> groupByJarName(List<String> allFile) {
        //需要加密的jar名与jar下的所有class类名
        Map<String, List<String>> jarClasses = new HashMap<>(200);

        for (String file : allFile) {
            if (!file.endsWith(".class")) {//class文件
                continue;
            }
            file = file.replace(targetDir, "");
            file = file.startsWith(Const.FILE_SEPARATOR) ? file.substring(1) : file;
            file = file.replace(Const.FILE_SEPARATOR, ".");
            file = file.substring(0, file.length() - 6);
            String jarName;
            String clsName;
            if ((file.contains("BOOT-INF.lib") || file.contains("WEB-INF.lib")) && file.contains(Const.LIB_JAR_DIR)) {
                //lib的jar包内的
                file = file.replace("BOOT-INF.lib.", "").replace("WEB-INF.lib.", "");
                jarName = file.substring(0, file.indexOf(Const.LIB_JAR_DIR));
                clsName = file.substring(file.indexOf(Const.LIB_JAR_DIR) + Const.LIB_JAR_DIR.length() + 1);
            } else if (file.contains("BOOT-INF.classes") || file.contains("WEB-INF.classes")) {
                //jar/war包-INF/classes下的class文件
                file = file.replace("BOOT-INF.classes.", "").replace("WEB-INF.classes.", "");
                jarName = "CLASSES";
                clsName = file;
            } else {
                //jar包下的class文件
                jarName = "ROOT";
                clsName = file;
            }

            //判断是否是需要加密的包，是不是排除的类
            if (ClassUtils.isPackage(packages, clsName) && (excludeClass == null || excludeClass.size() == 0 || !excludeClass.contains(clsName))) {
                List<String> jarCls = jarClasses.get(jarName);
                jarCls = jarCls == null ? new ArrayList<>() : jarCls;
                jarCls.add(clsName);
                jarClasses.put(jarName, jarCls);
            }
        }
        return jarClasses;
    }

    /**
     * 加密class文件并压缩成一个文件，放在META-INF里
     *
     * @param jarClasses 分组好的jar和jar下的class
     * @return 已经加密的类名
     */
    private List<String> encryptClass(Map<String, List<String>> jarClasses) {
        //已经加密的类名
        List<String> encryptClasses = new ArrayList<>();
        ZipOutputStream zos = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(targetDir + Const.FILE_SEPARATOR + "META-INF" + Const.FILE_SEPARATOR + Const.FILE_NAME));
            zos = new ZipOutputStream(out);
            for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
                for (String classname : entry.getValue()) {
                    String classPath = targetDir + Const.FILE_SEPARATOR + realPath(entry.getKey(), classname, jarOrWar);
                    File sourceFile = new File(classPath);
                    zos.putNextEntry(new ZipEntry(classname));
                    byte[] bytes = IoUtils.readFileToByte(sourceFile);
                    bytes = EncryptUtils.en(bytes, password + classname, 1);
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                    encryptClasses.add(classname);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zos, out);
        }
        return encryptClasses;
    }

    /**
     * 加密class文件不压缩，放在META-INF/classes里
     *
     * @param jarClasses 分组好的jar和jar下的class
     * @return 已经加密的类名
     */
    private List<String> encryptClass2(Map<String, List<String>> jarClasses) {
        //已经加密的类名
        List<String> encryptClasses = new ArrayList<>();

        File metaInfoClasses = new File(targetDir + Const.FILE_SEPARATOR + "META-INF" + Const.FILE_SEPARATOR + Const.FILE_NAME);
        if (!metaInfoClasses.exists()) {
            metaInfoClasses.mkdirs();
        }

        for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
            for (String classname : entry.getValue()) {
                String classPath = targetDir + Const.FILE_SEPARATOR + realPath(entry.getKey(), classname, jarOrWar);
                File sourceFile = new File(classPath);
                byte[] bytes = IoUtils.readFileToByte(sourceFile);
                bytes = EncryptUtils.en(bytes, password + classname, 1);
                File targetFile = new File(metaInfoClasses, classname);
                IoUtils.writeFile(targetFile, bytes);
                encryptClasses.add(classname);
            }
        }

        return encryptClasses;
    }

    /**
     * 清空class文件的方法体，并保留参数信息
     *
     * @param jarClasses 分组好的jar和jar下的class
     * @throws NotFoundException NotFoundException
     */
    private void clearClassMethod(Map<String, List<String>> jarClasses) {
        String libpath = targetDir + Const.FILE_SEPARATOR + ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF") + Const.FILE_SEPARATOR + "lib" + Const.FILE_SEPARATOR;
        try {
            for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
                //初始化javassist
                ClassPool pool = ClassPool.getDefault();
                //lib目录
                ClassUtils.loadClassPath(pool, new String[]{libpath});
                //要修改的class所在的目录
                pool.insertClassPath(targetDir + Const.FILE_SEPARATOR + realPath(entry.getKey(), null, jarOrWar));
                //修改class方法体，并保存文件
                for (String classname : entry.getValue()) {
                    byte[] bts = ClassUtils.rewriteAllMethods(pool, classname);
                    if (bts != null) {
                        String path = targetDir + Const.FILE_SEPARATOR + realPath(entry.getKey(), classname, jarOrWar);
                        IoUtils.writeFile(new File(path), bts);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * @param jarClasses 分组好的jar和jar下的class
     * @return 打包后的jar绝对路径
     */
    private String packageJar(Map<String, List<String>> jarClasses) {
        String jarOrWar = jarPath.substring(jarPath.lastIndexOf(".") + 1);
        String libpath = targetDir + Const.FILE_SEPARATOR + ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF") + Const.FILE_SEPARATOR + "lib" + Const.FILE_SEPARATOR;

        //[1]先打包lib下的jar
        for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
            if (!"CLASSES".equals(entry.getKey()) && !"ROOT".equals(entry.getKey())) {
                JarUtils.doJar(libpath + entry.getKey() + Const.LIB_JAR_DIR, libpath + entry.getKey() + ".jar");
            }
        }

        //[2]删除内部jar解压出来的目录
        for (String file : includeJars) {
            File dir = new File(libpath + file.replace(".jar", Const.LIB_JAR_DIR));
            if (dir.exists()) {
                IoUtils.delete(dir);
            }
        }

        //[3]再打包jar
        String result = JarUtils.doJar(targetDir, jarPath.replace("." + jarOrWar, "-encrypted." + jarOrWar));

        //[4]删除jar解压出来的目录
        File dir = new File(targetDir);
        if (dir.exists()) {
            IoUtils.delete(dir);
        }
        return result;
    }

    /**
     * 根据类名和jar名还原真是路径
     *
     * @param jar
     * @param className
     * @return class的绝对路径
     */
    private String realPath(String jar, String className, String warOrJar) {
        String path;
        String inf = "jar".equals(warOrJar) ? "BOOT-INF" : "WEB-INF";
        if ("ROOT".equals(jar)) {
            path = "";
        } else if ("CLASSES".equals(jar)) {
            path = inf + Const.FILE_SEPARATOR + "classes";
        } else {
            path = inf + Const.FILE_SEPARATOR + "lib" + Const.FILE_SEPARATOR + jar + Const.LIB_JAR_DIR;
        }
        if (className == null || className.length() == 0) {
            return path;
        }
        path = path + (path.length() == 0 ? "" : Const.FILE_SEPARATOR) + className.replace(".", Const.FILE_SEPARATOR) + ".class";
        return path;
    }

    /**
     * 获取加密的文件数量
     *
     * @return 数量
     */
    public Integer getEncryptFileCount() {
        return encryptFileCount;
    }
}
