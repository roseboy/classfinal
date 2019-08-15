package net.roseboy.classfinal.plugin;

import net.roseboy.classfinal.JarEncryptor;
import net.roseboy.classfinal.util.StrUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * maven插件主类
 *
 * @author roseboy
 */
@Mojo(name = "classFinal", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassFinal extends AbstractMojo {


    //MavenProject
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    //密码
    @Parameter(property = "password", required = true)
    private String password;
    //加密的内部-lib/jar名称
    @Parameter(property = "libjars")
    String libjars;
    //要加密的包名前缀
    @Parameter(property = "packages")
    String packages;
    //排除的类名
    @Parameter(property = "excludes")
    String excludes;

    /**
     * 打包的时候执行
     *
     * @throws MojoExecutionException MojoExecutionException
     * @throws MojoFailureException   MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        Build build = project.getBuild();
        String targetjar = build.getDirectory() + File.separator + build.getFinalName() + "." + project.getPackaging();
        getLog().info("加密jar: " + targetjar);
        List<String> includeJarList = StrUtils.toList(libjars);
        List<String> packageList = StrUtils.toList(packages);
        List<String> excludeClassList = StrUtils.toList(excludes);
        includeJarList.add("-");

        //加密过程
        getLog().info("处理中...");
        JarEncryptor decryptor = new JarEncryptor();
        String result = decryptor.doEncryptJar(targetjar, password, packageList, includeJarList, excludeClassList);
        getLog().info(result);
        getLog().info("加密完成，请牢记密码！");
        getLog().info("");
    }

}