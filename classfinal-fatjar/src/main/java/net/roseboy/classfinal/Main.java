package net.roseboy.classfinal;


import net.roseboy.classfinal.util.*;

import java.io.File;
import java.util.List;
import java.util.Scanner;


/**
 * 加密普通jar，springboot jar，spring web war
 * 启动 java -jar this.jar
 * 启动2 java -jar this.jar -file springboot.jar -libjars a.jar,b.jar -packages net.roseboy,yiyon.com -exclude org.spring -pwd 995800 -Y
 *
 * @author roseboy
 */
public class Main {
    /**
     * 入口方法
     *
     * @param args 参数
     */
    public static void main(String[] args) {






        Const.pringInfo();
        Scanner scanner = new Scanner(System.in);

        //参数配置
        CmdLineOption cmd = new CmdLineOption();
        cmd.addOption("packages", true, "加密的包名(可为空,多个用\",\"分割)");
        cmd.addOption("pwd", true, "加密密码");
        cmd.addOption("code", true, "机器码");
        cmd.addOption("exclude", true, "排除的类名(可为空,多个用\",\"分割)");
        cmd.addOption("file", true, "加密的jar/war路径");
        cmd.addOption("libjars", true, "jar/war lib下的jar(可为空,多个用\",\"分割)");
        cmd.addOption("classpath", true, "依赖jar包目录(可为空,多个用\",\"分割)");
        cmd.addOption("cfgfiles", true, "需要加密的配置文件(可为空,多个用\",\"分割)");
        cmd.addOption("Y", false, "无需确认");
        cmd.addOption("debug", false, "调试模式");
        cmd.addOption("C", false, "生成机器码");
        cmd.parse(args);

        if (cmd.hasOption("C")) {
            makeCode();
            return;
        }

        //全部参数(需要加密的class路径,lib下的jar,包名,排除的class,依赖jar包路径,密码,机器码,需要加密的配置文件)
        String path=null,libjars,packages,excludeClass,classpath,password=null,code,cfgfiles;

        //没有参数手动输入
        if (args == null || args.length == 0) {
            while (StrUtils.isEmpty(path)) {
                Log.print("请输入需要加密的jar/war路径:");
                path = scanner.nextLine();
            }

            Log.print("请输入jar/war包lib下要加密jar文件名(多个用\",\"分割):");
            libjars = scanner.nextLine();

            Log.print("请输入需要加密的包名(可为空,多个用\",\"分割):");
            packages = scanner.nextLine();

            Log.print("请输入需要排除的类名(可为空,多个用\",\"分割):");
            excludeClass = scanner.nextLine();

            Log.print("请输入依赖jar包目录(可为空,多个用\",\"分割):");
            classpath = scanner.nextLine();

            Log.print("请输入要加密的配置文件名(可为空,多个用\",\"分割):");
            cfgfiles = scanner.nextLine();


            Log.print("请输入机器码(可为空):");
            code = scanner.nextLine();

            while (StrUtils.isEmpty(password)) {
                Log.print("请输入加密密码:");
                password = scanner.nextLine();
            }
        }else{//在参数中取
            path = cmd.getOptionValue("file", "");
            libjars = cmd.getOptionValue("libjars", "");
            packages = cmd.getOptionValue("packages", "");
            excludeClass = cmd.getOptionValue("exclude", "");
            classpath = cmd.getOptionValue("classpath", "");
            password = cmd.getOptionValue("pwd", "");
            code = cmd.getOptionValue("code", "");
            cfgfiles = cmd.getOptionValue("cfgfiles", "");
        }

        //test数据
        if ("1".equals(path)) {
            path = "/Users/roseboy/fsdownload/yiyon-package-liuyuan-1.0.0.jar";
            libjars = "yiyon-*.jar,aspectjweaver-1.8.13.jar,a.jar";
            packages = "com.yiyon,net.roseboy.*";//包名过滤
            excludeClass = "org.spring.*";//排除的类
            password = "123456";
            classpath = "/Users/roseboy/code-space/apache-tomcat-8.5.32/lib";
            cfgfiles = "*.yml";
            Const.DEBUG = false;
        } else if ("2".equals(path)) {
            path = "/Users/roseboy/code-space/pig_project/target/pig_project_maven.war";
            packages = "net.roseboy";//包名过滤
            excludeClass = "org.spring";//排除的类
            password = "#";
            classpath = "/Users/roseboy/code-space/apache-tomcat-8.5.32/lib";
            Const.DEBUG = true;
        } else if ("3".equals(path)) {
            path = "/Users/roseboy/Desktop/iyun.node.dicomserver.jar";
            packages = "com,org";//包名过滤
            password = "1234";
            Const.DEBUG = true;
        }

        Log.println();
        Log.println("加密信息如下:");
        Log.println("-------------------------");
        Log.println("1. jar/war路径:      " + path);
        Log.println("2. lib下的jar:       " + libjars);
        Log.println("3. 包名前缀:          " + packages);
        Log.println("4. 排除的类名:        " + excludeClass);
        Log.println("5. 加密配置文件:      " + cfgfiles);
        Log.println("6. ClassPath:       " + classpath);
        Log.println("7. 密码:             " + password);
        Log.println("8. 机器码:           " + code);
        Log.println("-------------------------");
        Log.println();

        String yes;
        if (cmd.hasOption("Y")) {
            yes = "Y";
        } else {
            Log.println("确定执行吗？(Y/n)");
            yes = scanner.nextLine();
            while (!"n".equals(yes) && !"Y".equals(yes)) {
                Log.println("Yes or No ？[Y/n]");
                yes = scanner.nextLine();
            }
        }
        IoUtils.close(scanner);

        if (!"Y".equals(yes)) {
            Log.println("已取消！");
            return;
        }
        Log.println("处理中...");
        List<String> includeJarList = StrUtils.toList(libjars);
        List<String> packageList = StrUtils.toList(packages);
        List<String> excludeClassList = StrUtils.toList(excludeClass);
        List<String> classPathList = StrUtils.toList(classpath);
        List<String> cfgFileList = StrUtils.toList(cfgfiles);
        includeJarList.add("-");

        JarEncryptor encryptor = new JarEncryptor(path, password.trim().toCharArray());
        encryptor.setCode(StrUtils.isEmpty(code) ? null : code.trim().toCharArray());
        encryptor.setPackages(packageList);
        encryptor.setIncludeJars(includeJarList);
        encryptor.setExcludeClass(excludeClassList);
        encryptor.setClassPath(classPathList);
        encryptor.setCfgfiles(cfgFileList);
        try {
            String result = encryptor.doEncryptJar();
            Log.println("加密完成，请牢记密码！");
            Log.println("==>" + result);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * 生成机器码
     */
    public static void makeCode() {
        String path = JarUtils.getRootPath(null);
        path = path.substring(0, path.lastIndexOf("/") + 1);

        String code = new String(SysUtils.makeMarchinCode());
        File file = new File(path, "classfinal-code.txt");
        IoUtils.writeTxtFile(file, code);
        Log.println("Server code is: " + code);
        Log.println("==>" + file.getAbsolutePath());
        Log.println();
    }
}
