package com.gildStudios.DiTo.androidApp;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypt {
    private static final String AES_cryptAlgorithm = "AES";
    private static final String AES_cryptKey = "1Hbfh667adfDEJ78";

    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.AES_cryptAlgorithm);
        cipher.init(1, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, 0);
        return encryptedValue64;

    }

    public static String decrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.AES_cryptAlgorithm);
        cipher.init(2, key);
        byte[] decryptedValue64 = Base64.decode(value, 0);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        String decryptedValue = new String(decryptedByteValue,"UTF-8");
        return decryptedValue;

    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(AESCrypt.AES_cryptKey.getBytes(), AESCrypt.AES_cryptAlgorithm);
        return key;
    }
}
