package net.roseboy.classfinal.util;

import net.roseboy.classfinal.Constants;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * jar/war操作工具类
 *
 * @author roseboy
 */
public class JarUtils {


    /**
     * 把目录压缩成jar
     *
     * @param jarDir    需要打包的目录
     * @param targetJar 打包出的jar/war文件路径
     * @return 打包出的jar/war文件路径
     */
    public static String doJar(String jarDir, String targetJar) {
        //枚举jarDir下的所有文件以及目录
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
                if (isDel(file)) {
                    continue;
                }
                String fileName = file.getAbsolutePath().substring(jarDir.length());
                fileName = fileName.startsWith(File.separator) ? fileName.substring(1) : fileName;
                if (file.isDirectory()) {
                    //目录，添加一个目录entry
                    JarArchiveEntry je = new JarArchiveEntry(fileName + File.separator);
                    je.setTime(System.currentTimeMillis());
                    jos.putArchiveEntry(je);
                    jos.closeArchiveEntry();
                } else if (fileName.endsWith(".jar")) {
                    //jar文件，需要写crc信息
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
                    //其他文件直接写入
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
     * @return 所有文件的完整路径，包含目录
     */
    public static List<String> unJar(String jarPath, String targetDir, List<String> includeJars) {
        List<String> list = new ArrayList<>();

        //如果不存在创建目录
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

                //释放jar文件，如果在includeJars中，递归释放jar内的文件
                if (jarEntry.getName().endsWith(".jar")) {
                    ByteArrayOutputStream jos0 = new ByteArrayOutputStream();
                    IoUtils.copy(jis, jos0);
                    byte[] bytes = jos0.toByteArray();
                    IoUtils.writeFile(new File(targetDir + File.separator + jarEntry.getName()), bytes);
                    String jarName = jarEntry.getName().replace("BOOT-INF" + File.separator + "lib" + File.separator, "");
                    jarName = jarName.replace("WEB-INF" + File.separator + "lib" + File.separator, "");
                    if (includeJars == null || includeJars.size() == 0 || includeJars.contains(jarName)) {
                        String targetPath0 = targetDir + File.separator + jarEntry.getName();
                        String targetDir0 = targetDir + File.separator + jarEntry.getName().replace(".jar", Constants.LIB_JAR_DIR);
                        List<String> list0 = unJar(targetPath0, targetDir0, includeJars);
                        list.addAll(list0);
                    }

                } else if (jarEntry.isDirectory()) {
                    //如果是目录，创建目录
                    File dir = new File(targetDir + File.separator + jarEntry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    //其他文件，直接释放
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

    /**
     * 在jar中根据文件名释放文件
     *
     * @param zip        压缩文件
     * @param fileName   文件名
     * @param targetFile 释放的目标文件
     * @return 释放出的文件的绝对路径
     */
    public static String releaseFileFromJar(File zip, String fileName, File targetFile) {
        byte[] bytes = getFileFromJar(zip, fileName);
        if (bytes == null) {
            return null;
        }
        IoUtils.writeFile(targetFile, bytes);
        return targetFile.getAbsolutePath();

    }

    /**
     * 在压缩文件中获取一个文件的字节
     *
     * @param zip      压缩文件
     * @param fileName 文件名
     * @return 文件的字节
     */
    public static byte[] getFileFromJar(File zip, String fileName) {
        ZipFile zipFile = null;
        try {
            if (!zip.exists()) {
                return null;
            }
            zipFile = new ZipFile(zip);
            ZipEntry zipEntry = zipFile.getEntry(fileName);
            if (zipEntry == null) {
                return null;
            }
            InputStream is = zipFile.getInputStream(zipEntry);
            return IoUtils.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zipFile);
        }
        return null;
    }

    /**
     * 是否删除这个文件
     *
     * @param file 文件
     * @return 是否需要删除
     */
    public static boolean isDel(File file) {
        for (String f : Constants.DLE_FILES) {
            if (file.getAbsolutePath().endsWith(f)) {
                return true;
            }
        }
        return false;
    }
}
