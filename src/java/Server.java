import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;

public class Server {

    private static final int SERVER_PORT = 12345; // Cambiar por el puerto deseado

    public static void main(String[] args) {
        try {
            // Inicio del servidor y espera de conexión del cliente
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Servidor escuchando en el puerto " + SERVER_PORT + " ...");
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado.");

            // Recepción de clave pública del cliente
            ObjectInputStream publicKeyInputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey clientPublicKey = (PublicKey) publicKeyInputStream.readObject();

            // Generación de par de claves para Diffie-Hellman
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Envío de clave pública al cliente
            ObjectOutputStream publicKeyOutputStream = new ObjectOutputStream(socket.getOutputStream());
            publicKeyOutputStream.writeObject(keyPair.getPublic());
            publicKeyOutputStream.flush();

            // Generación de clave compartida usando Diffie-Hellman
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(clientPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            // Derivación de clave de cifrado
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            SecretKeySpec secretKey = new SecretKeySpec(sharedSecretHash, "AES");

            // Recepción del archivo cifrado
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int encryptedFileSize = dataInputStream.readInt();
            byte[] encryptedFileBytes = new byte[encryptedFileSize];
            dataInputStream.readFully(encryptedFileBytes);

            // Descifrado del archivo con AES
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] fileBytes = aesCipher.doFinal(encryptedFileBytes);

            // Creación del archivo descifrado
            FileOutputStream fileOutputStream = new FileOutputStream("archivo_descifrado.txt");
            fileOutputStream.write(fileBytes);
            fileOutputStream.close();

            // Recepción del hash del cliente
            int hashSize = dataInputStream.readInt();
            byte[] clientHash = new byte[hashSize];
            dataInputStream.readFully(clientHash);

            // Cálculo del hash SHA-256 del archivo recibido
            byte[] serverHash = sha256.digest(fileBytes);

            // Comparación de los hashes
            boolean hashMatch = MessageDigest.isEqual(clientHash, serverHash);

            if (hashMatch) {
                System.out.println("Archivo transferido correctamente.");
            } else {
                System.out.println("Error: El archivo transferido está corrupto.");
            }

            // Cierre de la conexión
            socket.close();
            serverSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}