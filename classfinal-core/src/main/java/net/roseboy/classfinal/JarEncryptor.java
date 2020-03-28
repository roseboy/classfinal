package net.roseboy.classfinal;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.roseboy.classfinal.util.*;

import java.io.File;
import java.util.*;

/**
 * java class加密
 *
 * @author roseboy
 */
public class JarEncryptor {
    //加密配置文件：加载配置文件是注入解密代码的配置
    static Map<String, String> aopMap = new HashMap<>();

    static {
        //org.springframework.core.io.ClassPathResource#getInputStream注入解密功能
        aopMap.put("spring.class", "org.springframework.core.io.ClassPathResource#getInputStream");
        aopMap.put("spring.code", "char[] c=${passchar};"
                + "is=net.roseboy.classfinal.JarDecryptor.getInstance().decryptConfigFile(this.path,is,c);");
        aopMap.put("spring.line", "999");

        //com.jfinal.kit.Prop#getInputStream注入解密功能
        aopMap.put("jfinal.class", "com.jfinal.kit.Prop#<Prop>(java.lang.String,java.lang.String)");
        aopMap.put("jfinal.code", "char[] c=${passchar};inputStream=net.roseboy.classfinal.JarDecryptor.getInstance().decryptConfigFile(fileName,inputStream,c);");
        aopMap.put("jfinal.line", "62");
    }

    //要加密的jar或war
    private String jarPath = null;
    //要加密的包，多个用逗号隔开
    private List<String> packages = null;
    //-INF/lib下要加密的jar
    private List<String> includeJars = null;
    //排除的类名
    private List<String> excludeClass = null;
    //依赖jar路径
    private List<String> classPath = null;
    //需要加密的配置文件
    private List<String> cfgfiles = null;
    //密码
    private char[] password = null;
    //机器码
    private char[] code = null;

    //jar还是war
    private String jarOrWar = null;
    //工作目录
    private File targetDir = null;
    //-INF/lib目录
    private File targetLibDir = null;
    //-INF/classes目录
    private File targetClassesDir = null;
    //加密的文件数量
    private Integer encryptFileCount = null;
    //存储解析出来的类名和路径
    private Map<String, String> resolveClassName = new HashMap<>();

    /**
     * 构造方法
     *
     * @param jarPath  要加密的jar或war
     * @param password 密码
     */
    public JarEncryptor(String jarPath, char[] password) {
        super();
        this.jarPath = jarPath;
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
        if (password.length == 1 && password[0] == '#') {
            Log.debug("加密模式：无密码");
        }
        Log.debug("机器绑定：" + (StrUtils.isEmpty(this.code) ? "否" : "是"));

        this.jarOrWar = jarPath.substring(jarPath.lastIndexOf(".") + 1);
        Log.debug("加密类型：" + jarOrWar);
        //临时work目录
        this.targetDir = new File(jarPath.replace("." + jarOrWar, Const.LIB_JAR_DIR));
        this.targetLibDir = new File(this.targetDir, ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF")
                + File.separator + "lib");
        this.targetClassesDir = new File(this.targetDir, ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF")
                + File.separator + "classes");
        Log.debug("临时目录：" + targetDir);

        //[1]释放所有文件
        List<String> allFile = JarUtils.unJar(jarPath, this.targetDir.getAbsolutePath());
        allFile.forEach(s -> Log.debug("释放：" + s));
        //[1.1]内部jar只释放需要加密的jar
        List<String> libJarFiles = new ArrayList<>();
        allFile.forEach(path -> {
            if (!path.toLowerCase().endsWith(".jar")) {
                return;
            }
            String name = path.substring(path.lastIndexOf(File.separator) + 1);
            if (StrUtils.isMatchs(this.includeJars, name, false)) {
                String targetPath = path.substring(0, path.length() - 4) + Const.LIB_JAR_DIR;
                List<String> files = JarUtils.unJar(path, targetPath);
                files.forEach(s -> Log.debug("释放：" + s));
                libJarFiles.add(path);
                libJarFiles.addAll(files);
            }
        });
        allFile.addAll(libJarFiles);

        //压缩静态文件
//        allFile.forEach(s -> {
//            if (!s.endsWith(".ftl")) {
//                return;
//            }
//            File file = new File(s);
//            String code = IoUtils.readTxtFile(file);
//            code = HtmlUtils.removeComments(code);
//            code = HtmlUtils.removeBlankLine(code);
//            IoUtils.writeTxtFile(file, code);
//        });

        //[2]提取所有需要加密的class文件
        List<File> classFiles = filterClasses(allFile);

        //[3]将本项目的代码添加至jar中
        addClassFinalAgent();

        //[4]将正常的class加密，压缩另存
        List<String> encryptClass = encryptClass(classFiles);
        this.encryptFileCount = encryptClass.size();

        //[5]清空class方法体，并保存文件
        clearClassMethod(classFiles);

        //[6]加密配置文件
        encryptConfigFile();

        //[7]打包回去
        String result = packageJar(libJarFiles);

        return result;
    }


