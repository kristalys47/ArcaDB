package orc.helperClasses;

import java.nio.ByteBuffer;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private  String encryptionKey           = "key";
    private static final String characterEncoding       = "UTF-8";
    private static final String cipherTransformation    = "AES/CBC/PKCS5PADDING";
    private static final String aesEncryptionAlgorithem = "AES";
    private Cipher cipher;
    public AES(String encryptionKey){
        this.encryptionKey = encryptionKey;
        try {
            cipher = Cipher.getInstance(cipherTransformation);
            byte[] key = encryptionKey.getBytes(characterEncoding);
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
        } catch (Exception E) {
            System.err.println("Encryption Initialization Error : "+E.getMessage());
        }
    }

    public long encrypt(String plainText) {
        String encryptedText = "";
        ByteBuffer buffer = null;
        try {
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));
            buffer = ByteBuffer.wrap(cipherText);

        } catch  (Exception E) {
            System.err.println("Encryping Error : "+E.getMessage());
        }
        return buffer.getLong();
    }











}
