package server;

import execution.*;
import java.io.*;
import java.net.*;
import function.*;

public class DBServer {

    private static final int PORT = Integer.parseInt(Function.readConf("config/conf.txt", "PORT"));

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré. En écoute sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepte une nouvelle connexion
                System.out.println("Client connecté : " + clientSocket.getInetAddress());

                // Délègue le traitement du client à un thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur sur le serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