    /**
     * 找出所有需要加密的class文件
     *
     * @param allFile 所有文件
     * @return 待加密的class列表
     */
    public List<File> filterClasses(List<String> allFile) {
        List<File> classFiles = new ArrayList<>();
        allFile.forEach(file -> {
            if (!file.endsWith(".class")) {
                return;
            }
            //解析出类全名
            String className = resolveClassName(file, true);
            //判断包名相同和是否排除的类
            if (StrUtils.isMatchs(this.packages, className, false)
                    && !StrUtils.isMatchs(this.excludeClass, className, false)) {
                classFiles.add(new File(file));
                Log.debug("待加密: " + file);
            }
        });
        return classFiles;
    }

    /**
     * 加密class文件，放在META-INF/classes里
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

        //无密码模式,自动生成一个密码
        if (this.password.length == 1 && this.password[0] == '#') {
            char[] randChars = EncryptUtils.randChar(32);
            this.password = EncryptUtils.md5(randChars);
            File configPass = new File(metaDir, Const.CONFIG_PASS);
            IoUtils.writeFile(configPass, StrUtils.toBytes(randChars));
        }

        //有机器码
        if (StrUtils.isNotEmpty(this.code)) {
            File configCode = new File(metaDir, Const.CONFIG_CODE);
            IoUtils.writeFile(configCode, StrUtils.toBytes(EncryptUtils.md5(this.code)));
        }

        //加密另存
        classFiles.forEach(classFile -> {
            String className = classFile.getName();
            if (className.endsWith(".class")) {
                className = resolveClassName(classFile.getAbsolutePath(), true);
            }
            byte[] bytes = IoUtils.readFileToByte(classFile);
            char[] pass = StrUtils.merger(this.password, className.toCharArray());
            bytes = EncryptUtils.en(bytes, pass, Const.ENCRYPT_TYPE);
            //有机器码，再用机器码加密一遍
            if (StrUtils.isNotEmpty(this.code)) {
                pass = StrUtils.merger(className.toCharArray(), this.code);
                bytes = EncryptUtils.en(bytes, pass, Const.ENCRYPT_TYPE);
            }
            File targetFile = new File(metaDir, className);
            IoUtils.writeFile(targetFile, bytes);
            encryptClasses.add(className);
            Log.debug("加密：" + className);
        });

        //加密密码hash存储，用来验证密码是否正确
        char[] pchar = EncryptUtils.md5(StrUtils.merger(this.password, EncryptUtils.SALT));
        pchar = EncryptUtils.md5(StrUtils.merger(EncryptUtils.SALT, pchar));
        IoUtils.writeFile(new File(metaDir, Const.CONFIG_PASSHASH), StrUtils.toBytes(pchar));

        return encryptClasses;
    }

    /**
     * 清空class文件的方法体，并保留参数信息
     *
     * @param classFiles jar/war 下需要加密的class文件
     */
    private void clearClassMethod(List<File> classFiles) {
        //初始化javassist
        ClassPool pool = ClassPool.getDefault();
        //[1]把所有涉及到的类加入到ClassPool的classpath
        //[1.1]lib目录所有的jar加入classpath
        ClassUtils.loadClassPath(pool, this.targetLibDir);
        Log.debug("ClassPath: " + this.targetLibDir.getAbsolutePath());

        //[1.2]外部依赖的lib加入classpath
        ClassUtils.loadClassPath(pool, this.classPath);
        this.classPath.forEach(classPath -> Log.debug("ClassPath: " + classPath));

        //[1.3]要修改的class所在的目录（-INF/classes 和 libjar）加入classpath
        List<String> classPaths = new ArrayList<>();
        classFiles.forEach(classFile -> {
            String classPath = resolveClassName(classFile.getAbsolutePath(), false);
            if (classPaths.contains(classPath)) {
                return;
            }
            try {
                pool.insertClassPath(classPath);
            } catch (NotFoundException e) {
                //Ignore
            }
            classPaths.add(classPath);
            Log.debug("ClassPath: " + classPath);

        });

        //[2]修改class方法体，并保存文件
        classFiles.forEach(classFile -> {
            //解析出类全名
            String className = resolveClassName(classFile.getAbsolutePath(), true);
            byte[] bts = null;
            try {
                Log.debug("清除方法体: " + className);
                bts = ClassUtils.rewriteAllMethods(pool, className);
            } catch (Exception e) {
                Log.debug("ERROR:" + e.getMessage());
            }
            if (bts != null) {
                IoUtils.writeFile(classFile, bts);
            }
        });
    }

