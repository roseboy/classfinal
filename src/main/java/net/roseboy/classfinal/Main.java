package net.roseboy.classfinal;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.roseboy.util.ClassUtils;
import net.roseboy.util.EncryptUtils;
import net.roseboy.util.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * 加密普通javaweb项目，解压后的那种，只加密classes下的class文件
 * 启动 java -jar this.jar
 * 启动2 java -jar this.jar -classes /user/tomcat/classes -libs lib1,lib2 -packages net.roseboy,yiyon.com -exclude org.spring -pwd 995800 -Y
 *
 * @author roseboy
 * @date 2019-08-02
 */
public class Main {
    public static final String FILE_NAME = "classes.dat";//加密出来的文件名
    public static final String LIB_JAR_DIR = "_jar";//lib下的jar解压的目录名后缀

    public static void main(String[] args) {
        try {
            String path = null;//需要加密的class路径
            String packages = null; //包名
            String libs = null;//依赖jar包的路径
            String excludeClass = null;//排除的class
            String password = null;//密码

            //先接受参数
            CommandLine cmd = getCmdOptions(args);
            if (cmd == null) {
                return;
            }
            if (cmd.hasOption("classes")) {
                path = cmd.getOptionValue("classes");
            }
            if (cmd.hasOption("libs")) {
                libs = cmd.getOptionValue("libs");
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
            if (args == null || args.length == 0 || (args.length == 1 && args[0].equals("-C"))) {
                while (path == null || path.length() == 0) {
                    System.out.print("请输入需要加密的classes路径:");
                    path = scanner.nextLine();
                }

                System.out.print("请输入需要加密的包名(可为空,多个用\",\"分割):");
                packages = scanner.nextLine();

                System.out.print("请输入需要排除的类名(可为空,多个用\",\"分割):");
                excludeClass = scanner.nextLine();

                System.out.print("请输入依赖jar包的路径(多个用\",\"分割):");
                libs = scanner.nextLine();

                while (password == null || password.length() == 0) {
                    System.out.print("请输入加密密码:");
                    password = scanner.nextLine();
                }
            }
            System.out.println();
            System.out.println("加密信息如下:");
            System.out.println("-------------------------");
            System.out.println("classes路径:    " + path);
            System.out.println("包名:           " + packages);
            System.out.println("排除的类名:      " + excludeClass);
            System.out.println("依赖包路径:      " + libs);
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
                //加密过程
                System.out.println("处理中...");
                String result = doEncryptClass(path, packages, libs, excludeClass, password);
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
     * 加密class文件
     *
     * @param encryptPath    classes路径
     * @param encryptPackage 包名，可为空
     * @param libs           依赖的jar包路径，多个用分号隔开
     * @param excludeClass   无需加密的类，可为空，多个用分号隔开
     * @param password       密码
     * @throws NotFoundException
     */
    public static String doEncryptClass(String encryptPath, String encryptPackage, String libs, String excludeClass, String password) throws NotFoundException {
        if (encryptPath == null || encryptPath.length() == 0) {
            throw new RuntimeException("加密classes路径有误");
        }
        if (password == null || password.length() == 0) {
            throw new RuntimeException("密码不能为空");
        }
        encryptPath = encryptPath.endsWith(File.separator) ? encryptPath.substring(0, encryptPath.length() - 1) : encryptPath;

        //需要加密目录下的所有class文件
        List<File> classFiles = new ArrayList<>();
        IOUtils.listFile(classFiles, new File(encryptPath), ".class");

        //需要加密的class名
        List<String> classNames = new ArrayList<>();
        for (File classFile : classFiles) {
            String className = classFile.getAbsolutePath().substring(encryptPath.length() + 1);
            className = className.replace(".class", "").replace(File.separator, ".");
            if (ClassUtils.isPackage(encryptPackage, className) && (excludeClass == null || excludeClass.length() == 0 || !excludeClass.contains(className))) {
                classNames.add(className);
            }
        }

        //加密class，另存
        EncryptUtils.encryptFiles(encryptPath, encryptPath + File.separator + FILE_NAME, classNames, password);
        //EncryptUtils.encryptFiles(encryptPath, null, classNames, password, false);

        //初始化javassist
        ClassPool pool = ClassPool.getDefault();
        if (libs != null && libs.length() > 0) {
            ClassUtils.loadClassPath(pool, libs.split(","));
        }
        pool.insertClassPath(encryptPath);

        //修改class方法体，并保存文件
        for (String classname : classNames) {
            byte[] bts = ClassUtils.rewriteMethod(pool, classname);
            if (bts != null) {
                String path = encryptPath + File.separator + classname.replace(".", File.separator) + ".class";
                IOUtils.writeFile(new File(path), bts);
            }
        }
        return encryptPath + File.separator + FILE_NAME;
    }

    /**
     * cmd 参数
     *
     * @return
     */
    public static CommandLine getCmdOptions(String[] args) {
        CommandLine cmd = null;
        Options options = new Options();
        options.addOption("classes", true, "加密的classes路径");
        options.addOption("libs", true, "项目依赖的jar包目录(多个用\",\"分割)");
        options.addOption("packages", true, "加密的包名(可为空,多个用\",\"分割)");
        options.addOption("pwd", true, "加密密码");
        options.addOption("exclude", true, "排除的类名(可为空,多个用\",\"分割)");
        options.addOption("file", true, "加密的jar/war路径");
        options.addOption("libjars", true, "jar/war lib下的jar(多个用\",\"分割)");
        options.addOption("Y", false, "无需确认");
        options.addOption("C", false, "加密class目录");

        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return cmd;
    }


    public static void pringInfo() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("=                                                       =");
        System.out.println("=         Java Class Encryption Tool   by Mr.K          =");
        System.out.println("=                                                       =");
        System.out.println("=========================================================");
        System.out.println();
    }

    public static void printDog() {
        System.out.println("                                                                           ");
        System.out.println("                                                                           ");
        System.out.println("          .,:,,,                                        .::,,,::.          ");
        System.out.println("        .::::,,;;,                                  .,;;:,,....:i:         ");
        System.out.println("        :i,.::::,;i:.      ....,,:::::::::,....   .;i:,.  ......;i.        ");
        System.out.println("        :;..:::;::::i;,,:::;:,,,,,,,,,,..,.,,:::iri:. .,:irsr:,.;i.        ");
        System.out.println("        ;;..,::::;;;;ri,,,.                    ..,,:;s1s1ssrr;,.;r,        ");
        System.out.println("        :;. ,::;ii;:,     . ...................     .;iirri;;;,,;i,        ");
        System.out.println("        ,i. .;ri:.   ... ............................  .,,:;:,,,;i:        ");
        System.out.println("        :s,.;r:... ....................................... .::;::s;        ");
        System.out.println("        ,1r::. .............,,,.,,:,,........................,;iir;        ");
        System.out.println("        ,s;...........     ..::.,;:,,.          ...............,;1s        ");
        System.out.println("       :i,..,.              .,:,,::,.          .......... .......;1,       ");
        System.out.println("      ir,....:rrssr;:,       ,,.,::.     .r5S9989398G95hr;. ....,.:s,      ");
        System.out.println("     ;r,..,s9855513XHAG3i   .,,,,,,,.  ,S931,.,,.;s;s&BHHA8s.,..,..:r:     ");
        System.out.println("    :r;..rGGh,  :SAG;;G@BS:.,,,,,,,,,.r83:      hHH1sXMBHHHM3..,,,,.ir.    ");
        System.out.println("   ,si,.1GS,   sBMAAX&MBMB5,,,,,,:,,.:&8       3@HXHBMBHBBH#X,.,,,,,,rr    ");
        System.out.println("   ;1:,,SH:   .A@&&B#&8H#BS,,,,,,,,,.,5XS,     3@MHABM&59M#As..,,,,:,is,   ");
        System.out.println("  .rr,,,;9&1   hBHHBB&8AMGr,,,,,,,,,,,:h&&9s;   r9&BMHBHMB9:  . .,,,,;ri.  ");
        System.out.println("  :1:....:5&XSi;r8BMBHHA9r:,......,,,,:ii19GG88899XHHH&GSr.      ...,:rs.  ");
        System.out.println("  ;s.     .:sS8G8GG889hi.        ....,,:;:,.:irssrriii:,.        ...,,i1,  ");
        System.out.println("  ;1,         ..,....,,isssi;,        .,,.                      ....,.i1,  ");
        System.out.println("  ;h:               i9HHBMBBHAX9:         .                     ...,,,rs,  ");
        System.out.println("  ,1i..            :A#MBBBBMHB##s                             ....,,,;si.  ");
        System.out.println("  .r1,..        ,..;3BMBBBHBB#Bh.     ..                    ....,,,,,i1;   ");
        System.out.println("   :h;..       .,..;,1XBMMMMBXs,.,, .. :: ,.               ....,,,,,,ss.   ");
        System.out.println("    ih: ..    .;;;, ;;:s58A3i,..    ,. ,.:,,.             ...,,,,,:,s1,    ");
        System.out.println("    .s1,....   .,;sh,  ,iSAXs;.    ,.  ,,.i85            ...,,,,,,:i1;     ");
        System.out.println("     .rh: ...     rXG9XBBM#M#MHAX3hss13&&HHXr         .....,,,,,,,ih;      ");
        System.out.println("      .s5: .....    i598X&&A&AAAAAA&XG851r:       ........,,,,:,,sh;       ");
        System.out.println("      . ihr, ...  .         ..                    ........,,,,,;11:.       ");
        System.out.println("         ,s1i. ...  ..,,,..,,,.,,.,,.,..       ........,,.,,.;s5i.         ");
        System.out.println("          .:s1r,......................       ..............;shs,           ");
        System.out.println("          . .:shr:.  ....                 ..............,ishs.             ");
        System.out.println("              .,issr;,... ...........................,is1s;.               ");
        System.out.println("                 .,is1si;:,....................,:;ir1sr;,                  ");
        System.out.println("                    ..:isssssrrii;::::::;;iirsssssr;:..                    ");
        System.out.println("                         .,::iiirsssssssssrri;;:.                          ");
        System.out.println("");

        try {
            for (int i = 0; i < 30; i++) {
                System.out.print(".");
                Thread.sleep(100);
            }
            System.out.println("ok");
        } catch (Exception e) {

        }
    }


}
