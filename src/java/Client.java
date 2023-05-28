import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;

public class Client {

    private static final String SERVER_HOST = "localhost"; // Cambiar por la dirección IP o el nombre de dominio del servidor
    private static final int SERVER_PORT = 12345; // Cambiar por el puerto deseado

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Debe proporcionar un nombre de archivo como parámetro.");
            return;
        }

        String filename = args[0];

        try {
            // Conexión al servidor
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Conectado al servidor.");

            // Generación de par de claves para Diffie-Hellman
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Envío de clave pública al servidor
            ObjectOutputStream publicKeyOutputStream = new ObjectOutputStream(socket.getOutputStream());
            publicKeyOutputStream.writeObject(keyPair.getPublic());
            publicKeyOutputStream.flush();

            // Recepción de clave pública del servidor
            ObjectInputStream publicKeyInputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey serverPublicKey = (PublicKey) publicKeyInputStream.readObject();

            // Generación de clave compartida usando Diffie-Hellman
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(serverPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            // Derivación de clave de cifrado
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            SecretKeySpec secretKey = new SecretKeySpec(sharedSecretHash, "AES");

            // Cifrado del archivo con AES y transferencia al servidor
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Lectura y cifrado del archivo
            File file = new File(filename);
            byte[] fileBytes = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileBytes);
            fileInputStream.close();

            byte[] encryptedFileBytes = aesCipher.doFinal(fileBytes);

            // Envío del archivo cifrado al servidor
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(encryptedFileBytes.length);
            dataOutputStream.write(encryptedFileBytes);
            dataOutputStream.flush();

            // Cálculo del hash SHA-256 del archivo original
            byte[] fileHash = sha256.digest(fileBytes);

            // Envío del hash al servidor
            dataOutputStream.writeInt(fileHash.length);
            dataOutputStream.write(fileHash);
            dataOutputStream.flush();

            System.out.println("Archivo enviado correctamente.");

            // Cierre de la conexión
            socket.close();
            System.out.println("Conexión cerrada.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}