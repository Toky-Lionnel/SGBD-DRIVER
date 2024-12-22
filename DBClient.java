package server;

import java.io.*;
import java.net.*;
import function.*;

public class DBClient {  
    
    private static final String HOST = Function.readConf("config/conf.txt", "HOST");  // L'adresse du serveur
    private static final int PORT = Integer.parseInt(Function.readConf("config/conf.txt", "PORT"));  // Le port d'écoute

    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté au serveur sur " + HOST + ":" + PORT);
            System.out.print("Entrez une commande SQL ou 'exit' pour quitter : \n");

            // Lire et envoyer plusieurs commandes SQL
            String sqlCommand;
            while (true) {
                System.out.print("[SQL] -> ");
                sqlCommand = userInput.readLine();

                if (sqlCommand.equalsIgnoreCase("exit")) {
                    break;  // Quitter la boucle si l'utilisateur entre 'exit'
                }

                // Envoyer la commande au serveur
                out.println(sqlCommand);

                StringBuilder serverResponse = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !"END_OF_RESPONSE".equals(line)) {
                    serverResponse.append(line).append("\n");
                }

                System.out.println(serverResponse.toString());
            
            }

        } catch (IOException e) {
            System.err.println("Erreur côté client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

