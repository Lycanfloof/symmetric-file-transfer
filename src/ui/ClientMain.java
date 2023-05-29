package src.ui;

import java.util.Scanner;
import src.model.*;

public class ClientMain {

    public static void main(String[] args) {
        String[] ls = askForInput();

        Client client = new Client();
        client.initTransfer(ls[0], Integer.valueOf(ls[1]), ls[2]);
    }

    private static String[] askForInput() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Dirección IP|Puerto|Nombre_de_Archivo:");
        String in = sc.nextLine();
        
        sc.close();

        return in.split("\\|");
    }

}
