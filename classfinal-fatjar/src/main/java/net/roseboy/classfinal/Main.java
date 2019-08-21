package net.roseboy.classfinal;


import net.roseboy.classfinal.util.CmdLineOption;
import net.roseboy.classfinal.util.StrUtils;

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
            CmdLineOption cmd = getCmdOptions(args);
            if (cmd == null) {
                return;
            }

            //需要加密的class路径
            String path = cmd.getOptionValue("file");
            //lib下的jar
            String libjars = cmd.getOptionValue("libjars");
            //包名
            String packages = cmd.getOptionValue("packages");
            //排除的class
            String excludeClass = cmd.getOptionValue("exclude");
            //密码
            String password = cmd.getOptionValue("pwd");

            //没有参数手动输入
            if (args == null || args.length == 0) {
                while (StrUtils.isEmpty(path)) {
                    System.out.print("请输入需要加密的jar/war路径:");
                    path = scanner.nextLine();
                }

                System.out.print("请输入jar/war包lib下要加密jar文件名(多个用\",\"分割):");
                libjars = scanner.nextLine();

                System.out.print("请输入需要加密的包名(可为空,多个用\",\"分割):");
                packages = scanner.nextLine();

                System.out.print("请输入需要排除的类名(可为空,多个用\",\"分割):");
                excludeClass = scanner.nextLine();

                while (StrUtils.isEmpty(password)) {
                    System.out.print("请输入加密密码:");
                    password = scanner.nextLine();
                }
            }

            //test数据
            if ("123123".equals(path)) {
                //springboot jar
                path = "/Users/roseboy/work-yiyon/易用框架/yiyon-server-liuyuan/yiyon-package-liuyuan/target/yiyon-package-liuyuan-1.0.0.jar";
                //spring web war
                //path = "/Users/roseboy/work-yiyon/北大口腔/erpbeidakouqiang/target/erpbeidakouqiang-1.0.0.war";

                libjars = "yiyon-basedata-1.0.0.jar,jeee-admin-1.0.0.jar,aspectjweaver-1.8.13.jar";
                packages = "com.yiyon,net.roseboy,yiyon";//包名过滤
                excludeClass = "org.spring";//排除的类
                password = "123456";
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
                List<String> includeJarList = StrUtils.toList(libjars);
                List<String> packageList = StrUtils.toList(packages);
                List<String> excludeClassList = StrUtils.toList(excludeClass);
                includeJarList.add("-");

                //加密过程
                System.out.println("处理中...");
                JarEncryptor decryptor = new JarEncryptor(path, password, packageList, includeJarList, excludeClassList);

                System.out.println(decryptor.getClass().getProtectionDomain());
                String result = decryptor.doEncryptJar();
                System.out.println("加密完成，请牢记密码！");
                System.out.println(result);
            } else {
                System.out.println("已取消！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * cmd 参数
     *
     * @return CommandLine
     */
    public static CmdLineOption getCmdOptions(String[] args) {
        CmdLineOption options = new CmdLineOption();
        options.addOption("packages", true, "加密的包名(可为空,多个用\",\"分割)");
        options.addOption("pwd", true, "加密密码");
        options.addOption("exclude", true, "排除的类名(可为空,多个用\",\"分割)");
        options.addOption("file", true, "加密的jar/war路径");
        options.addOption("libjars", true, "jar/war lib下的jar(多个用\",\"分割)");
        options.addOption("Y", false, "无需确认");

        try {
            options.parse(args);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return options;
    }

}
