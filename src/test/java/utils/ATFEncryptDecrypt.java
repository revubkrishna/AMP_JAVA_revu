package utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ATFEncryptDecrypt {

    public static void main(String[] args)
    {
        System.out.println("Encrypted: " + Encrypt("abc@123"));
        System.out.println("Decrypted: " + Decrypt("HKLfdjakhflkfdaskjfh=="));
    }

    public static String Encrypt(String textToEncrypt) {
        String encryptedText = null;
        try {
            encryptedText = Encrypt(textToEncrypt, "automation");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return encryptedText;
    }

    private static String Decrypt(String textToEncrypt) {
        String decryptedText = null;
        try {
            decryptedText = Decrypt(textToEncrypt, "automation");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedText;
    }

    private static String Encrypt(String textToEncrypt, String automationType) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(automationType.getBytes(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(1, secretKeySpec);
        byte[] encrypted = cipher.doFinal(textToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String Decrypt(String encryptedText, String automationType) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(automationType.getBytes(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(2, secretKeySpec);
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypt);
    }
}