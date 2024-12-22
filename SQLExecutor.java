package execution;

import java.io.File;
import java.io.IOException;
import java.util.*;
import relation.*;
import operation.*;
import function.*;

public class SQLExecutor {

    private static String DATABASE_DIRECTORY = Function.readConf("config/conf.txt", "DATABASE");


    // Méthode principale pour exécuter la commande SQL
    public static String executeSQL(String sqlCommand, String directoryPath) {

        String result = "";

        try {

            Map<String, Object> parsedSQL = SQLParser.parse(sqlCommand);

            // Identifier et exécuter la commande en fonction de son type
            String commandType = (String) parsedSQL.get("command");

            switch (commandType.toUpperCase()) {
                case "SELECT":
                    result += executeSelect(parsedSQL, directoryPath);
                    break;
                case "INSERT":
                    result += executeInsert(parsedSQL, directoryPath);
                    break;
                case "CREATE":
                    result += executeCreateTable(parsedSQL,directoryPath);
                    break;
                case "DESCRIBE":
                    result += executeDescribe(parsedSQL, directoryPath);
                    break;
                case "DROP":
                    result += executeDropTable(parsedSQL, directoryPath);
                    break;
                case "UPDATE":
                    result += executeUpdate(parsedSQL, directoryPath);
                    break;
                case "DELETE":
                    result += executeDelete(parsedSQL, directoryPath);
                    break;
                case "USE":
                    result += executeUseDatabase(parsedSQL);
                case "SHOW DATABASES":
                    result += executeShowDatabases();
                    break;
                case "DROP DATABASE":
                    result += executeDropDatabase(parsedSQL);
                    break;
                case "CREATE DATABASE":
                    result += executeCreateDatabase(parsedSQL);
                    break;
                default:
                    result += "Erreur : Commande SQL non supportée : " + commandType;
                    break;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            result += "Erreur d'analyse SQL : " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            result += "Erreur inattendue executeSQL: " + e.getMessage() + "\nCommande : " + sqlCommand;
        }

        return result;
    }


    public static String executeShowDatabases() {
        File databaseRoot = new File(DATABASE_DIRECTORY);
    
        if (!databaseRoot.exists() || !databaseRoot.isDirectory()) {
            return "Erreur : Le répertoire principal des bases de données est introuvable.";
        }
    
        // Lister les bases de données (sous-dossiers dans le répertoire principal)
        File[] directories = databaseRoot.listFiles(File::isDirectory);
        if (directories == null || directories.length == 0) {
            return "Aucune base de données disponible.";
        }
    
        StringBuilder result = new StringBuilder("Bases de données disponibles :\n");
        for (File dir : directories) {
            result.append("- ").append(dir.getName()).append("\n");
        }
        return result.toString();
    }


    public static String executeCreateDatabase(Map<String, Object> parsedCommand) {
        String databaseName = (String) parsedCommand.get("database_name");
    
        File databaseDirectory = new File(DATABASE_DIRECTORY + databaseName);
    
        if (databaseDirectory.exists()) {
            return "Erreur : La base de données '" + databaseName + "' existe déjà.";
        }
    
        // Création du dossier pour la base de données
        if (databaseDirectory.mkdirs()) {
            return "Base de données '" + databaseName + "' créée avec succès.";
        } else {
            return "Erreur : Impossible de créer la base de données '" + databaseName + "'.";
        }
    }
    
    
    public static String executeUseDatabase(Map<String, Object> parsedCommand) {
        String databaseName = (String) parsedCommand.get("database_name");

        File databaseDirectory = new File(DATABASE_DIRECTORY + databaseName);

        if (!databaseDirectory.exists() || !databaseDirectory.isDirectory()) {
            return "Erreur : La base de données '" + databaseName + "' n'existe pas.";
        }

        try {
            Function.modifyLastLine("config/conf.txt", databaseName+"/");
            return "Base de données '" + databaseName + "' utilisée.";
        } catch (Exception e) {
            return "Erreur : Impossible de mettre à jour la configuration pour la base de données '" + databaseName + "'.";
        }
    }

    public static String executeDropDatabase(Map<String, Object> parsedCommand) {
        String databaseName = (String) parsedCommand.get("database_name");
    
        File databaseDirectory = new File(DATABASE_DIRECTORY + databaseName);
    
        if (!databaseDirectory.exists() || !databaseDirectory.isDirectory()) {
            return "Erreur : La base de données '" + databaseName + "' n'existe pas.";
        }
    
        // Supprimer récursivement le dossier
        try {
            Function.deleteDirectory(databaseDirectory);
            return "Base de données '" + databaseName + "' supprimée avec succès.";
        } catch (IOException e) {
            return "Erreur : Impossible de supprimer la base de données '" + databaseName + "'.";
        }
    }

    

    // Exécution de la commande SELECT
    public static String executeSelect(Map<String, Object> parsedSQL, String directoryPath) {
        try {

            String resultat = "";

            if (parsedSQL.get("need").equals("*")) {

                int search = 0;
                for (String key : parsedSQL.keySet()) {

                    if (key.equals("condition")) {
                        search = 1;
                    } else if (key.equals("table_name2")) {
                        search = 2;
                    }

                }

                if (search == 0) {
                    resultat += selectAll(parsedSQL, directoryPath);
                } else if (search == 1) {
                    resultat += selectWithCondition(parsedSQL, directoryPath);
                } else if (search == 2) {
                    resultat += tableJoin(parsedSQL, directoryPath);
                }

            } else if (parsedSQL.get("need").equals("!*")) {

                int search = 0;
                for (String key : parsedSQL.keySet()) {

                    if (key.equals("condition")) {
                        search = 1;
                    } else if (key.equals("table_name2")) {
                        search = 2;
                    }

                }

                if (search == 0) {
                    resultat += selectProjectionWithoutCndt(parsedSQL, directoryPath);
                } else if (search == 1) {
                    resultat += selectProjectionWithCndt(parsedSQL, directoryPath);
                } else if (search == 2) {
                    resultat += tableJoin(parsedSQL, directoryPath);
                }
            } else if (!parsedSQL.get("need").equals("!*") || !parsedSQL.get("need").equals("*")) {
                resultat += calculateAggregate(parsedSQL, directoryPath);
            }

            return resultat;
        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;
    }

    public static String calculateAggregate(Map<String, Object> parsedSQL, String directoryPath) {
        try {
            String resultat = "";
            Function f = new Function();

            Relation rel = new Relation((String) parsedSQL.get("table_name"));

            TableStorage tableStorage = new TableStorage(rel, directoryPath);

            try {

                Relation table = tableStorage.readData(); // Lire les données de la deuxième table

                if (parsedSQL.get("need").toString().contains("MIN")
                        || parsedSQL.get("need").toString().contains("MAX")) {
                    Relation result = f.getMinOrMax(table, parsedSQL.get("need").toString());

                    resultat += result.afficheRelationToString();
                } else if (parsedSQL.get("need").toString().contains("AVG")) {
                    Relation result = f.getAvg(table, parsedSQL.get("need").toString());

                    resultat += result.afficheRelationToString();
                } else if (parsedSQL.get("need").toString().contains("SUM")) {
                    Relation result = f.getSum(table, parsedSQL.get("need").toString());
                
                    resultat += result.afficheRelationToString();
                }

                // resultat += relation.afficheRelationToString();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
            }

            return resultat;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String tableJoin(Map<String, Object> parsedSQL, String directoryPath) {
        try {
            String resultat = "";

            if (parsedSQL.get("need").equals("*")) {

                int search = 0;
                for (String key : parsedSQL.keySet()) {

                    if (key.equals("operator")) {
                        if (parsedSQL.get(key).equals("cartProduct")) {
                            search = 0;
                        } else if (parsedSQL.get(key).equals("jointure")) {
                            search = 1;
                        } else if (parsedSQL.get(key).equals("tetajointure")) {
                            search = 2;
                        }
                    }

                }

                if (search == 0) {
                    System.out.println("0");
                    resultat += selectCrossJoin(parsedSQL, directoryPath);
                } else if (search == 1) {
                    System.out.println("1");
                    resultat += selectJointure(parsedSQL, directoryPath);
                } else if (search == 2) {
                    System.out.println("2");
                    resultat += selectTetaJointure(parsedSQL, directoryPath);
                }

            } else if (parsedSQL.get("need").equals("!*")) {

                int search = 0;
                for (String key : parsedSQL.keySet()) {

                    if (key.equals("operator")) {
                        if (parsedSQL.get(key).equals("cartProduct")) {
                            search = 0;
                        } else if (parsedSQL.get(key).equals("jointure")) {
                            search = 1;
                        } else if (parsedSQL.get(key).equals("tetajointure")) {
                            search = 2;
                        }
                    }

                }

                if (search == 0) {
                    resultat += projectionCrossJoin(parsedSQL, directoryPath);
                } else if (search == 1) {
                } else if (search == 2) {
                }

            }

            return resultat;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String selectCrossJoin(Map<String, Object> parsedSQL, String directoryPath) throws Exception {

        String resultat = "";

        Relation rel1 = new Relation((String) parsedSQL.get("table_name1"));
        Relation rel2 = new Relation((String) parsedSQL.get("table_name2"));

        TableStorage tableStorage1 = new TableStorage(rel1, directoryPath);
        TableStorage tableStorage2 = new TableStorage(rel2, directoryPath);

        try {

            Relation table1 = tableStorage1.readData(); // Lire les données de la première table
            Relation table2 = tableStorage2.readData(); // Lire les données de la deuxième table

            // Effectuer la croisement des tables
            Relation crossJoin = Jointure.produitCartesien(table1, table2);
            resultat += crossJoin.afficheRelationToString();

            resultat += "\n (" + crossJoin.getNupl().size() + ") ligne(s) selectionnés";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
        }

        return resultat;
    }

    public static String projectionCrossJoin(Map<String, Object> parsedSQL, String directoryPath) throws Exception {

        String resultat = "";

        Relation rel1 = new Relation((String) parsedSQL.get("table_name1"));
        Relation rel2 = new Relation((String) parsedSQL.get("table_name2"));

        TableStorage tableStorage1 = new TableStorage(rel1, directoryPath);
        TableStorage tableStorage2 = new TableStorage(rel2, directoryPath);

        ArrayList<String> att = new ArrayList<String>();

        for (String key : parsedSQL.keySet()) {

            if (!key.equals("table_name1") && !key.equals("need")
                    && !key.equals("command") && !key.equals("table_name2")) {
                att.add(parsedSQL.get(key).toString()); // Ajoute la valeur associée à cette clé
            }

        }

        try {
            Relation table1 = tableStorage1.readData(); // Lire les données de la première table
            Relation table2 = tableStorage2.readData(); // Lire les données de la deuxième table

            // Effectuer la croisement des tables
            Relation crossJoin = Jointure.produitCartesien(table1, table2);
            Relation projection = Operation.projection(att, crossJoin);

            resultat += projection.afficheRelationToString();

            resultat += "\n (" + projection.getNupl().size() + ") ligne(s) selectionnés";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
        }

        return resultat;
    }

    public static String selectJointure(Map<String, Object> parsedSQL, String directoryPath) throws Exception {

        String result = "";

        Relation rel1 = new Relation((String) parsedSQL.get("table_name1"));
        Relation rel2 = new Relation((String) parsedSQL.get("table_name2"));

        TableStorage tableStorage1 = new TableStorage(rel1, directoryPath);
        TableStorage tableStorage2 = new TableStorage(rel2, directoryPath);

        try {
            Relation table1 = tableStorage1.readData(); // Lire les données de la première table
            Relation table2 = tableStorage2.readData(); // Lire les données de la deuxième table

            // Effectuer la croisement des tables
            Relation jointure = Jointure.jointure(table1, table2);

            result += jointure.afficheRelationToString();

            result += "\n (" + jointure.getNupl().size() + ") ligne(s) selectionnés";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
        }

        return result;
    }

    public static String selectTetaJointure(Map<String, Object> parsedSQL, String directoryPath) throws Exception {

        String result = "";

        Relation rel1 = new Relation((String) parsedSQL.get("table_name1"));
        Relation rel2 = new Relation((String) parsedSQL.get("table_name2"));

        TableStorage tableStorage1 = new TableStorage(rel1, directoryPath);
        TableStorage tableStorage2 = new TableStorage(rel2, directoryPath);

        ArrayList<String> attributes = new ArrayList<>();
        attributes.add((String) parsedSQL.get("attribut1"));
        attributes.add((String) parsedSQL.get("attribut2"));

        String comparator = (String) parsedSQL.get("comparator");
        try {
            Relation table1 = tableStorage1.readData(); // Lire les données de la première table
            Relation table2 = tableStorage2.readData(); // Lire les données de la deuxième table

            // Effectuer la croisement des tables
            Relation tetaJointure = Jointure.tetaJointure(table1, table2, attributes, comparator);

            result += tetaJointure.afficheRelationToString();

            result += "\n (" + tetaJointure.getNupl().size() + ") ligne(s) selectionnés";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la lecture des données : " + e.getMessage());
        }

        return result;
    }

    public static String selectAll(Map<String, Object> parsedSQL, String directoryPath) {

        String result = "";

        Relation rel = new Relation((String) parsedSQL.get("table_name"));

        TableStorage tableStorage = new TableStorage(rel, directoryPath);

        try {
            Relation table = tableStorage.readData(); // Lire les données de la table

            result += table.afficheRelationToString();
            result += "\n (" + table.getNupl().size() + ") ligne(s) selectionnés";
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result += "ERROR  selectAll : " + e.getMessage();
            return result;
        }
    }

    public static String selectWithCondition(Map<String, Object> parsedSQL, String directoryPath) {

        String result = "";

        try {

            String condition = parsedSQL.get("condition").toString();
            Relation rel = new Relation(parsedSQL.get("table_name").toString());

            TableStorage tableStorage = new TableStorage(rel, directoryPath);

            try {
                Relation table = tableStorage.readData();

                // Lire les données de la table
                Relation selection = Operation.selectionMultiple(condition, table);

                result += selection.afficheRelationToString();
                result += "\n (" + selection.getNupl().size() + ") ligne(s) selectionnés";

            } catch (Exception e) {
                e.printStackTrace();
                result += "ERROR selectWithCondition: " + e.getMessage();
            }

            return result;

        } catch (Exception e) {
            // TODO: handle exception
            return "ERROR selectWithCondition : " + e.getMessage();
        }
    }

    public static String selectProjectionWithoutCndt(Map<String, Object> parsedSQL, String directoryPath) {

        String result = "";

        try {

            ArrayList<String> att = new ArrayList<String>();

            for (String key : parsedSQL.keySet()) {

                if (!key.equals("table_name") && !key.equals("need") && !key.equals("command")) {
                    att.add(parsedSQL.get(key).toString()); // Ajoute la valeur associée à cette clé
                }

            }

            Relation rel = new Relation((String) parsedSQL.get("table_name"));

            TableStorage tableStorage = new TableStorage(rel, directoryPath);

            try {
                Relation table = tableStorage.readData(); // Lire les données de la table
                Relation projection = Operation.projection(att, table);

                result += projection.afficheRelationToString();
                result += "\n (" + projection.getNupl().size() + ") ligne(s) selectionnés";

            } catch (Exception e) {
                e.printStackTrace();
                result += "ERROR selectProjectionWithoutCndt : " + e.getMessage();
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result += "ERROR selectProjectionWithoutCndt : " + e.getMessage();
            return result;
        }
    }

    public static String selectProjectionWithCndt(Map<String, Object> parsedSQL, String directoryPath) {

        String result = "";

        try {

            ArrayList<String> att = new ArrayList<String>();

            for (String key : parsedSQL.keySet()) {

                if (!key.equals("table_name") && !key.equals("need") && !key.equals("command")
                        && !key.equals("condition")) {
                    String sql = parsedSQL.get(key).toString();
                    att.add(sql); // Ajoute la valeur associée à cette clé
                }

            }

            String condition = parsedSQL.get("condition").toString();

            Relation rel = new Relation((String) parsedSQL.get("table_name"));

            TableStorage tableStorage = new TableStorage(rel, directoryPath);

            try {
                Relation table = tableStorage.readData(); // Lire les données de la table
                Relation projection = Operation.projection(att, table);
                Relation select = Operation.selectionMultiple(condition, projection);

                result += select.afficheRelationToString();

                result += "\n (" + select.getNupl().size() + ") ligne(s) selectionnés";

            } catch (Exception e) {
                e.printStackTrace();
                result += "ERROR selectProjectionWithCndt : " + e.getMessage();
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR selectProjectionWithCndt: " + e.getMessage();
        }
    }

    // Exécution de la commande INSERT
    public static String executeInsert(Map<String, Object> parsedSQL, String directoryPath) {

        String result = "";

        Relation rel = new Relation((String) parsedSQL.get("table_name"));

        List<String> values = (List<String>) parsedSQL.get("values");

        TableStorage tableStorage = new TableStorage(rel, directoryPath);

        try {
            tableStorage.insertData(values); // Insérer les données dans la table
            result += "QUERY OK , Insertion terminé";
        } catch (Exception e) {
            e.printStackTrace();
            result += "Erreur lors de l'insertion des données : " + e.getMessage();
        }
        return result;
    }

    // Exécution de la commande CREATE TABLE
    public static String executeCreateTable(Map<String, Object> parsedSQL,String directoryPath) {

        String result = "";

        try {

            Relation rel = new Relation();
            Domaine d = new Domaine();
            Attribut att = new Attribut();
            ArrayList<Attribut> attList = new ArrayList<Attribut>();

            // Extraire les informations nécessaires
            String tableName = (String) parsedSQL.get("table_name");
            List<Map<String, String>> columns = (List<Map<String, String>>) parsedSQL.get("columns");

            for (Map<String, String> column : columns) {
                d = new Domaine(column.get("type"), Integer.parseInt(column.get("limite")));
                att = new Attribut(column.get("name"), d);
                attList.add(att);
            }

            rel = new Relation(tableName, attList);

            TableStorage storage = new TableStorage(rel, directoryPath);

            try {
                // Appeler la méthode createSchema
                storage.createSchema(rel);
                storage.createDataFile();
                result += "QUERY OK , Table crée avec succès";
            } catch (Exception e) {
                e.printStackTrace();
                result += "Erreur lors de la création du schéma pour la table '" + tableName + "' : " + e.getMessage();
            }

            return result;

        } catch (Exception exception) {
            exception.printStackTrace();
            return "ERREUR LORS DE LA CREATION DE LA TABLE : " + exception.getMessage();
        }
    }

    public static String executeDescribe(Map<String, Object> parsedSQL, String directoryPath) {
        String result = "";

        System.out.println("nom de table + " + (String) parsedSQL.get("table_name"));
        Relation rel = new Relation((String) parsedSQL.get("table_name"));
        TableStorage tableStorage = new TableStorage(rel, directoryPath);

        try {
            List<String> discribeTable = tableStorage.describeTable();

            for (String string : discribeTable) {
                result += string + "\n";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String executeDropTable(Map<String, Object> parsedSQL, String directoryPath) {
        String result = "";

        Relation rel = new Relation((String) parsedSQL.get("table_name"));
        TableStorage tableStorage = new TableStorage(rel, directoryPath);

        try {

            result = tableStorage.dropTable();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // Méthode pour exécuter une commande UPDATE
    public static String executeUpdate(Map<String, Object> parsedUpdate, String filePath) throws Exception {

        String result = "";
        try {

            String tableName = (String) parsedUpdate.get("table_name");
            tableName = tableName.toLowerCase();
            Map<String, String> updates = (Map<String, String>) parsedUpdate.get("updates");
            String condition = (String) parsedUpdate.get("condition");

            StringBuilder valeurUpdate = new StringBuilder();
            for (Map.Entry<String, String> entry : updates.entrySet()) {
                if (valeurUpdate.length() > 0) {
                    valeurUpdate.append(", "); // Séparateur entre les paires
                }
                valeurUpdate.append(entry.getKey()).append("=").append(entry.getValue());
            }

            File inputFile = new File(filePath + "/" + tableName + ".data");

            Relation relation_up = TableStorage.getRelation(filePath + "/" + tableName);
            Relation up = Update.updateTable(relation_up, condition, valeurUpdate.toString());
            TableStorage.writeObjectListToFile(up.getNupl(), filePath + "/" + tableName + ".data");

            result += "Modification effectué avec succès";

        } catch (Exception e) {
            result += e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public static String executeDelete(Map<String, Object> parsedDelete, String filePath) throws Exception {
        String result = "";

        try {
            // Récupérer le nom de la table et la condition de suppression
            String tableName = (String) parsedDelete.get("table_name");
            tableName = tableName.toLowerCase();
            String condition = (String) parsedDelete.get("condition");

            // Ouvrir le fichier de la table concernée
            File inputFile = new File(filePath + "/" + tableName + ".data");
            Relation relation_delete = TableStorage.getRelation(filePath + "/" + tableName);

            // Supprimer les lignes correspondant à la condition
            Relation deleted = Update.deleteTable(relation_delete, condition);

            // Sauvegarder la table mise à jour
            TableStorage.writeObjectListToFile(deleted.getNupl(), filePath + "/" + tableName + ".data");

            result += "Suppression effectuée avec succès.";

        } catch (Exception e) {
            result = "Erreur : " + e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

}

/* 
 * CREATE TABLE test1 (ID INTEGER 100, NOM STRING 255)
 * 
 * INSERT INTO test1 (ID, NOM) VALUES (1, Archi)
 * INSERT INTO test1 (ID, NOM) VALUES (2, Algo)
 * INSERT INTO test1 (ID, NOM) VALUES (3, Syst)
 * 
 * CREATE TABLE test2 (ID INTEGER 100, NOM STRING 255)
 * 
 * INSERT INTO test2 (ID, NOM) VALUES (1, Archi)
 * INSERT INTO test2 (ID, NOM) VALUES (2, Algo)
 * INSERT INTO test2 (ID, NOM) VALUES (3, Math)
 * 
*/

/*
 * CREATE TABLE Table_name (Attribute1 Attribute1_type Limite1, Attribute2
 * Attribute2_type Limite2)
 * 
 * INSERT INTO Table_name (Attribute1, Attribute2) VALUES (Value1, Values2)
 * 
 * -> selection
 * 
 * SELECT * FROM Table_name
 * 
 * SELECT * FROM Table_name WHERE condition1 AND condition2 -> selection
 * multiple
 * 
 * SELECT * FROM Table_name1 CROSS JOIN Table_name2 -> produit cartesienne
 * 
 * SELECT * FROM Table_name1 JOIN Table_name2 -> jointure simple
 * 
 * SELECT * FROM Table_name1 INNER JOIN Table_name2 ON Table_name1.Attribute !=
 * Table_name2.Attribute -> teta-jointure
 * 
 * -> projection
 * 
 * SELECT Attribute1 Attribute2 FROM Table_name
 * 
 * SELECT Attribute1 Attribute2 FROM Table_name WHERE condition1 AND condition2
 * -> selection multiple
 * 
 * SELECT Attribute1 FROM Table_name1 CROSS JOIN Table_name2 -> produit
 * cartesienne
 * 
 * DROP TABLE test2
 * 
 * DELETE FROM test2
 * 
 * DELETE FROM test2 WHERE ID=10
 * 
 * UPDATE test2 SET ID = 10 WHERE ID = 1
 * 
 * SELECT AVG(id)/MAX()/MIN()/SUM() FROM test2
 */