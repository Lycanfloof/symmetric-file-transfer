package src.ui;

import java.util.Scanner;

import src.model.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();

        int port = askForInput();
        server.start(port);
    }

    private static int askForInput() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Puerto:");
        String in = sc.nextLine();
        
        sc.close();

        return Integer.valueOf(in);
    }
}
