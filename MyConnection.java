package driver;

import server.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyConnection {

    private final Socket socket;
    private final ClientHandler client;
    private final BufferedReader in;
    private final PrintWriter out;

    /**
     * Constructeur de la connexion personnalisée.
     *
     * @param url  L'URL contenant les informations de connexion (par ex. jdbc:mydb://localhost:5000).
     * @param info Les propriétés contenant les informations supplémentaires (nom d'utilisateur, mot de passe, etc.).
     * @throws SQLException En cas de problème de connexion.
     */

    public MyConnection(String url, Properties info) throws SQLException {

        try {
            Map <String,String> parseURL = parseDatabaseURL(url);

            String host = parseURL.get("host");
            int port = Integer.parseInt(parseURL.get("port"));
            String database = parseURL.get("database");

            // Création du socket et initialisation des flux
            this.socket = new Socket(host, port);
            this.client = new ClientHandler(socket, database);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connexion établie avec le serveur SGBD sur " + host + ":" + port);

        } catch (IOException | IllegalArgumentException e) {
            throw new SQLException("Erreur de connexion : " + e.getMessage(), e);
        }
    }


    public Statement createStatement() throws SQLException {
        return new MyStatement(out, in,this.client.getDATABASE_PATH());
    }


    public static Map<String, String> parseDatabaseURL(String url) {
        Map<String, String> result = new HashMap<>();

        // Vérifie si l'URL commence par "jdbc:mydatabase://"
        String prefix = "jdbc:mydatabase://";
        if (!url.startsWith(prefix)) {
            throw new IllegalArgumentException("URL invalide : doit commencer par " + prefix);
        }

        // Supprime le préfixe "jdbc:mydatabase://"
        String remainingUrl = url.substring(prefix.length());

        // Sépare l'adresse du port et de la base de données
        String[] parts = remainingUrl.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("URL invalide : doit contenir une adresse, un port et une base de données.");
        }

        // Sépare l'hôte et le port
        String hostAndPort = parts[0];
        String[] hostPortSplit = hostAndPort.split(":");
        if (hostPortSplit.length != 2) {
            throw new IllegalArgumentException("URL invalide : format attendu host:port.");
        }

        // Ajoute les éléments au résultat
        result.put("host", hostPortSplit[0]); // localhost
        result.put("port", hostPortSplit[1]); // 5000
        result.put("database", parts[1]);    // bdd

        return result;
    }


    /**
     * Méthode pour envoyer une requête au serveur.
     *
     * @param query La requête SQL à envoyer.
     * @throws IOException En cas de problème d'envoi ou de réception.
     */
    public void sendQuery(String query) throws IOException {
        out.println(query); // Envoyer la requête au serveur
        System.out.println("Requête envoyée : " + query);

        // Lire et afficher la réponse
        String response;
        while ((response = in.readLine()) != null) {
            System.out.println("Réponse reçue : " + response);
        }
    }

    /**
     * Ferme proprement les ressources utilisées par la connexion.
     *
     * @throws IOException En cas de problème lors de la fermeture.
     */
    public void close() throws IOException {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            System.out.println("Connexion fermée.");
        } catch (IOException e) {
            throw new IOException("Erreur lors de la fermeture des ressources : " + e.getMessage(), e);
        }
    }

    /**
     * Parse l'URL pour extraire l'hôte et le port.
     *
     * @param url L'URL de connexion (format attendu : jdbc:mydb://host:port).
     * @return Un tableau contenant l'hôte et le port.
     */
    private String[] parseUrl(String url) {
        if (url == null || !url.startsWith("jdbc:mydb://")) {
            throw new IllegalArgumentException("URL invalide. Format attendu : jdbc:mydb://host:port");
        }

        String[] parts = url.substring("jdbc:mydb://".length()).split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("URL invalide. L'URL doit inclure un hôte et un port.");
        }
        return parts;
    }

    /**
     * Authentifie le client auprès du serveur à l'aide des propriétés fournies.
     *
     * @param info Les propriétés contenant les informations d'authentification (username, password).
     * @throws IOException En cas de problème de communication avec le serveur.
     */
    private void authenticate(Properties info) throws IOException {
        String username = info.getProperty("username");
        String password = info.getProperty("password");

        if (username != null && password != null) {
            out.println("AUTH " + username + " " + password);
            String authResponse = in.readLine();
            if (!"OK".equalsIgnoreCase(authResponse)) {
                throw new IOException("Échec de l'authentification : " + authResponse);
            }
            System.out.println("Authentification réussie pour l'utilisateur : " + username);
        } else {
            System.out.println("Aucune information d'authentification fournie.");
        }
    }
}
