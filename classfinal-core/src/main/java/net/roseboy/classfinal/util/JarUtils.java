package net.roseboy.classfinal.util;

import net.roseboy.classfinal.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

        ZipOutputStream zos = null;
        OutputStream out = null;

        try {
            File jar = new File(targetJar);
            if (jar.exists()) {
                jar.delete();
            }

            out = new FileOutputStream(jar);
            zos = new ZipOutputStream(out);

            for (File file : files) {
                if (isDel(file)) {
                    continue;
                }
                String fileName = file.getAbsolutePath().substring(jarDir.length());
                fileName = fileName.startsWith(Constants.FILE_SEPARATOR) ? fileName.substring(1) : fileName;
                if (file.isDirectory()) {
                    //目录，添加一个目录entry
                    ZipEntry ze = new ZipEntry(fileName + Constants.FILE_SEPARATOR);
                    ze.setTime(System.currentTimeMillis());
                    zos.putNextEntry(ze);
                    zos.closeEntry();
                } else if (fileName.endsWith(".jar")) {
                    //jar文件，需要写crc信息
                    byte[] bytes = IoUtils.readFileToByte(file);
                    ZipEntry ze = new ZipEntry(fileName);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(bytes.length);
                    ze.setTime(System.currentTimeMillis());
                    ze.setCrc(IoUtils.crc32(bytes));
                    zos.putNextEntry(ze);
                    zos.write(bytes);
                    zos.closeEntry();
                } else {
                    //其他文件直接写入
                    ZipEntry ze = new ZipEntry(fileName);
                    ze.setTime(System.currentTimeMillis());
                    zos.putNextEntry(ze);
                    byte[] bytes = IoUtils.readFileToByte(file);
                    zos.write(bytes);
                    zos.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zos);
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
        targetDir = targetDir.endsWith(Constants.FILE_SEPARATOR) ? targetDir.substring(0, targetDir.length() - 1) : targetDir;
        File targetDirs = new File(targetDir);
        if (!targetDirs.exists()) {
            targetDirs.mkdirs();
        }

        FileInputStream fin = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(jarPath));
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                list.add(targetDir + Constants.FILE_SEPARATOR + entry.getName());
                //释放jar文件，如果在includeJars中，递归释放jar内的文件
                if (entry.getName().endsWith(".jar")) {
                    byte[] bytes = IoUtils.toByteArray(zipFile.getInputStream(entry));
                    IoUtils.writeFile(new File(targetDir + Constants.FILE_SEPARATOR + entry.getName()), bytes);
                    String jarName = entry.getName().replace("BOOT-INF" + Constants.FILE_SEPARATOR + "lib" + Constants.FILE_SEPARATOR, "");
                    jarName = jarName.replace("WEB-INF" + Constants.FILE_SEPARATOR + "lib" + Constants.FILE_SEPARATOR, "");
                    if (includeJars == null || includeJars.size() == 0 || includeJars.contains(jarName)) {
                        String targetPath0 = targetDir + Constants.FILE_SEPARATOR + entry.getName();
                        String targetDir0 = targetDir + Constants.FILE_SEPARATOR + entry.getName().replace(".jar", Constants.LIB_JAR_DIR);
                        List<String> list0 = unJar(targetPath0, targetDir0, includeJars);
                        list.addAll(list0);
                    }
                } else if (entry.isDirectory()) {
                    //如果是目录，创建目录
                    File dir = new File(targetDir + Constants.FILE_SEPARATOR + entry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    //其他文件，直接释放
                    byte[] bytes = IoUtils.toByteArray(zipFile.getInputStream(entry));
                    IoUtils.writeFile(new File(targetDir + Constants.FILE_SEPARATOR + entry.getName()), bytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(zipFile, fin);
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
