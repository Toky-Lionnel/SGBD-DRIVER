package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import execution.*;
import function.*;


public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private static final String DIRECTORY_PATH = Function.readConf("config/conf.txt", "DATABASE"); 
    private static final ThreadLocal<String> threadLocalDatabasePath = ThreadLocal.withInitial(() -> "");

    public String getDATABASE_PATH() {
        return threadLocalDatabasePath.get();
    }

    public void setDATABASE_PATH(String dATABASE_PATH) {
        threadLocalDatabasePath.set(dATABASE_PATH);
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        setDATABASE_PATH("");
    }


    public ClientHandler(Socket socket, String database) {
        this.clientSocket = socket;
        setDATABASE_PATH(database);
    }

    @Override
    public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

        String sqlCommand;
        while ((sqlCommand = in.readLine()) != null) {
            
            if (sqlCommand.startsWith("BDD")) {
                setDATABASE_PATH(parseDatabaseFromMessage(sqlCommand)+"/");
                continue;
            }

            System.out.println("Commande reçue de " + clientSocket.getInetAddress() + ": " + sqlCommand);


            if (sqlCommand.toUpperCase().contains("USE")) {
                try {
                    String[] tokens = sqlCommand.trim().split("\\s+");
                    Map<String, Object> parseSQL = parseUseDatabase(tokens);
                    executeUseDatabase((String) parseSQL.get("database_name"));
                    out.println("Utilisation de la BDD " + parseSQL.get("database_name"));
                } catch (Exception e) {
                    out.println("Erreur lors de l'utilisation de la base : " + e.getMessage());
                }
                out.println("END_OF_RESPONSE");
                out.flush();
                continue;
            }


            if (getDATABASE_PATH().equals("") && !sqlCommand.toUpperCase().contains("DATABASE")) {
                out.println("Veuillez choisir une base de donnée");
                out.println("END_OF_RESPONSE");
                out.flush();
                continue;
            }

            try {
                String response = executeSQL(sqlCommand, getDATABASE_PATH());
                out.println(response);
            } catch (Exception e) {
                out.println("Erreur lors de l'exécution : " + e.getMessage());
            }
            out.println("END_OF_RESPONSE");
            out.flush();

        }
    } catch (IOException e) {
        System.err.println("Erreur avec le client : " + e.getMessage());
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Impossible de fermer le socket client : " + e.getMessage());
        }
    }
}


    private static String executeSQL(String sqlCommand,String DATABASE_PATH) {
        try {
            String result = SQLExecutor.executeSQL(sqlCommand, DIRECTORY_PATH+DATABASE_PATH);  // Exécution
            return result;  
        } catch (Exception e) {
            return "Erreur lors de l'exécution : " + e.getMessage();  // Message d'erreur
        }
    }


    public void executeUseDatabase (String database) {
        setDATABASE_PATH(database+"/");
    }


    public static Map<String, Object> parseUseDatabase(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
    
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Commande USE incorrecte. Syntaxe attendue : USE <database>");
        }
    
        result.put("command", "USE");
        result.put("database_name", tokens[1]);    
        return result;
    }
    
    public static String parseDatabaseFromMessage(String message) {
    if (message == null || !message.startsWith("BDD : ")) {
        throw new IllegalArgumentException("Message invalide ou ne commence pas par 'BDD : '");
    }
    return message.substring(6).trim(); // Extrait tout après "BDD : " et supprime les espaces
}

    
}
