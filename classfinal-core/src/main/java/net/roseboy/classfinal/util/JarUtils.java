package net.roseboy.classfinal.util;

import net.roseboy.classfinal.Const;

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
    //打包时需要删除的文件
    public static final String[] DLE_FILES = {".DS_Store", "Thumbs.db"};

    /**
     * 把目录压缩成jar
     *
     * @param jarDir    需要打包的目录
     * @param targetJar 打包出的jar/war文件路径
     * @return 打包出的jar/war文件路径
     */
    public static String doJar(String jarDir, String targetJar) {
        File jarDirFile = new File(jarDir);
        //枚举jarDir下的所有文件以及目录
        List<File> files = new ArrayList<>();
        IoUtils.listFile(files, jarDirFile);

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
                String fileName = file.getAbsolutePath().substring(jarDirFile.getAbsolutePath().length() + 1);
                fileName = fileName.replace(File.separator, "/");
                //目录，添加一个目录entry
                if (file.isDirectory()) {
                    ZipEntry ze = new ZipEntry(fileName + "/");
                    ze.setTime(System.currentTimeMillis());
                    zos.putNextEntry(ze);
                    zos.closeEntry();
                }
                //jar文件，需要写crc信息
                else if (fileName.endsWith(".jar")) {
                    byte[] bytes = IoUtils.readFileToByte(file);
                    ZipEntry ze = new ZipEntry(fileName);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(bytes.length);
                    ze.setTime(System.currentTimeMillis());
                    ze.setCrc(IoUtils.crc32(bytes));
                    zos.putNextEntry(ze);
                    zos.write(bytes);
                    zos.closeEntry();
                }
                //其他文件直接写入
                else {
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
            IoUtils.close(zos, out);
        }
        return targetJar;
    }

    /**
     * 释放jar内以及子jar的所有文件
     *
     * @param jarPath     jar文件
     * @param targetDir   释放文件夹
     * @param includeJars 需要再次释放的jar文件，为空释放所有
     * @return 所有文件的完整路径，包含目录
     */
    public static List<String> unJar(String jarPath, String targetDir, List<String> includeJars) {
        return unJar(jarPath, targetDir, includeJars, null, null);
    }

    /**
     * 释放jar内以及子jar的所有文件
     *
     * @param jarPath      jar文件
     * @param targetDir    释放文件夹
     * @param includeJars  需要再次释放的jar文件，为空释放所有
     * @param excludeFiles 排除释放的文件，为空释放所有
     * @return 所有文件的完整路径，包含目录
     */
    public static List<String> unJar(String jarPath, String targetDir, List<String> includeJars,
                                     List<String> includeFiles, List<String> excludeFiles) {
        List<String> list = new ArrayList<>();
        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdirs();
        }

        FileInputStream fin = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(jarPath));
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File targetFile = new File(target, entry.getName());

                //跳过排除的文件
                if (excludeFiles != null
                        && (excludeFiles.contains(entry.getName()) || excludeFiles.contains(targetFile.getName()))) {
                    continue;
                }

                //跳过未包含的文件
                if (includeFiles != null && includeFiles.size() > 0 && !entry.isDirectory()
                        && (!includeFiles.contains(entry.getName()) && !includeFiles.contains(targetFile.getName()))) {
                    continue;
                }

                //释放jar文件，如果在includeJars中，递归释放jar内的文件
                if (entry.getName().endsWith(".jar")) {
                    byte[] bytes = IoUtils.toBytes(zipFile.getInputStream(entry));
                    IoUtils.writeFile(targetFile, bytes);
                    String jarName = targetFile.getName();
                    if (includeJars == null || includeJars.size() == 0 || includeJars.contains(jarName)) {
                        String targetPath0 = targetFile.getAbsolutePath();
                        String targetDir0 = targetFile.getAbsolutePath().replace(".jar", Const.LIB_JAR_DIR);
                        List<String> list0 = unJar(targetPath0, targetDir0, includeJars, includeFiles, excludeFiles);
                        list.addAll(list0);
                    }
                }
                //如果是目录，创建目录
                else if (entry.isDirectory()) {
                    if (!targetFile.exists()) {
                        targetFile.mkdirs();
                    }
                }
                //其他文件，直接释放
                else {
                    byte[] bytes = IoUtils.toBytes(zipFile.getInputStream(entry));
                    IoUtils.writeFile(targetFile, bytes);
                }
                list.add(targetFile.getAbsolutePath());
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
            return IoUtils.toBytes(is);
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
        for (String f : DLE_FILES) {
            if (file.getAbsolutePath().endsWith(f)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取class运行的classes目录或所在的jar包目录
     *
     * @return 路径字符串
     */
    public static String getRootPath() {
        String path = JarUtils.class.getResource("").getPath();
        try {
            path = java.net.URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }
        if (path.contains("!")) {
            path = path.substring(0, path.indexOf("!"));
        }
        if (path.contains("/classes/")) {
            path = path.substring(0, path.indexOf("/classes/") + 9);
        }
        return path;
    }

}
