package net.roseboy.classfinal.util;

import java.io.*;
import java.util.List;
import java.util.zip.CRC32;

/**
 * 工具
 *
 * @author roseboy
 */
public class IoUtils {

    /**
     * 写文件
     *
     * @param file      文件
     * @param fileBytes 字节
     */
    public static void writeFile(File file, byte[] fileBytes) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(fileBytes, 0, fileBytes.length);
            os.flush();
        } catch (Exception e) {
        } finally {
            close(os);
        }
    }

    /**
     * 读取文件
     *
     * @param file 文件
     * @return 字节
     */
    public static byte[] readFileToByte(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * stream2byte[]
     *
     * @param input 输入流
     * @return 字节
     * @throws IOException IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        } finally {
            close(output, input);
        }
    }

    /**
     * 递归查找文件，只返回文件
     *
     * @param fileList 返回的文件列表
     * @param dir      目录
     * @param endWith  文件后缀
     */
    public static void listFile(List<File> fileList, File dir, String endWith) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("目录[" + dir.getAbsolutePath() + "]不存在");
        }
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                listFile(fileList, f, endWith);
            } else if (f.isFile() && f.getName().endsWith(endWith)) {
                fileList.add(f);
            }
        }
    }

    /**
     * 枚举所有文件，包括文件夹
     *
     * @param filess 返回的文件列表
     * @param dir    目录
     */
    public static void listFile(List<File> filess, File dir) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("目录[" + dir.getAbsolutePath() + "]不存在");
        }
        File[] files = dir.listFiles();
        for (File f : files) {
            filess.add(f);
            if (f.isDirectory()) {
                listFile(filess, f);
            }
        }
    }

    /**
     * 删除整个目录
     *
     * @param dir 目录
     */
    public static void delete(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isFile()) {
            dir.delete();
        } else {
            File[] files = dir.listFiles();
            for (File f : files) {
                delete(f);
            }
        }
        dir.delete();
    }

    /**
     * 复制输入输出流
     *
     * @param input  输入流
     * @param output 输出流
     * @return 字节大小
     * @throws IOException IOException
     */
    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 计算cec
     *
     * @param bytes 字节
     * @return crc值
     */
    public static long crc32(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }


    /**
     * 关闭流
     *
     * @param outs Closeable
     */
    public static void close(Closeable... outs) {
        if (outs != null) {
            for (Closeable out : outs) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 读取文本文件
     *
     * @param file 文件
     * @return 内容
     */
    public static String readTxtFile(File file) {
        StringBuffer txt = new StringBuffer("");
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(new FileInputStream(file), "UTF-8");
            bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                txt.append(lineTxt).append("\r\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(bufferedReader, read);
        }
        return txt.toString();
    }

    /**
     * 写文件
     *
     * @param file 文件
     * @param txt  内容
     */
    public static void writeTxtFile(File file, String txt) {
        BufferedWriter out = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new BufferedWriter(new FileWriter(file));
            out.write(txt);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(out);
        }
    }
}
