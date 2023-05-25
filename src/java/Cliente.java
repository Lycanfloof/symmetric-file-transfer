import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.*;

public class Cliente {
    public static void main(String[] args) {
        String direccionServidor = "localhost";
        int puerto = 12345;

        try {
            Socket socket = new Socket(direccionServidor, puerto);

            // Recibir clave pública del servidor
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey servidorPublicKey = (PublicKey) inputStream.readObject();

            // Generar par de claves Diffie-Hellman
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(keyPair.getPrivate());

            // Enviar clave pública al servidor
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(keyPair.getPublic());
            outputStream.flush();

            // Generar clave secreta compartida
            keyAgreement.doPhase(servidorPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            // Derivar clave AES a partir de la clave secreta compartida
            SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, 0, 16, "AES");

            // Enviar archivo cifrado
            OutputStream fileOutputStream = socket.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream("archivo.txt");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
            cipherOutputStream.close();
            fileInputStream.close();

            // Calcular hash SHA-256 del archivo
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256Digest.digest(Files.readAllBytes(new File("archivo.txt").toPath()));
            String hash = Base64.getEncoder().encodeToString(hashBytes);

            // Enviar hash al servidor
            outputStream.writeObject(hash);
            outputStream.flush();

            inputStream.close();
            outputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
