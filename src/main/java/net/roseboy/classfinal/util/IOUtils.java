package net.roseboy.classfinal.util;

import java.io.*;
import java.util.List;
import java.util.zip.CRC32;

public class IOUtils {

    /**
     * 写文件
     *
     * @param file
     * @param fileBytes
     */
    public static void writeFile(File file, byte[] fileBytes) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(fileBytes, 0, fileBytes.length);
            os.flush();
        } catch (Exception e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
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
     * @param input
     * @return
     * @throws IOException
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

//            byte[] byt = new byte[input.available()];
//            input.read(byt);
//            return byt;
        } finally {
            output.close();
            input.close();
        }
    }

    /**
     * 递归查找文件
     *
     * @param classes
     * @param dir
     * @param endWith
     */
    public static void listFile(List<File> classes, File dir, String endWith) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("目录[" + dir.getAbsolutePath() + "]不存在");
        }
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                listFile(classes, f, endWith);
            } else if (f.isFile() && f.getName().endsWith(endWith)) {
                classes.add(f);
            }
        }
    }

    /**
     * 枚举所有文件，包括文件夹
     *
     * @param filess
     * @param dir
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

    public static long crc32(byte[] bytes) {
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }

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
}
