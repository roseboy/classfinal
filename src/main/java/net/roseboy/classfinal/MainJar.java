package net.roseboy.classfinal;


import javassist.ClassPool;
import javassist.NotFoundException;
import net.roseboy.classfinal.util.ClassUtils;
import net.roseboy.classfinal.util.EncryptUtils;
import net.roseboy.classfinal.util.IoUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 加密普通jar，springboot jar，spring web war
 * 启动 java -jar this.jar
 * 启动2 java -jar this.jar -file springboot.jar -libjars a.jar,b.jar -packages net.roseboy,yiyon.com -exclude org.spring -pwd 995800 -Y
 *
 * @author roseboy
 * @date 2019-08-05
 */
public class MainJar {
    public static void main(String[] args) {
        Main.pringInfo();

        try {
            //先接受参数
            CommandLine cmd = Main.getCmdOptions(args);
            if (cmd == null) {
                return;
            }
            //加密tomcat下已经解压过的war包
            if (cmd.hasOption("C") || cmd.hasOption("classes")) {
                Main.main(args);
                return;
            }

            String path = null;//需要加密的class路径
            String packages = null; //包名
            String libjars = null;
            String excludeClass = null;//排除的class
            String password = null;//密码

            if (cmd.hasOption("file")) {
                path = cmd.getOptionValue("file");
            }
            if (cmd.hasOption("libjars")) {
                libjars = cmd.getOptionValue("libjars");
            }
            if (cmd.hasOption("packages")) {
                packages = cmd.getOptionValue("packages");
            }
            if (cmd.hasOption("pwd")) {
                password = cmd.getOptionValue("pwd");
            }
            if (cmd.hasOption("exclude")) {
                excludeClass = cmd.getOptionValue("exclude");
            }

            //没有参数手动输入
            Scanner scanner = new Scanner(System.in);
            if (args == null || args.length == 0) {
                while (path == null || path.length() == 0) {
                    System.out.print("请输入需要加密的jar/war路径:");
                    path = scanner.nextLine();
                }

                System.out.print("请输入jar/war包lib下要加密jar文件名(多个用\",\"分割):");
                libjars = scanner.nextLine();

                System.out.print("请输入需要加密的包名(可为空,多个用\",\"分割):");
                packages = scanner.nextLine();

                System.out.print("请输入需要排除的类名(可为空,多个用\",\"分割):");
                excludeClass = scanner.nextLine();

                while (password == null || password.length() == 0) {
                    System.out.print("请输入加密密码:");
                    password = scanner.nextLine();
                }
            }

            //test数据
            if ("123123".equals(path)) {
                //springboot jar
                //path = "/Users/roseboy/work-yiyon/易用框架/yiyon-server-liuyuan/yiyon-package-liuyuan/target/yiyon-package-liuyuan-1.0.0.jar";
                //spring web war
                path = "/Users/roseboy/work-yiyon/北大口腔/erpbeidakouqiang/target/erpbeidakouqiang-1.0.0.war";
                //fat jar
                //path = "/Users/roseboy/code-space/agent/target/agent-1.0.jar";

                libjars = "yiyon-basedata-1.0.0.jar,jeee-admin-1.0.0.jar,aspectjweaver-1.8.13.jar";
                packages = "com.yiyon,net.roseboy,yiyon";//包名过滤
                excludeClass = "org.spring";//排除的类
                password = "000000";
            }


            System.out.println();
            System.out.println("加密信息如下:");
            System.out.println("-------------------------");
            System.out.println("jar/war路径:    " + path);
            System.out.println("lib下的jar:      " + libjars);
            System.out.println("包名:           " + packages);
            System.out.println("排除的类名:      " + excludeClass);
            System.out.println("密码:           " + password);
            System.out.println("-------------------------");
            System.out.println();

            String yes;
            if (cmd.hasOption("Y")) {
                yes = "Y";
            } else {
                System.out.println("请牢记密码，密码忘记将无法启动项目。确定执行吗？(Y/n)");
                yes = scanner.nextLine();
                while (!"n".equals(yes) && !"Y".equals(yes)) {
                    System.out.println("Yes or No ？(Y/n)");
                    yes = scanner.nextLine();
                }
            }

            if ("Y".equals(yes)) {
                List<String> includeJars = new ArrayList<>();
                includeJars.add("-");
                if (libjars != null && libjars.length() > 0) {
                    includeJars.addAll(Arrays.asList(libjars.split(",")));
                }
                //加密过程
                System.out.println("处理中...");
                String result = doEncryptJar(path, packages, includeJars, excludeClass, password);
                System.out.println("加密完成，请牢记密码！");
                System.out.println(result);
            } else {
                System.out.println("已取消！");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * 加密jar的主要过程
     *
     * @param jarPath      要加密的jar或war
     * @param packages     要加密的包，多个用逗号隔开
     * @param includeJars  -INF/lib下要加密的jar
     * @param excludeClass 排除的类名
     * @param password     密码
     * @return 加密后文件的路径
     * @throws IOException
     * @throws NotFoundException
     */
    public static String doEncryptJar(String jarPath, String packages, List<String> includeJars, String excludeClass, String password) throws IOException, NotFoundException {
        if (!jarPath.endsWith(".jar") && !jarPath.endsWith(".war")) {
            throw new RuntimeException("jar/war文件格式有误");
        }
        if (password == null || password.length() == 0) {
            throw new RuntimeException("密码不能为空");
        }
        String jarOrWar = jarPath.substring(jarPath.lastIndexOf(".") + 1);
        //临时work目录
        String targetDir = jarPath.replace("." + jarOrWar, Main.LIB_JAR_DIR);

        //[1]释放所有文件，内部jar只释放需要加密的jar
        List<String> allFile = unJar(jarPath, targetDir, includeJars);

        //[2]按照jar包名分组,只要需要加密的class文件
        Map<String, List<String>> jarClasses = new HashMap<>(200);//需要加密的jar名与jar下的所有class类名
        for (String file : allFile) {
            if (!file.endsWith(".class")) {//class文件
                continue;
            }
            file = file.replace(targetDir, "");
            file = file.startsWith(File.separator) ? file.substring(1) : file;
            file = file.replace(File.separator, ".");
            file = file.substring(0, file.length() - 6);
            String jarName;
            String clsName;
            if ((file.contains("BOOT-INF.lib") || file.contains("WEB-INF.lib")) && file.contains(Main.LIB_JAR_DIR)) {//lib的jar包内的
                file = file.replace("BOOT-INF.lib.", "").replace("WEB-INF.lib.", "");
                jarName = file.substring(0, file.indexOf(Main.LIB_JAR_DIR));
                clsName = file.substring(file.indexOf(Main.LIB_JAR_DIR) + Main.LIB_JAR_DIR.length() + 1);
            } else if (file.contains("BOOT-INF.classes") || file.contains("WEB-INF.classes")) {//jar/war包-INF/classes下的class文件
                file = file.replace("BOOT-INF.classes.", "").replace("WEB-INF.classes.", "");
                jarName = "CLASSES";
                clsName = file;
            } else {//jar包下的class文件
                jarName = "ROOT";
                clsName = file;
            }

            //判断是否是需要加密的包，是不是排除的类
            if (ClassUtils.isPackage(packages, clsName) && (excludeClass == null || excludeClass.length() == 0 || !excludeClass.contains(clsName))) {
                List<String> jarCls = jarClasses.get(jarName);
                jarCls = jarCls == null ? new ArrayList<>() : jarCls;
                jarCls.add(clsName);
                jarClasses.put(jarName, jarCls);
            }

        }

        //[3]加密class，压缩另存
        ZipOutputStream zos = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(targetDir + File.separator + Main.FILE_NAME));
            zos = new ZipOutputStream(out);
            for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
                for (String classname : entry.getValue()) {
                    String classPath = targetDir + File.separator + ClassUtils.realPath(entry.getKey(), classname, jarOrWar);
                    File sourceFile = new File(classPath);
                    zos.putNextEntry(new ZipEntry(classname));
                    byte[] bytes = IoUtils.readFileToByte(sourceFile);
                    bytes = EncryptUtils.en(bytes, password);
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zos, out);
        }

        //[4]修改class方法体，并保存文件
        String libpath = targetDir + File.separator + ("jar".equals(jarOrWar) ? "BOOT-INF" : "WEB-INF") + File.separator + "lib" + File.separator;
        for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
            //初始化javassist
            ClassPool pool = ClassPool.getDefault();
            //lib目录
            ClassUtils.loadClassPath(pool, new String[]{libpath});
            //要修改的class所在的目录
            pool.insertClassPath(targetDir + File.separator + ClassUtils.realPath(entry.getKey(), null, jarOrWar));
            //修改class方法体，并保存文件
            for (String classname : entry.getValue()) {
                byte[] bts = ClassUtils.rewriteMethod(pool, classname);
                if (bts != null) {
                    String path = targetDir + File.separator + ClassUtils.realPath(entry.getKey(), classname, jarOrWar);
                    IoUtils.writeFile(new File(path), bts);
                }
            }
        }

        //[5]打包回去
        //[5.1]先打包lib下的jar
        for (Map.Entry<String, List<String>> entry : jarClasses.entrySet()) {
            if (!"CLASSES".equals(entry.getKey()) && !"ROOT".equals(entry.getKey())) {
                doJar(libpath + entry.getKey() + Main.LIB_JAR_DIR, libpath + entry.getKey() + ".jar");
            }
        }

        //[5.2]删除内部jar解压出来的目录
        for (String file : includeJars) {
            File dir = new File(libpath + file.replace(".jar", Main.LIB_JAR_DIR));
            if (dir.exists()) {
                IoUtils.delete(dir);
            }
        }

        //[5.3]再打包jar
        String result = doJar(targetDir, jarPath.replace("." + jarOrWar, "-encrypted." + jarOrWar));

        //[5.4]删除jar解压出来的目录
        File dir = new File(targetDir);
        if (dir.exists()) {
            IoUtils.delete(dir);
        }

        return result;
    }


