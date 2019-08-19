package net.roseboy.classfinal.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单加密解密
 *
 * @author roseboy
 */
public class EncryptUtils {
    //盐
    private static final String SALT = "whoisyourdaddy#$@#@";
    //rsa 长度
    private static int KEY_LENGTH = 1024;

    /**
     * 加密
     *
     * @param msg  内容
     * @param key  密钥
     * @param type 类型
     * @return 密文
     */
    public static byte[] en(byte[] msg, String key, int type) {
        if (type == 1) {
            return enAES(msg, md5(key + SALT));
        }
        return enSimple(msg, key);
    }

    /**
     * 解密
     *
     * @param msg  密文
     * @param key  密钥
     * @param type 类型
     * @return 明文
     */
    public static byte[] de(byte[] msg, String key, int type) {
        if (type == 1) {
            return deAES(msg, md5(key + SALT));
        }
        return deSimple(msg, key);
    }

    /**
     * 合并byte[]
     *
     * @param bts 字节数组
     * @return 合并后的字节
     */
    private static byte[] merger(byte[]... bts) {
        int lenght = 0;
        for (byte[] b : bts) {
            lenght += b.length;
        }

        byte[] bt = new byte[lenght];
        int lastLength = 0;
        for (byte[] b : bts) {
            System.arraycopy(b, 0, bt, lastLength, b.length);
            lastLength = b.length;
        }
        return bt;
    }

