package utils;

import javax.crypto.*;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * 密码学工具类，支持的算法格式详见：https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html
 */
public class CryptoUtils {

    /**
     * 将便于网络传输的byte[]格式公钥转换为便于程序处理的PublicKey格式公钥
     * @param asymmetricAlgorithm 非对称加密算法
     * @param encodedKey byte[]格式的公钥
     * @return PublicKey格式的公钥
     */
    public static PublicKey parseEncodedPublicKey(String asymmetricAlgorithm, byte[] encodedKey) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(asymmetricAlgorithm);
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * @param asymmetricAlgorithm 非对称加密算法
     * @param encodedKey byte[]格式的私钥
     * @return PrivateKey格式的私钥
     */
    public static PrivateKey parseEncodedPrivateKey(String asymmetricAlgorithm, byte[] encodedKey) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(asymmetricAlgorithm);
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 以指定算法生成非对称加密公私钥对
     * @param asymmetricAlgorithm 非对称加密算法
     * @return 生成的公私钥对
     */
    public static KeyPair generateKeyPair(String asymmetricAlgorithm) {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(asymmetricAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * 根据publicKey中的参数，以DH算法生成非对称加密公私钥对
     * @param publicKey 包含参数的DH非对称加密公钥
     * @return 生成的公私钥对
     */
    public static KeyPair generateKeyPairWithParams(DHPublicKey publicKey) {
        DHParameterSpec params = publicKey.getParams();
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(params);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * 根据DH密钥协商算法，以及指定的摘要、对称加密算法生成对称加密密钥
     * @param digestAlgorithm 摘要算法
     * @param symmetricAlgorithm 对称加密算法
     * @param publicKey 密钥协商中对方的临时公钥
     * @param privateKey 密钥协商中自己的临时私钥
     * @return 生成的对称加密密钥
     */
    public static SecretKey generateSecretKey(String digestAlgorithm, String symmetricAlgorithm, DHPublicKey publicKey, DHPrivateKey privateKey) {
        byte[] secretArr = new byte[0];
        MessageDigest messageDigest = null;
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            secretArr = keyAgreement.generateSecret();
            messageDigest = MessageDigest.getInstance(digestAlgorithm);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        byte[] digest = messageDigest.digest(secretArr);
        SecretKey secret = new SecretKeySpec(digest, symmetricAlgorithm);
        return secret;
    }

    /**
     * 对称加密
     * @param symmetricAlgorithm 对称加密算法
     * @param raw 待加密内容
     * @param secretKey 对称加密密钥
     * @return 加密内容
     */
    public static byte[] encrypt(String symmetricAlgorithm, byte[] raw, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(symmetricAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(raw);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对称解密
     * @param symmetricAlgorithm 对称加密算法
     * @param enc 加密内容
     * @param secretKey 对称加密密钥
     * @return 解密内容
     */
    public static byte[] decrypt(String symmetricAlgorithm, byte[] enc, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(symmetricAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(enc);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 签名
     * @param digestAlgorithm 摘要算法
     * @param asymmetricAlgorithm 非对称加密算法
     * @param raw 待签名内容
     * @param privateKey 自己的私钥
     * @return （对摘要的）签名
     */
    public static byte[] sign(String digestAlgorithm, String asymmetricAlgorithm, byte[] raw, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(digestAlgorithm.replace("-", "") + "with" + asymmetricAlgorithm);
            signature.initSign(privateKey);
            signature.update(raw);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验签
     * @param digestAlgorithm 摘要算法
     * @param asymmetricAlgorithm 非对称加密算法
     * @param raw 原始内容
     * @param sig （对原始内容摘要的）签名
     * @param publicKey 签名者公钥
     * @return 验签是否通过
     */
    public static boolean verify(String digestAlgorithm, String asymmetricAlgorithm, byte[] raw, byte[] sig, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(digestAlgorithm.replace("-", "") + "with" + asymmetricAlgorithm);
            signature.initVerify(publicKey);
            signature.update(raw);
            return signature.verify(sig);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

}
