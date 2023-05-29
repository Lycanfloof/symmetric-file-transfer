package src.model;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Client extends ConnectionPoint {

    public void initTransfer(String host, int port, String fileName) {
        try {

            setSocket(new Socket(host, port));
            System.out.println("Conectado al servidor.");

            transfer(fileName);
            System.out.println("Archivo enviado correctamente.");

            getSocket().close();
            System.out.println("Conexión cerrada.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transfer(String fileName) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        sendPublicKey();
        PublicKey serverKey = receivePublicKey();
        setReceiverPublicKey(serverKey);
        
        readFile(fileName);
        encryptFile();
        sendEncryptedFileAndHash();
    }

}
