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

        try {
            //先接收参数
            CmdLineOption cmd = new CmdLineOption();
            cmd.addOption("packages", true, "加密的包名(可为空,多个用\",\"分割)");
            cmd.addOption("pwd", true, "加密密码");
            cmd.addOption("code", true, "机器码");
            cmd.addOption("exclude", true, "排除的类名(可为空,多个用\",\"分割)");
            cmd.addOption("file", true, "加密的jar/war路径");
            cmd.addOption("libjars", true, "jar/war lib下的jar(多个用\",\"分割)");
            cmd.addOption("cpasspath", true, "依赖jar包目录(多个用\",\"分割)");
            cmd.addOption("Y", false, "无需确认");
            cmd.addOption("debug", false, "调试模式");
            cmd.addOption("C", false, "生成机器码");
            cmd.parse(args);

            if (cmd.hasOption("C")) {
                makeCode();
                return;
            }


            //需要加密的class路径
            String path = cmd.getOptionValue("file", "");
            //lib下的jar
            String libjars = cmd.getOptionValue("libjars", "");
            //包名
            String packages = cmd.getOptionValue("packages", "");
            //排除的class
            String excludeClass = cmd.getOptionValue("exclude", "");
            //依赖jar包路径
            String cpasspath = cmd.getOptionValue("cpasspath", "");
            //密码
            String password = cmd.getOptionValue("pwd", "");
            //机器码
            String code = cmd.getOptionValue("code", "");


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
                cpasspath = scanner.nextLine();


                Log.print("请输入机器码(可为空):");
                code = scanner.nextLine();

                while (StrUtils.isEmpty(password)) {
                    Log.print("请输入加密密码:");
                    password = scanner.nextLine();
                }
            }

            //test数据
            if ("1".equals(path)) {
                path = "/Users/roseboy/work-yiyon/易用框架/yiyon-server-liuyuan/yiyon-package-liuyuan/target/yiyon-package-liuyuan-1.0.0.jar";
                libjars = "yiyon-basedata-1.0.0.jar,jeee-admin-1.0.0.jar,aspectjweaver-1.8.13.jar,a.jar";
                packages = "com.yiyon,net.roseboy,yiyon";//包名过滤
                excludeClass = "org.spring";//排除的类
                password = "123456";
                cpasspath = "/Users/roseboy/code-space/apache-tomcat-8.5.32/lib";
                Const.DEBUG = true;
            } else if ("2".equals(path)) {
                path = "/Users/roseboy/code-space/pig_project/target/pig_project_maven.war";
                packages = "net.roseboy";//包名过滤
                excludeClass = "org.spring";//排除的类
                password = "#";
                cpasspath = "/Users/roseboy/code-space/apache-tomcat-8.5.32/lib";
                Const.DEBUG = true;
            }


            Log.println();
            Log.println("加密信息如下:");
            Log.println("-------------------------");
            Log.println("jar/war路径:    " + path);
            Log.println("lib下的jar:      " + libjars);
            Log.println("包名:           " + packages);
            Log.println("排除的类名:      " + excludeClass);
            Log.println("ClassPath:      " + cpasspath);
            Log.println("密码:           " + password);
            Log.println("机器码:           " + code);
            Log.println("-------------------------");
            Log.println();

            String yes;
            if (cmd.hasOption("Y")) {
                yes = "Y";
            } else {
                Log.println("请牢记密码，密码忘记将无法启动项目。确定执行吗？(Y/n)");
                yes = scanner.nextLine();
                while (!"n".equals(yes) && !"Y".equals(yes)) {
                    Log.println("Yes or No ？[Y/n]");
                    yes = scanner.nextLine();
                }
            }

            if ("Y".equals(yes)) {
                List<String> includeJarList = StrUtils.toList(libjars);
                List<String> packageList = StrUtils.toList(packages);
                List<String> excludeClassList = StrUtils.toList(excludeClass);
                List<String> classPathList = StrUtils.toList(cpasspath);
                includeJarList.add("-");

                //加密过程
                Log.println("处理中...");
                JarEncryptor decryptor = new JarEncryptor(path, password.trim().toCharArray(),
                        StrUtils.isEmpty(code) ? null : code.trim().toCharArray(),
                        packageList, includeJarList, excludeClassList, classPathList);
                String result = decryptor.doEncryptJar();
                Log.println("加密完成，请牢记密码！");
                Log.println("==>" + result);
            } else {
                Log.println("已取消！");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.println("ERROR: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * 生成机器码
     */
    public static void makeCode() {
        String path = ClassUtils.getRootPath();
        path = path.substring(0, path.lastIndexOf("/") + 1);

        String code = new String(SysUtils.makeMarchinCode());
        File file = new File(path, "classfinal-code.txt");
        IoUtils.writeTxtFile(file, code);
        Log.println("Server code is: " + code);
        Log.println("==>" + file.getAbsolutePath());
        Log.println();
    }
}
