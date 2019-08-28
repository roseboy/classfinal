package net.roseboy.classfinal;

import javassist.ClassPool;
import net.roseboy.classfinal.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private char[] password = null;
    //jar还是war
    private String jarOrWar = null;
    //工作目录
    private File targetDir = null;
    //-INF/lib目录
    private File targetLibDir = null;
    //加密的文件数量
    private Integer encryptFileCount = null;
    //存储解析出来的类名和路径
    private Map<String, String> resolveClassName = new HashMap<>();

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
    public JarEncryptor(String jarPath, char[] password, List<String> packages,
                        List<String> includeJars, List<String> excludeClass) {
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
        if (!new File(jarPath).exists()) {
            throw new RuntimeException("文件不存在:" + jarPath);
        }
        if (password == null || password.length == 0) {
            throw new RuntimeException("密码不能为空");
        }
        this.jarOrWar = jarPath.substring(jarPath.lastIndexOf(".") + 1);
        Log.debug("加密类型：" + jarOrWar);
        //临时work目录
        this.targetDir = new File(jarPath.replace("." + jarOrWar, Const.LIB_JAR_DIR));
        this.targetLibDir = new File(this.targetDir, ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF") + File.separator + "lib");
        Log.debug("临时目录：" + targetDir);

        //[1]释放所有文件，内部jar只释放需要加密的jar
        List<String> allFile = JarUtils.unJar(jarPath, this.targetDir.getAbsolutePath(), includeJars);
        for (String s : allFile) {
            Log.debug("释放：" + s);
        }

        //[2]提取所有需要加密的class文件
        List<File> classFiles = filterClasses(allFile);

        //[3]将本项目的代码添加至jar中
        if ("jar".equalsIgnoreCase(this.jarOrWar)) {
            addClassFinalAgent();
        }

        //[4]将正常的class加密，压缩另存
        List<String> encryptClass = encryptClass(classFiles);
        encryptFileCount = encryptClass.size();

        //[5]修改class方法体，并保存文件
        clearClassMethod(classFiles);

        //[6]打包回去
        String result = packageJar();

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
    public String doEncryptJar(String jarPath, char[] password, List<String> packages,
                               List<String> includeJars, List<String> excludeClass) {
        this.jarPath = jarPath;
        this.packages = packages;
        this.includeJars = includeJars;
        this.excludeClass = excludeClass;
        this.password = password;
        return this.doEncryptJar();
    }

    /**
     * 找出所有需要加密的class文件
     *
     * @param allFile 所有文件
     * @return 待加密的class列表
     */
    public List<File> filterClasses(List<String> allFile) {
        List<File> classFiles = new ArrayList<>();
        for (String file : allFile) {
            if (!file.endsWith(".class")) {
                continue;
            }
            //解析出类全名
            String className = resolveClassName(file, true);
            //判断包名相同和是否排除的类
            if (ClassUtils.isPackage(this.packages, className) && !ClassUtils.isClass(this.excludeClass, className)) {
                classFiles.add(new File(file));
                Log.debug("待加密: " + file);
            }
        }
        return classFiles;
    }

    /**
     * 加密class文件不，放在META-INF/classes里
     *
     * @param classFiles jar/war 下需要加密的class文件
     * @return 已经加密的类名
     */
    private List<String> encryptClass(List<File> classFiles) {
        List<String> encryptClasses = new ArrayList<>();

        //加密后存储的位置
        File metaDir = new File(this.targetDir, "META-INF" + File.separator + Const.FILE_NAME);
        if (!metaDir.exists()) {
            metaDir.mkdirs();
        }

        //加密另存
        for (File classFile : classFiles) {
            String className = resolveClassName(classFile.getAbsolutePath(), true);
            byte[] bytes = IoUtils.readFileToByte(classFile);
            bytes = EncryptUtils.en(bytes, IoUtils.merger(password, className.toCharArray()), 1);
            File targetFile = new File(metaDir, className);
            IoUtils.writeFile(targetFile, bytes);
            encryptClasses.add(className);
            Log.debug("加密：" + className);
        }

        return encryptClasses;
    }

    /**
     * 清空class文件的方法体，并保留参数信息
     *
     * @param classFiles jar/war 下需要加密的class文件
     */
    private void clearClassMethod(List<File> classFiles) {
        try {
            //初始化javassist
            ClassPool pool = ClassPool.getDefault();
            //lib目录
            ClassUtils.loadClassPath(pool, new File[]{this.targetLibDir});
            Log.debug("ClassPath: " + this.targetLibDir);
            List<String> classPaths = new ArrayList<>();
            for (File classFile : classFiles) {
                //要修改的class所在的目录
                String classPath = resolveClassName(classFile.getAbsolutePath(), false);
                if (!classPaths.contains(classPath)) {
                    pool.insertClassPath(classPath);
                    classPaths.add(classPath);
                    Log.debug("ClassPath: " + classPath);
                }

                //解析出类全名
                String className = resolveClassName(classFile.getAbsolutePath(), true);
                //修改class方法体，并保存文件
                byte[] bts = null;
                try {
                    bts = ClassUtils.rewriteAllMethods(pool, className);
                } catch (Exception e) {
                    Log.debug("ERROR:" + e.getMessage());
                }
                if (bts != null) {
                    IoUtils.writeFile(classFile, bts);
                    Log.debug("清除方法体: " + className);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 压缩成jar
     *
     * @return 打包后的jar绝对路径
     */
    private String packageJar() {
        //[1]先打包lib下的jar
        for (String jar : includeJars) {
            if (!jar.endsWith(".jar")) {
                continue;
            }
            String jarFile = this.targetLibDir.getAbsolutePath() + File.separator + jar;
            String jarDir = jarFile.substring(0, jarFile.length() - 4) + Const.LIB_JAR_DIR;
            if (!new File(jarDir).exists()) {
                continue;
            }
            JarUtils.doJar(jarDir, jarFile);
            IoUtils.delete(new File(jarDir));
            Log.debug("打包: " + jarFile);
        }

        //删除meta-inf下的maven
        IoUtils.delete(new File(this.targetDir, "META-INF/maven"));

        //[2]再打包jar
        String enctyptedJar = jarPath.replace("." + jarOrWar, "-encrypted." + jarOrWar);
        String result = JarUtils.doJar(this.targetDir.getAbsolutePath(), enctyptedJar);
        IoUtils.delete(this.targetDir);
        Log.debug("打包: " + enctyptedJar);
        return result;
    }

    /**
     * 向jar文件中添加classfinal的代码
     */
    public void addClassFinalAgent() {
        List<String> paths = new ArrayList<>();
        String p1 = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String p2 = ClassPool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        paths.add(p1);
        paths.add(p2);
        for (String path : paths) {
            if (path.endsWith(".jar")) {
                List<String> excludeFiles = new ArrayList<>();
                excludeFiles.add("META-INF/MANIFEST.MF");
                excludeFiles.add("说明.txt");
                JarUtils.unJar(path, this.targetDir.getAbsolutePath(), null, excludeFiles);
            } else if (path.endsWith("/classes/")) {
                List<File> files = new ArrayList<>();
                File pathFile = new File(path);
                IoUtils.listFile(files, new File(path));
                for (File file : files) {
                    path = file.getAbsolutePath().substring(pathFile.getAbsolutePath().length());
                    File targetFile = new File(this.targetDir, path);
                    if (file.isDirectory()) {
                        targetFile.mkdirs();
                    } else {
                        byte[] bytes = IoUtils.readFileToByte(file);
                        IoUtils.writeFile(targetFile, bytes);
                    }
                }
            }
        }

        //把javaagent信息加入到MANIFEST.MF
        File manifest = new File(this.targetDir, "META-INF/MANIFEST.MF");
        String[] txts = IoUtils.readTxtFile(manifest).split("\r\n");
        StringBuffer newTxt = new StringBuffer();
        for (int i = 0; i < txts.length; i++) {
            newTxt.append(txts[i]).append("\r\n");
            if (txts[i].startsWith("Main-Class:")) {
                newTxt.append("Premain-Class: ").append(CoreAgent.class.getName()).append("\r\n");
            }
        }
        newTxt.append("\r\n\r\n");
        IoUtils.writeTxtFile(manifest, newTxt.toString());
    }

    /**
     * 根据class的绝对路径解析出class名称或class包所在的路径
     *
     * @param fileName    class绝对路径
     * @param classOrPath true|false
     * @return class名称|包所在的路径
     */
    private String resolveClassName(String fileName, boolean classOrPath) {
        String result = resolveClassName.get(fileName + classOrPath);
        if (result != null) {
            return result;
        }
        String file = fileName.substring(0, fileName.length() - 6);
        String K_CLASSES = File.separator + "classes" + File.separator;
        String K_LIB = File.separator + "lib" + File.separator;

        String clsPath;
        String clsName;
        //lib内的的jar包
        if (file.contains(K_LIB)) {
            clsName = file.substring(file.indexOf(Const.LIB_JAR_DIR, file.indexOf(K_LIB)) + Const.LIB_JAR_DIR.length() + 1);
            clsPath = file.substring(0, file.length() - clsName.length() - 1);
        }
        //jar/war包-INF/classes下的class文件
        else if (file.contains(K_CLASSES)) {
            clsName = file.substring(file.indexOf(K_CLASSES) + K_CLASSES.length());
            clsPath = file.substring(0, file.length() - clsName.length() - 1);

        }
        //jar包下的class文件
        else {
            clsName = file.substring(file.indexOf(Const.LIB_JAR_DIR) + Const.LIB_JAR_DIR.length() + 1);
            clsPath = file.substring(0, file.length() - clsName.length() - 1);
        }
        result = classOrPath ? clsName.replace(File.separator, ".") : clsPath;
        resolveClassName.put(fileName + classOrPath, result);
        return result;
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