    /**
     * 把目录压缩成jar
     *
     * @param jarDir
     * @param targetJar
     * @return
     */
    public static String doJar(String jarDir, String targetJar) {
        List<File> files = new ArrayList<>();
        IoUtils.listFile(files, new File(jarDir));

        JarArchiveOutputStream jos = null;
        OutputStream out = null;

        try {
            File jar = new File(targetJar);
            if (jar.exists()) {
                jar.delete();
            }

            out = new FileOutputStream(jar);
            jos = new JarArchiveOutputStream(out);

            for (File file : files) {
                if (ClassUtils.isDel(file)) {
                    continue;
                }
                String fileName = file.getAbsolutePath().substring(jarDir.length());
                fileName = fileName.startsWith(File.separator) ? fileName.substring(1) : fileName;

                if (file.isDirectory()) {
                    JarArchiveEntry je = new JarArchiveEntry(fileName + File.separator);
                    je.setTime(System.currentTimeMillis());
                    jos.putArchiveEntry(je);
                    jos.closeArchiveEntry();
                } else if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
                    byte[] bytes = IoUtils.readFileToByte(file);
                    JarArchiveEntry je = new JarArchiveEntry(fileName);
                    je.setMethod(JarArchiveEntry.STORED);
                    je.setSize(bytes.length);
                    je.setTime(System.currentTimeMillis());
                    je.setCrc(IoUtils.crc32(bytes));
                    jos.putArchiveEntry(je);
                    jos.write(bytes);
                    jos.closeArchiveEntry();

                } else {
                    JarArchiveEntry je = new JarArchiveEntry(fileName);
                    je.setTime(System.currentTimeMillis());
                    jos.putArchiveEntry(je);
                    byte[] bytes = IoUtils.readFileToByte(file);
                    jos.write(bytes);
                    jos.closeArchiveEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(jos);
        }
        return targetJar;
    }


    /**
     * 释放jar内以及子jar的所有文件
     *
     * @param jarPath     jar文件
     * @param targetDir   释放文件夹
     * @param includeJars 释放的内部jar的名称
     * @return 所有文件的完整路径
     * @throws IOException
     */
    public static List<String> unJar(String jarPath, String targetDir, List<String> includeJars) throws IOException {
        List<String> list = new ArrayList<>();

        targetDir = targetDir.endsWith(File.separator) ? targetDir.substring(0, targetDir.length() - 1) : targetDir;
        File targetDirs = new File(targetDir);
        if (!targetDirs.exists()) {
            targetDirs.mkdirs();
        }

        JarArchiveInputStream jis = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(new File(jarPath));
            jis = new JarArchiveInputStream(fin);

            JarArchiveEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                list.add(targetDir + File.separator + jarEntry.getName());
                if (jarEntry.getName().endsWith(".jar")) {
                    ByteArrayOutputStream jos0 = new ByteArrayOutputStream();
                    IoUtils.copy(jis, jos0);
                    byte[] bytes = jos0.toByteArray();
                    IoUtils.writeFile(new File(targetDir + File.separator + jarEntry.getName()), bytes);
                    if (includeJars == null || includeJars.size() == 0 || includeJars.contains(jarEntry.getName().replace("BOOT-INF" + File.separator + "lib" + File.separator, "").replace("WEB-INF" + File.separator + "lib" + File.separator, ""))) {
                        List<String> list0 = unJar(targetDir + File.separator + jarEntry.getName(), targetDir + File.separator + jarEntry.getName().replace(".jar", Main.LIB_JAR_DIR), includeJars);
                        list.addAll(list0);
                    }

                } else if (jarEntry.isDirectory()) {
                    File dir = new File(targetDir + File.separator + jarEntry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    ByteArrayOutputStream jos0 = new ByteArrayOutputStream();
                    IoUtils.copy(jis, jos0);
                    byte[] bytes = jos0.toByteArray();
                    IoUtils.writeFile(new File(targetDir + File.separator + jarEntry.getName()), bytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(jis, fin);
        }
        return list;
    }

}
