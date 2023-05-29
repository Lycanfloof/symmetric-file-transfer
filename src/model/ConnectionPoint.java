package src.model;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public abstract class ConnectionPoint extends AESEncryption {

    private Socket socket;
    private byte[] fileBytes;
    private byte[] encryptedFileBytes;

    protected Socket getSocket() {
        return socket;
    }

    protected void setSocket(Socket socket) {
        this.socket = socket;
    }

    protected void sendPublicKey() throws IOException {
        OutputStream out = socket.getOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        PublicKey publicKey = getPublicKey();
        objectOut.writeObject(publicKey);
        objectOut.flush();
    }

    protected PublicKey receivePublicKey() throws IOException, ClassNotFoundException {
        InputStream in = socket.getInputStream();
        ObjectInputStream objectIn = new ObjectInputStream(in);

        return (PublicKey) objectIn.readObject();
    }

    protected void createFile(String fileName) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        fileOutputStream.write(fileBytes);
        fileOutputStream.close();
    }

    protected void readFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);

        fileBytes = new byte[(int) file.length()];
        fileInputStream.read(fileBytes);
        fileInputStream.close();
    }

    protected void encryptFile() {
        encryptedFileBytes = encrypt(fileBytes);
    }

    protected void decryptFile() {
        fileBytes = decrypt(encryptedFileBytes);
    }

    protected byte[] calculateFileHash() throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance(getHashing());
        return sha256.digest(fileBytes);
    }

    protected Boolean compareHashes(byte[] hash1, byte[] hash2) {
        return MessageDigest.isEqual(hash1, hash2);
    }

    protected void sendEncryptedFileAndHash() throws IOException, NoSuchAlgorithmException {
        OutputStream out = socket.getOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        
        dataOut.writeInt(encryptedFileBytes.length);
        dataOut.write(encryptedFileBytes);

        byte[] fileHash = calculateFileHash();
        dataOut.writeInt(fileHash.length);
        dataOut.write(fileHash);
        
        dataOut.flush();
    }

    protected byte[] receiveEncryptedFileAndHash() throws IOException {
        InputStream in = socket.getInputStream();
        DataInputStream dataIn = new DataInputStream(in);

        int encryptedFileSize = dataIn.readInt();
        encryptedFileBytes = new byte[encryptedFileSize];

        dataIn.readFully(encryptedFileBytes);

        int hashFileSize = dataIn.readInt();
        byte[] fileHash = new byte[hashFileSize];

        dataIn.readFully(fileHash);

        return fileHash;
    }

}
