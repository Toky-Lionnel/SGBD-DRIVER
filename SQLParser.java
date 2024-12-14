package execution;

import java.util.*;
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

        if (tokens[0].equalsIgnoreCase("SELECT")) {
            return parseSelect(tokens); // Appel à parseSelect
        } else if (tokens[0].equalsIgnoreCase("INSERT")) {
            return parseInsert(sql); // Appel à parseInsert
        } else if (tokens[0].equalsIgnoreCase("CREATE") && tokens[1].equalsIgnoreCase("TABLE")) {
            return parseCreateTable(sql); // Appel à parseCreateTable
        } else {
            throw new IllegalArgumentException("Commande SQL non reconnue : " + sql);
        }
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


    private static Map<String, Object> selectAll(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        
        // mijery oe misy WHERE ve ao am tokens[] 
        int search = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                search = 1;
            }
            else if (tokens[i].equalsIgnoreCase("JOIN")) {
                search = -1;
                if (tokens[i - 1].equalsIgnoreCase("CROSS") ) {
                    search = 2;                    
                }
            }
            else if (tokens[i].equalsIgnoreCase("ON") ) {
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
        }
        else if (search == 3) {
            result = selectTetaJointure(tokens);
        }
        else if (search == -1) {
            result = selectJointure(tokens);
        }

        return result;
    }

    private static Map<String, Object> selectJointure (String[] tokens) {
        
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

    private static Map<String, Object> selectTetaJointure (String[] tokens) {
        
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
    private static Map<String, Object> selectCrossJoin (String[] tokens) {

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
    private static Map<String, Object> selectCondition (String[] tokens) {
        String queryString = tokens[0];

        for (int i = 1; i < tokens.length; i++) {
            queryString +=  " " + tokens[i];
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
    private static Map<String, Object> selectNoCondition (String[] tokens) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "*");

        if (tokens.length < 4) {
            throw new IllegalArgumentException("Nom de table manquant dans la commande SELECT.");
        }
        result.put("table_name", tokens[3]);

        return result;
}

    private static Map<String, Object> selectProjection(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        
        int search = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("WHERE")) {
                search = 1;
            }
            if (tokens[i].equalsIgnoreCase("JOIN")) {
                if (tokens[i - 1].equalsIgnoreCase("CROSS") ) {
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
        }
        else if (search == 2) {
            result = projectionCrossJoin(tokens); 
        }

        return result;
    }

        // projection misy CROSS JOIN
        private static Map<String, Object> projectionCrossJoin (String[] tokens) {
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

    private static Map<String, Object> selectProjectionWithCndt (String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");
        result.put("need", "!*");
        int limite = 0;

        String queryString = tokens[0];

        for (int i = 1; i < tokens.length; i++) {
            queryString +=  " " + tokens[i];
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

    private static Map<String, Object> selectProjectionWithoutCndt (String[] tokens) {
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
    private static Map<String, Object> parseSelect(String[] tokens) {
        Map<String, Object> result = new HashMap<>();
        result.put("command", "SELECT");

        if (tokens[1].equals("*")) {
            result = selectAll(tokens);
        }
        else if (!tokens[1].equals("*")) {
            result = selectProjection(tokens);
        }

        return result;
    }

    // Nouvelle méthode pour analyser les commandes INSERT INTO
    private static Map<String, Object> parseInsert(String sql) {
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
