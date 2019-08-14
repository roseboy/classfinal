package net.roseboy.classfinal.plugin;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * maven插件主类
 *
 * @author roseboy
 * @date 2019-08-15
 */
@Mojo(name = "classFinal", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassFinal extends AbstractMojo {


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "prefix", required = true, defaultValue = "@")
    private String prefix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Build build = project.getBuild();
        String targetjar = build.getDirectory() + File.separator + build.getFinalName() + ".jar";
        getLog().info("加密jar: " + targetjar);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getLog().info("加密完成: " + build.getDirectory() + File.separator + build.getFinalName() + "-encrypted.jar");
        getLog().info("");
    }

}