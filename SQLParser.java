package execution;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import relation.*;
import execution.*;
import operation.*;

public class SQLParser {

    // Méthode principale pour analyser les commandes SQL
    public static Map<String, Object> parse(String sql) {
        Map<String, Object> result = new HashMap<>();
        String[] tokens = sql.trim().split("\\s+");

        if (tokens[0].equalsIgnoreCase("DROP") && !tokens[1].equalsIgnoreCase("DATABASE")) {
            return parseDropTable(tokens);
        } else if (tokens[0].equalsIgnoreCase("DESCRIBE")) {
            return parseDescribeTable(tokens);
        } else if (tokens[0].equalsIgnoreCase("SELECT")) {
            return parseSelect(tokens);
        } else if (tokens[0].equalsIgnoreCase("INSERT")) {
            return parseInsert(sql);
        } else if (tokens[0].equalsIgnoreCase("CREATE") && tokens[1].equalsIgnoreCase("TABLE")) {
            return parseCreateTable(sql);
        } else if (tokens[0].equalsIgnoreCase("UPDATE")) {
            return parseUpdateCommand(sql);
        } else if (tokens[0].equalsIgnoreCase("DELETE")) {
            return parseDeleteCommand(sql);
        } else if (tokens[0].equalsIgnoreCase("USE")) {
            return parseUseDatabase(tokens);
        } else if (tokens[0].equalsIgnoreCase("SHOW") && tokens[1].equalsIgnoreCase("DATABASES")) {
            return parseShowDatabases(tokens);
        } else if (tokens[0].equalsIgnoreCase("DROP") && tokens[1].equalsIgnoreCase("DATABASE")) {
            return parseDropDatabase(tokens);
        } else if (tokens[0].equalsIgnoreCase("CREATE") && tokens[1].equalsIgnoreCase("DATABASE")) {
            return parseCreateDatabase(tokens);
        } else {
            throw new IllegalArgumentException("Commande SQL non reconnue : " + sql);
        }
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
    
    
    public static Map<String, Object> parseCreateDatabase(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "CREATE DATABASE");
        result.put("database_name", tokens[2]); 
        return result;
    }
    

    public static Map<String, Object> parseShowDatabases(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SHOW DATABASES");
        return result;
    }
    
