package server;

import execution.*;
import java.io.*;
import java.net.*;
import function.*;

public class DBServer {

    private static final int PORT = Integer.parseInt(Function.readConf("config/conf.txt", "PORT"));
    private static final String DIRECTORY_PATH = Function.readConf("config/conf.txt", "DATABASE");  // Assurez-vous que ce chemin est correct

    public static void main(String[] args)  {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré. En écoute sur le port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Client connecté : " + clientSocket.getInetAddress());

                    // Lecture de la commande SQL envoyée par le client
                    String sqlCommand;
                
                    while ((sqlCommand = in.readLine()) != null) {
                        System.out.println("Commande reçue : " + sqlCommand);
        
                        // Exécution de la commande SQL et envoi de la réponse
                        String response = executeSQL(sqlCommand);
                        out.println(response);  // Envoi de la réponse au client
                        // Marquer la fin de la réponse
                        out.println("END_OF_RESPONSE");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur sur le serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour exécuter la commande SQL et retourner une réponse
    private static String executeSQL(String sqlCommand) {
        try {
            String result = SQLExecutor.executeSQL(sqlCommand, DIRECTORY_PATH);  // Exécution
            return result;  // Message de succès
        } catch (Exception e) {
            return "Erreur lors de l'exécution : " + e.getMessage();  // Message d'erreur
        }
    }
}
