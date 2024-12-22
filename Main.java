package main;

import driver.*;
import java.util.Properties;
import java.sql.*;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws Exception {
        // Définir les informations de connexion
        String url = "jdbc:mydatabase://localhost:5000/test2";
        java.util.Properties info = new java.util.Properties();

        try {
            MyConnection connection = new MyConnection(url, info);

            MyStatement stat = (MyStatement) connection.createStatement();

            MyResultSet result = (MyResultSet) stat.executeQuery("SELECT AVG(duree) FROM Cours");

            while (result.next()) {
                System.out.println(""+result.getDouble(1));
            }

            // Fermeture des ressources
            result.close();
            stat.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