    public static Map<String, Object> parseDropDatabase(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "DROP DATABASE");
        result.put("database_name", tokens[2]); 
        return result;
    }
    


    // Méthode pour analyser une commande UPDATE
    public static Map<String, Object> parseUpdateCommand(String sqlCommand) {
        Map<String, Object> parsedUpdate = new HashMap<>();

        // Vérifier que la commande commence par UPDATE
        sqlCommand = sqlCommand.trim();

        parsedUpdate.put("command", "UPDATE");

        if (!sqlCommand.toUpperCase().startsWith("UPDATE")) {
            throw new IllegalArgumentException("La commande SQL n'est pas valide pour une mise à jour.");
        }

        // Extraire le nom de la table
        String[] parts = sqlCommand.split("SET", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                    "Commande SQL invalide. Format attendu : UPDATE table_name SET col=val WHERE condition.");
        }

        String tableAndColumns = parts[0].trim();
        String tableName = tableAndColumns.split(" ")[1];
        parsedUpdate.put("table_name", tableName);

        // Extraire les colonnes et les valeurs
        String[] setAndWhere = parts[1].split("WHERE", 2);
        String[] columnAssignments = setAndWhere[0].split(",");

        Map<String, String> updates = new HashMap<>();
        for (String assignment : columnAssignments) {
            String[] keyValue = assignment.split("=");
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Erreur dans la section SET : " + assignment);
            }
            updates.put((String) keyValue[0].trim(), keyValue[1].trim());
        }
        parsedUpdate.put("updates", updates);

        // Extraire les conditions
        if (setAndWhere.length > 1) {
            parsedUpdate.put("condition", setAndWhere[1].trim());
        } else {
            parsedUpdate.put("condition", "");
        }

        return parsedUpdate;
    }

    // Methode pour analyser la commande DELETE
    public static Map<String, Object> parseDeleteCommand(String sqlCommand) {
        Map<String, Object> parsedDelete = new HashMap<>();

        // Vérifier que la commande commence par DELETE
        sqlCommand = sqlCommand.trim();
        parsedDelete.put("command", "DELETE");

        // Vérification que la commande commence bien par "DELETE"
        if (!sqlCommand.toUpperCase().startsWith("DELETE FROM")) {
            throw new IllegalArgumentException("La commande SQL n'est pas valide pour une suppression.");
        }

        // Extraire le nom de la table
        String[] parts = sqlCommand.split("WHERE", 2);
        if (parts.length < 2) {
            // Pas de condition => On met une chaîne vide
            parsedDelete.put("condition", "");
        } else {
            // Extraire la condition
            parsedDelete.put("condition", parts[1].trim());
        }

        // Extraire le nom de la table (avant le mot WHERE ou si aucune condition)
        String tableAndFrom = parts[0].trim();
        String tableName = tableAndFrom.split(" ")[2]; // Assumer que la table est après DELETE FROM
        parsedDelete.put("table_name", tableName);

        return parsedDelete;
    }

    public static Map<String, Object> parseDropTable(String[] tokens) {
        Map<String, Object> result = new HashMap<>();

        result.put("command", "DROP");
        result.put("table_name", tokens[2]);

        return result;
    }

    public static Map<String, Object> parseDescribeTable(String[] tokens) {
        Map<String, Object> result = new HashMap<>();

        result.put("command", "DESCRIBE");
        result.put("table_name", tokens[1]);
        return result;
    }

    // Méthode pour analyser la commande CREATE TABLE
    public static Map<String, Object> parseCreateTable(String sqlCommand) {

        Map<String, Object> parsedSQL = new HashMap<>();

        // Supprimer les espaces superflus et convertir la commande en majuscules
        sqlCommand = sqlCommand.trim().toUpperCase();

        // Vérifier si la commande commence par CREATE TABLE
        if (sqlCommand.startsWith("CREATE TABLE")) {
            parsedSQL.put("command", "CREATE");

            // Extraire le nom de la table et les colonnes
            Pattern pattern = Pattern.compile("CREATE TABLE (\\w+) \\((.*?)\\)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(sqlCommand);

            if (matcher.find()) {
                String tableName = matcher.group(1).trim();
                String columnsStr = matcher.group(2).trim();

                List<Map<String, String>> columns = new ArrayList<>();
                // Analyser les colonnes
                String[] columnDefinitions = columnsStr.split(",");
                for (String colDef : columnDefinitions) {
                    colDef = colDef.trim();
                    String[] parts = colDef.split(" ");
                    if (parts.length == 3) {
                        Map<String, String> column = new HashMap<>();
                        column.put("name", parts[0].trim());
                        column.put("type", parts[1].trim());
                        column.put("limite", parts[2].trim());
                        columns.add(column);
                    }
                }

                parsedSQL.put("table_name", tableName);
                parsedSQL.put("columns", columns);
            } else {
                throw new IllegalArgumentException("Erreur dans la syntaxe de CREATE TABLE.");
            }
        } else {
            throw new IllegalArgumentException("Commande CREATE TABLE non reconnue.");
        }
        return parsedSQL;
    }

    public static Map<String, Object> selectAll(String[] tokens) {
        Map<String, Object> result = new HashMap<>();

        // mijery oe misy WHERE ve ao am tokens[]
        int search = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                search = 1;
            } else if (tokens[i].equalsIgnoreCase("JOIN")) {
                search = -1;
                if (tokens[i - 1].equalsIgnoreCase("CROSS")) {
                    search = 2;
                }
            } else if (tokens[i].equalsIgnoreCase("ON")) {
                search = 3;
            }
        }

        // Raha tsy misy where dia manao alainy daoly
        if (search == 0) {
            result = selectNoCondition(tokens);
        }
        // Raha == 1 dia maka an le condition anaovana selection
        else if (search == 1) {
            result = selectCondition(tokens);
        }
        // Raha == 2 dia maka an le Table 2 anaovana produit cartesien
        else if (search == 2) {
            result = selectCrossJoin(tokens);
        } else if (search == 3) {
            result = selectTetaJointure(tokens);
        } else if (search == -1) {
            result = selectJointure(tokens);
        }

        return result;
    }

    public static Map<String, Object> selectJointure(String[] tokens) {

        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");
        result.put("table_name1", tokens[3]);
        result.put("operator", "jointure");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("JOIN")) {
                result.put("table_name2", tokens[i + 1]);
            }
        }

        return result;

    }

    public static Map<String, Object> selectTetaJointure(String[] tokens) {

        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");
        result.put("table_name1", tokens[3]);
        result.put("operator", "tetajointure");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("JOIN")) {
                result.put("table_name2", tokens[i + 1]);
            }
        }

        result.put("comparator", tokens[tokens.length - 2]);
        result.put("attribut1", tokens[tokens.length - 3]);
        result.put("attribut2", tokens[tokens.length - 1]);

        return result;

    }

    // select misy CROSS JOIN
    public static Map<String, Object> selectCrossJoin(String[] tokens) {

        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");
        result.put("table_name1", tokens[3]);
        result.put("operator", "cartProduct");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("CROSS")) {
                result.put("table_name2", tokens[i + 2]);
            }
        }

        return result;
    }

    // select misy WHERE
    public static Map<String, Object> selectCondition(String[] tokens) {
        String queryString = tokens[0];

        for (int i = 1; i < tokens.length; i++) {
            queryString += " " + tokens[i];
        }

        String condition = formatSQLConditions(queryString);

        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");
        result.put("table_name", tokens[3]);
        result.put("condition", condition);

        return result;
    }

    // select sans WHERE
    public static Map<String, Object> selectNoCondition(String[] tokens) {

        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");

        if (tokens.length < 4) {
            throw new IllegalArgumentException("Nom de table manquant dans la commande SELECT.");
        }
        result.put("table_name", tokens[3]);

        return result;
    }

    public static Map<String, Object> selectProjection(String[] tokens) {
        Map<String, Object> result = new HashMap<>();

        int search = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                search = 1;
            }
            if (tokens[i].equalsIgnoreCase("JOIN")) {
                if (tokens[i - 1].equalsIgnoreCase("CROSS")) {
                    search = 2;
                }
            }
        }

        // Raha tsy misy where dia manao alainy daoly
        if (search == 0) {
            result = selectProjectionWithoutCndt(tokens);
        }
        // Raha misy dia maka an le condition anaovana selection
        else if (search == 1) {
            result = selectProjectionWithCndt(tokens);
        } else if (search == 2) {
            result = projectionCrossJoin(tokens);
        }

        return result;
    }

    // projection misy CROSS JOIN
    public static Map<String, Object> projectionCrossJoin(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "!*");
        int limite = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("FROM")) {
                limite = i;
                result.put("table_name1", tokens[i + 1]);
            }

        }

        for (int j = 0; j < tokens.length; j++) {
            if (tokens[j].equalsIgnoreCase("CROSS")) {
                result.put("table_name2", tokens[j + 2]);
            }
        }

        for (int k = 1; k < limite; k++) {
            result.put("att" + k, tokens[k]);
        }

        return result;
    }

    public static Map<String, Object> selectProjectionWithCndt(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "!*");
        int limite = 0;

        String queryString = tokens[0];

        for (int i = 1; i < tokens.length; i++) {
            queryString += " " + tokens[i];
        }

        String condition = formatSQLConditions(queryString);

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("FROM")) {
                limite = i;
                result.put("table_name", tokens[i + 1]);
            }
        }

        for (int i = 1; i < limite; i++) {
            result.put("att" + i, tokens[i]);
        }

        result.put("condition", condition);

        return result;
    }

    public static Map<String, Object> selectProjectionWithoutCndt(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "!*");

        int limite = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("FROM")) {
                limite = i;
                result.put("table_name", tokens[i + 1]);
            }

        }

        for (int i = 1; i < limite; i++) {
            result.put("att" + i, tokens[i]);
        }

        return result;
    }

    // Analyse une commande SELECT
    public static Map<String, Object> parseSelect(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");

        if (tokens[1].equals("*")) {

            result = selectAll(tokens);
        } else if (tokens[1].startsWith("AVG") || tokens[1].startsWith("SUM") || tokens[1].startsWith("MIN")
                || tokens[1].startsWith("MAX")) {
    
            result = calculateAggregate(tokens);
        }
        else if (!tokens[1].startsWith("AVG") || !tokens[1].startsWith("SUM") || !tokens[1].startsWith("MIN")
                || !tokens[1].startsWith("MAX") || !tokens[1].equals("*")) {

            result = selectProjection(tokens);
        }
        return result;
    }

    public static Map<String, Object> calculateAggregate(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");

        System.out.println("Attributes: " + tokens[1]);
        result.put("need", tokens[1]);
        result.put("table_name", tokens[3]);

        return result;
    }

    // Nouvelle méthode pour analyser les commandes INSERT INTO
    public static Map<String, Object> parseInsert(String sql) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "INSERT");

        // Expression régulière pour capturer le schéma et les valeurs
        String regex = "INSERT INTO (\\w+) \\((.*?)\\) VALUES \\((.*?)\\)";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sql);

        if (matcher.matches()) {
            result.put("table_name", matcher.group(1));
            result.put("columns", List.of(matcher.group(2).split("\\s*,\\s*")));
            result.put("values", List.of(matcher.group(3).split("\\s*,\\s*")));
        } else {
            throw new IllegalArgumentException("Commande INSERT invalide.");
        }

        return result;
    }

    public static String formatSQLConditions(String input) {
        // Récupérer la partie après "WHERE"
        String conditions = input.split("WHERE", 2)[1].trim();

        // Diviser les conditions par "AND"
        String[] parts = conditions.split("AND");

        // Ajouter des parenthèses autour de chaque condition
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append("(").append(part.trim()).append(") ");
        }

        // Supprimer l'espace supplémentaire à la fin et retourner
        return result.toString().trim();
    }

}
