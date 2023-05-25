import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.*;

public class Servidor {
    public static void main(String[] args) {
        int puerto = 12345;

        try {
            ServerSocket servidorSocket = new ServerSocket(puerto);
            System.out.println("Servidor escuchando en el puerto " + puerto);

            Socket socket = servidorSocket.accept();
            System.out.println("Cliente conectado");

            // Generar par de claves Diffie-Hellman
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(keyPair.getPrivate());

            // Enviar clave pública al cliente
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(keyPair.getPublic());
            outputStream.flush();

            // Recibir clave pública del cliente
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey clientePublicKey = (PublicKey) inputStream.readObject();

            // Generar clave secreta compartida
            keyAgreement.doPhase(clientePublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            // Derivar clave AES a partir de la clave secreta compartida
            SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, 0, 16, "AES");

            // Recibir archivo cifrado
            InputStream fileInputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream("archivo_recibido.txt");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            cipherInputStream.close();

            // Calcular hash SHA-256 del archivo recibido
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256Digest.digest(Files.readAllBytes(new File("archivo_recibido.txt").toPath()));
            String hash = Base64.getEncoder().encodeToString(hashBytes);

            // Recibir hash del cliente
            String clienteHash = (String) inputStream.readObject();

            // Comparar hashes
            if (hash.equals(clienteHash)) {
                System.out.println("Archivo transferido correctamente.");
            } else {
                System.out.println("Error en la transferencia del archivo.");
            }

            inputStream.close();
            outputStream.close();
            socket.close();
            servidorSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
