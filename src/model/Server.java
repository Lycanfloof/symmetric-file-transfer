package src.model;

import java.net.ServerSocket;
import java.security.PublicKey;

public class Server extends ConnectionPoint {

    private ServerSocket serverSocket;
    
    public void start(int port) {
        try {

            serverSocket = new ServerSocket(port);
            System.out.println("El servidor está escuchando el puerto " + port + ".");
            
            setSocket(serverSocket.accept());
            System.out.println("Cliente conectado.");

            handleRequest();

            serverSocket.close();
            System.out.println("Servidor cerrado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest() throws Exception {
        PublicKey serverKey = receivePublicKey();
        setReceiverPublicKey(serverKey);
        sendPublicKey();

        byte[] receivedHash = receiveEncryptedFileAndHash();
        decryptFile();
        byte[] calculatedHash = calculateFileHash();

        Boolean isEqual = compareHashes(receivedHash, calculatedHash);
        if (isEqual) {
            createFile("decrypted_file.txt");
            System.out.println("Archivo transferido correctamente.");
        } else {
            throw new Exception("Error: El archivo transferido está corrupto.");
        }
    }

}