    /**
     * 向jar文件中添加classfinal的代码
     */
    public void addClassFinalAgent() {
        List<String> thisJarPaths = new ArrayList<>();
        thisJarPaths.add(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        //paths.add(ClassPool.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        //把本项目的class文件打包进去
        thisJarPaths.forEach(thisJar -> {
            File thisJarFile = new File(thisJar);
            if ("jar".endsWith(this.jarOrWar) && thisJar.endsWith(".jar")) {
                List<String> includeFiles = Arrays.asList(Const.CLASSFINAL_FILES);
                JarUtils.unJar(thisJar, this.targetDir.getAbsolutePath(), includeFiles);
            } else if ("war".endsWith(this.jarOrWar) && thisJar.endsWith(".jar")) {
                File targetClassFinalJar = new File(this.targetLibDir, thisJarFile.getName());
                byte[] bytes = IoUtils.readFileToByte(thisJarFile);
                IoUtils.writeFile(targetClassFinalJar, bytes);
            }
            //本项目开发环境中未打包
            else if (thisJar.endsWith("/classes/")) {
                List<File> files = new ArrayList<>();
                IoUtils.listFile(files, new File(thisJar));
                files.forEach(file -> {
                    String className = file.getAbsolutePath().substring(thisJarFile.getAbsolutePath().length());
                    File targetFile = "jar".equals(this.jarOrWar) ? this.targetDir : this.targetClassesDir;
                    targetFile = new File(targetFile, className);
                    if (file.isDirectory()) {
                        targetFile.mkdirs();
                    } else if (StrUtils.containsArray(file.getAbsolutePath(), Const.CLASSFINAL_FILES)) {
                        byte[] bytes = IoUtils.readFileToByte(file);
                        IoUtils.writeFile(targetFile, bytes);
                    }
                });
            }
        });

        //把javaagent信息加入到MANIFEST.MF
        File manifest = new File(this.targetDir, "META-INF/MANIFEST.MF");
        String preMain = "Premain-Class: " + CoreAgent.class.getName();
        String[] txts = {};
        if (manifest.exists()) {
            txts = IoUtils.readTxtFile(manifest).split("\r\n");
        }

        String str = StrUtils.insertStringArray(txts, preMain, "Main-Class:");
        IoUtils.writeTxtFile(manifest, str + "\r\n\r\n");
    }

    /**
     * 加密classes下的配置文件
     */
    private void encryptConfigFile() {
        if (this.cfgfiles == null || this.cfgfiles.size() == 0) {
            return;
        }

        //支持的框架
        //String[] supportFrame = {"spring", "jfinal"};
        String[] supportFrame = {"spring"};
        //需要注入解密功能的class
        List<File> aopClass = new ArrayList<>(supportFrame.length);

        // [1].读取配置文件时解密
        Arrays.asList(supportFrame).forEach(name -> {
            String javaCode = aopMap.get(name + ".code");
            String clazz = aopMap.get(name + ".class");
            Integer line = Integer.parseInt(aopMap.get(name + ".line"));
            javaCode = javaCode.replace("${passchar}", StrUtils.toCharArrayCode(this.password));
            byte[] bytes = null;
            try {
                String thisJar = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                //获取 框架 读取 配置文件的类,将密码注入该类
                bytes = ClassUtils.insertCode(clazz, javaCode, line, this.targetLibDir, new File(thisJar));
            } catch (Exception e) {
                e.printStackTrace();
                Log.debug(e.getClass().getName() + ":" + e.getMessage());
            }
            if (bytes != null) {
                File cls = new File(this.targetDir, clazz.split("#")[0] + ".class");
                IoUtils.writeFile(cls, bytes);
                aopClass.add(cls);
            }
        });

        //加密读取配置文件的类
        this.encryptClass(aopClass);
        aopClass.forEach(cls -> cls.delete());


        //[2].加密配置文件
        List<File> configFiles = new ArrayList<>();
        File[] files = this.targetClassesDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && StrUtils.isMatchs(this.cfgfiles, file.getName(), false)) {
                configFiles.add(file);
            }
        }
        //加密
        this.encryptClass(configFiles);
        //清空
        configFiles.forEach(file -> IoUtils.writeTxtFile(file, ""));
    }

    /**
     * 压缩成jar
     *
     * @return 打包后的jar绝对路径
     */
    private String packageJar(List<String> libJarFiles) {
        //[1]先打包lib下的jar
        libJarFiles.forEach(targetJar -> {
            if (!targetJar.endsWith(".jar")) {
                return;
            }

            String srcJarDir = targetJar.substring(0, targetJar.length() - 4) + Const.LIB_JAR_DIR;
            if (!new File(srcJarDir).exists()) {
                return;
            }
            JarUtils.doJar(srcJarDir, targetJar);
            IoUtils.delete(new File(srcJarDir));
            Log.debug("打包: " + targetJar);
        });

        //删除META-INF下的maven
        IoUtils.delete(new File(this.targetDir, "META-INF/maven"));

        //[2]再打包jar
        String targetJar = jarPath.replace("." + jarOrWar, "-encrypted." + jarOrWar);
        String result = JarUtils.doJar(this.targetDir.getAbsolutePath(), targetJar);
        IoUtils.delete(this.targetDir);
        Log.debug("打包: " + targetJar);
        return result;
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
            clsName = file.substring(file.indexOf(Const.LIB_JAR_DIR, file.indexOf(K_LIB))
                    + Const.LIB_JAR_DIR.length() + 1);
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


    public Integer getEncryptFileCount() {
        return encryptFileCount;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public void setIncludeJars(List<String> includeJars) {
        this.includeJars = includeJars;
    }

    public void setExcludeClass(List<String> excludeClass) {
        this.excludeClass = excludeClass;
    }

    public void setClassPath(List<String> classPath) {
        this.classPath = classPath;
    }

    public void setCfgfiles(List<String> cfgfiles) {
        this.cfgfiles = cfgfiles;
    }

    public void setCode(char[] code) {
        this.code = code;
    }

}
