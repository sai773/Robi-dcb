package com.juno.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.juno.logs.ErrorLogger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AesEncryptDecrypt {
    private final int keySize;
    private final int iterationCount;
    private final Cipher cipher;
    
    public AesEncryptDecrypt(int keySize, int iterationCount) {
        this.keySize = keySize;
        this.iterationCount = iterationCount;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw fail(e);
        }
    }
    
    public String decrypt(String salt, String iv, String passphrase, String ciphertext) {
    	String res = null;
    	try {
        	/*SecretKey key = generateKey(salt, passphrase);
        	byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext));*/
        	PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
        	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                       
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(hex(iv)));
            byte[] input = Base64.decodeBase64(ciphertext.trim());
            byte[] decrypted = cipher.doFinal(input);
            res = new String(decrypted, "UTF-8");
            return res;
        }
        catch (UnsupportedEncodingException e) {
        	ErrorLogger.getLogger().error("Exception in decrypt : " + e);
            return null;
        }catch (Exception e){
        	ErrorLogger.getLogger().error("Exception in decrypt : " + e);
            return null;
        }
    }
    
    private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
        try {
            cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        }
        catch (InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | BadPaddingException e) {
        	ErrorLogger.getLogger().error("Exception in doFinal : " + e);
            return null;
        }
    }
    
    private SecretKey generateKey(String salt, String passphrase) {
        try {
        	PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        	ErrorLogger.getLogger().error("Exception in generateKey : " + e);
        	return null;
        }
    }

    public static byte[] base64(String str) {
        return Base64.decodeBase64(str);
    }
    
    public static byte[] hex(String str) {
        try {
            return Hex.decodeHex(str.toCharArray());
        }
        catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private IllegalStateException fail(Exception e) {
    	ErrorLogger.getLogger().error("IllegalStateException in fail : " + e);
    	return null;
    }
}