    /**
     * md5加密
     *
     * @param str 字符串
     * @return md5字串
     */
    public static byte[] md5byte(String str) {
        byte[] b = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            b = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * md5
     *
     * @param str 字串
     * @return 32位md5
     */
    public static String md5(String str) {
        String result = "";
        byte s[] = md5byte(str);

        for (int i = 0; i < s.length; i++) {
            result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
        }
        return result;
    }


    /**
     * 加密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 加密后的字节
     */
    public static byte[] enSimple(byte[] msg, int start, int end, String key) {
        byte[] keys = merger(md5byte(key + SALT), md5byte(SALT + key));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 解密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 解密后的字节
     */
    public static byte[] deSimple(byte[] msg, int start, int end, String key) {
        byte[] keys = merger(md5byte(key + SALT), md5byte(SALT + key));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 加密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 加密后的字节
     */
    public static byte[] enSimple(byte[] msg, String key) {
        return enSimple(msg, 0, msg.length - 1, key);
    }

    /**
     * 解密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 解密后的字节
     */
    public static byte[] deSimple(byte[] msg, String key) {
        return deSimple(msg, 0, msg.length - 1, key);
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String enRSA(String str, String publicKey) {
        try {
            byte[] in = str.getBytes("UTF-8");
            byte[] out = enRSA(in, publicKey);
            //String outStr = Base64.encodeBase64String(out);
            String outStr = Base64.getEncoder().encodeToString(out);
            return outStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA公钥加密
     *
     * @param msg       要加密的字节
     * @param publicKey 公钥
     * @return 解密后的字节
     */
    public static byte[] enRSA(byte[] msg, String publicKey) {
        try {
            //base64编码的公钥
            //byte[] decoded = Base64.decodeBase64(publicKey);
            byte[] decoded = Base64.getDecoder().decode(publicKey.getBytes("UTF-8"));
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return cipherDoFinal(cipher, msg, Cipher.ENCRYPT_MODE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * RSA私钥解密
     *
     * @param str        要解密的字符串
     * @param privateKey 私钥
     * @return 明文
     */
    public static String deRSA(String str, String privateKey) {
        try {
            //64位解码加密后的字符串
            //byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
            byte[] inputByte = Base64.getDecoder().decode(str.getBytes("UTF-8"));
            String outStr = new String(deRSA(inputByte, privateKey));
            return outStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA私钥解密
     *
     * @param msg        要解密的字节
     * @param privateKey 私钥
     * @return 解密后的字节
     */
    public static byte[] deRSA(byte[] msg, String privateKey) {
        try {
            //base64编码的私钥
            //byte[] decoded = Base64.decodeBase64(privateKey);
            byte[] decoded = Base64.getDecoder().decode(privateKey.getBytes("UTF-8"));
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return cipherDoFinal(cipher, msg, Cipher.DECRYPT_MODE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调用加密解密
     *
     * @param cipher Cipher
     * @param msg    要加密的字节
     * @param mode   解密/解密
     * @return 结果
     * @throws Exception Exception
     */
    private static byte[] cipherDoFinal(Cipher cipher, byte[] msg, int mode) throws Exception {
        int in_length = 0;
        if (mode == Cipher.ENCRYPT_MODE) {
            in_length = KEY_LENGTH / 8 - 11;
        } else if (mode == Cipher.DECRYPT_MODE) {
            in_length = KEY_LENGTH / 8;
        }

        byte[] in = new byte[in_length];
        byte[] out = new byte[0];

        for (int i = 0; i < msg.length; i++) {
            if (msg.length - i < in_length && i % in_length == 0) {
                in = new byte[msg.length - i];
            }
            in[i % in_length] = msg[i];
            if (i == (msg.length - 1) || (i % in_length + 1 == in_length)) {
                out = merger(out, cipher.doFinal(in));
            }
        }
        return out;
    }

    /**
     * 生成密钥对
     *
     * @return 密钥信息
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    public static Map<Integer, String> genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(KEY_LENGTH, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        BigInteger publicExponent = publicKey.getPublicExponent();
        BigInteger modulus = publicKey.getModulus();

        String publicKeyString = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.getEncoder().encode((privateKey.getEncoded())));

        Map<Integer, String> keyMap = new HashMap<>();
        keyMap.put(0, publicKeyString);  //0表示公钥
        keyMap.put(1, privateKeyString);  //1表示私钥
        keyMap.put(2, modulus.toString(16));//modulus
        keyMap.put(3, publicExponent.toString(16));//e
        return keyMap;
    }


    /**
     * AES加密字符串
     *
     * @param str 要加密的字符串
     * @param key 密钥
     * @return 加密结果
     */
    public static String enAES(String str, String key) {
        byte[] encrypted = null;
        try {
            encrypted = enAES(str.getBytes("utf-8"), key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted == null ? null : Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES加密字节
     *
     * @param msg 字节数组
     * @param key 密钥
     * @return 加密后的字节
     */
    public static byte[] enAES(byte[] msg, String key) {
        byte[] encrypted = null;
        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    /**
     * AES解密
     *
     * @param str 密文字串
     * @param key 密钥
     * @return 明文字串
     */
    public static String deAES(String str, String key) {
        String originalString = null;
        byte[] msg = Base64.getDecoder().decode(str);
        byte[] original = deAES(msg, key);
        try {
            originalString = new String(original, "utf-8");
        } catch (Exception e) {

        }
        return originalString;
    }

    /**
     * AES解密
     *
     * @param msg 要解密的字节
     * @param key 密钥
     * @return 明文字节
     */
    public static byte[] deAES(byte[] msg, String key) {
        byte[] original = null;
        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            original = cipher.doFinal(msg);
        } catch (Exception ex) {

        }
        return original;
    }

    /**
     * 加密测试
     *
     * @param args 参数
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        //加密字符串
        String message = "测试abcd1234！@#¥";
        for (int i = 0; i <= 10; i++) {
            message += message;
        }

//        //生成公钥和私钥
//        Map<Integer, String> keyMap = genKeyPair();
//        System.out.println("公钥:" + keyMap.get(0));
//        System.out.println("私钥:" + keyMap.get(1));
//        String messageEn = enRSA(message, keyMap.get(0));
//        System.out.println("密文:" + messageEn);
//        String messageDe = deRSA(messageEn, keyMap.get(1));
//        System.out.println("明文:" + message);
//        System.out.println("明文:" + messageDe);


        System.out.println("明文:" + message);
        String m = enAES(message, md5("admin"));
        System.out.println("密文:" + m);
        System.out.println("明文:" + deAES(m, md5("admin")));
        System.out.println("md5:" + md5("admin"));
    }
}
