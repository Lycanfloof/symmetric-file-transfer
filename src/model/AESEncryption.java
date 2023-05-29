package src.model;

import java.security.*;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {

    private static final String ENCRYPTION = "AES";
    private static final String HASHING = "SHA-256";
    private static final String KEY_EXCHANGE = "DiffieHellman";

    private PublicKey publicKey;
    private KeyAgreement keyAgreement;
    private byte[] sharedSecret;

    public AESEncryption() {
        initKeyParams();
    }

    private void initKeyParams() {
        try {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_EXCHANGE);
            kpg.initialize(2048);

            KeyPair kp = kpg.generateKeyPair();
            publicKey = kp.getPublic();
            
            keyAgreement = KeyAgreement.getInstance(KEY_EXCHANGE);
            keyAgreement.init(kp.getPrivate());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setReceiverPublicKey(PublicKey receiverPublicKey) {
        try {

            keyAgreement.doPhase(receiverPublicKey, true);
            sharedSecret = keyAgreement.generateSecret();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] data) {
        try {

            Key key = generateKey();
            Cipher c = Cipher.getInstance(ENCRYPTION);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = c.doFinal(data);

            return encryptedData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public byte[] decrypt(byte[] encryptedData) {
        try {

            Key key = generateKey();
            Cipher c = Cipher.getInstance(ENCRYPTION);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = c.doFinal(encryptedData);

            return decryptedData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedData;
    }

    protected Key generateKey() throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance(HASHING);
        byte[] sharedSecretHash = sha256.digest(sharedSecret);

        return new SecretKeySpec(sharedSecretHash, ENCRYPTION);
    }

    public static String getEncryption() {
        return ENCRYPTION;
    }

    public static String getHashing() {
        return HASHING;
    }

    public static String getKeyExchange() {
        return KEY_EXCHANGE;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    
}