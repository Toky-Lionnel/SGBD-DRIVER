package execution;

import java.io.*;
import java.util.*;
import operation.*;
import relation.*;

public class TableStorage {

    private String tableName;
    private Relation relation;
    private String directoryPath;

    public TableStorage(String tableName, String directoryPath) {
        this.tableName = tableName;
        this.directoryPath = directoryPath;
    }

    public TableStorage(Relation rel, String directoryPath) {
        this.relation = rel;
        this.directoryPath = directoryPath;
    }

    // Créer un fichier .schema pour la table

    public void createSchema(Relation rel) throws IOException {
        tableName = rel.getNom_relation();

        // Préparer la liste des colonnes dans le format "nom type"
        List<String> columnNames = new ArrayList<String>();

        for (int i = 0; i < rel.getAtt().size(); i++) {
            columnNames.add(rel.getAtt().get(i).getNom_attribut() + " " +
                    rel.getAtt().get(i).getDomaine().getType_attribut() + " " +
                    rel.getAtt().get(i).getDomaine().getLimite());
        }

        File schemaFile = new File(directoryPath, tableName.toLowerCase() + ".schema");

        if (schemaFile.exists()) {
            System.out.println("La table '" + tableName.toLowerCase() + "' existe déjà.");
            return;
        }

        try (BufferedWriter schemaWriter = new BufferedWriter(new FileWriter(schemaFile))) {

            for (String string : columnNames) {
                schemaWriter.write(string);
                schemaWriter.newLine();
            }
            System.out.println("Le schéma de la table '" + tableName.toLowerCase() + "' a été créé avec succès.");
        }
    }

    // Créer un fichier .data vide pour la table
    public void createDataFile() throws IOException {
        tableName = relation.getNom_relation();

        File dataFile = new File(directoryPath, tableName.toLowerCase() + ".data");

        if (dataFile.exists()) {
            System.out.println("Le fichier de données pour la table '" + tableName.toLowerCase() + "' existe déjà.");
            return;
        }

        // Créer le fichier de données vide
        if (dataFile.createNewFile()) {
            System.out.println("Le fichier de données pour la table '" + tableName.toLowerCase() + "' a été créé.");
        }
    }

    public static ArrayList<String> extractDataTypes(String filePath) throws IOException {
        ArrayList<String> dataTypes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Lire le fichier ligne par ligne
            while ((line = reader.readLine()) != null) {
                // Diviser la ligne par les espaces
                String[] parts = line.split("\\s+");

                // Vérifier si la partie 2 (index 1) est un type de donnée
                if (parts.length > 1 && isValidDataType(parts[1])) {
                    dataTypes.add(parts[1]);
                }
            }
        }

        return dataTypes;
    }

    // Vérifie si le mot est un type valide
    private static boolean isValidDataType(String type) {
        // Ajouter les types valides ici
        return type.equalsIgnoreCase("Integer") ||
                type.equalsIgnoreCase("String") ||
                type.equalsIgnoreCase("Float") ||
                type.equalsIgnoreCase("Double");
    }

    public static ArrayList <Attribut> loadAttributs(String filePath) throws Exception {
        ArrayList <Attribut> attributs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) 
        {
            String line;

            // Lire chaque ligne du fichier
            while ((line = reader.readLine()) != null) {
                // Diviser la ligne par les espaces
                String[] parts = line.split("\\s+");


                if (parts.length == 3) {
                    String nomAttribut = parts[0];
                    String type = parts[1];
                    int taille = Integer.parseInt(parts[2]);

                    Domaine domaine = new Domaine(type, taille);
                    Attribut attribut = new Attribut(nomAttribut, domaine);

                    attributs.add(attribut);
                } else {
                    System.err.println("Ligne mal formée ignorée : " + line);
                }
            }
        }
        return attributs;
    }


    // Méthode pour lire un fichier et stocker chaque ligne comme un ArrayList d'objets
    public static List<List<Object>> readFileToObjectList(String filePath) throws IOException {
        List<List<Object>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                List<Object> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Operation.transformObject(value));
                }
                data.add(row);
            }
        }

        return data;
    }


    public static ArrayList <Nuplets> loadNuplets (String filePath) throws Exception {

        ArrayList <Nuplets> nupl = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                ArrayList <Object> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Operation.transformObject(value));
                }
                Nuplets nuplet = new Nuplets(row);
                nupl.add(nuplet);
            }
        }
        return nupl;
    }


    public static Relation getRelation (String filePath) throws Exception {

        ArrayList <Attribut> attributs = loadAttributs(filePath+".schema");
        ArrayList <Nuplets> nuplets = loadNuplets(filePath+".data");

        return (new Relation (filePath,attributs,nuplets));
    }


    public static void writeObjectListToFile(ArrayList<Nuplets> nuplets, String filePath) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            for (Nuplets nuplet : nuplets) {
                ArrayList<Object> valeurs = nuplet.getValeurs();
                // Convertir les valeurs en chaîne de caractères
                String line = String.join(",", valeurs.stream().map(Object::toString).toArray(String[]::new));
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // Ajouter des données dans le fichier .data
    public void insertData(List<String> values) throws Exception {
        tableName = relation.getNom_relation();
        File dataFile = new File(directoryPath, tableName.toLowerCase() + ".data");

        ArrayList<String> typeAttributs = extractDataTypes(directoryPath + tableName.toLowerCase() + ".schema");

        ArrayList<String> valeurs = new ArrayList<>(values);

        ArrayList<Object> valeurObject = Relation.StringToObject(valeurs, typeAttributs);

        ArrayList<Attribut> attributs = loadAttributs(directoryPath + tableName.toLowerCase() + ".schema");

        System.out.println(attributs.get(0).getDomaine().getLimite());

        Nuplets nup = new Nuplets(attributs, valeurObject);

        try (BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile, true))) {
            dataWriter.write(String.join(",", values));
            dataWriter.newLine();
            System.out.println("Les données ont été insérées dans la table '" + tableName.toLowerCase() + "'.");
        }
    }

    // Lire les données du fichier .data (pour SELECT)
    public Relation readData() throws IOException {
        try {

            tableName = relation.getNom_relation().toLowerCase();

            ArrayList<Attribut> data = new ArrayList<Attribut>();
            ArrayList<Nuplets> lines = new ArrayList<Nuplets>();

            File dataFile = new File(directoryPath, tableName + ".data");
            File schemaFile = new File(directoryPath, tableName + ".schema");

            if (!dataFile.exists()) {
                System.out.println("Aucune donnée disponible pour la table '" + tableName + "'.");
                return null;
            }

            try (BufferedReader dataReader = new BufferedReader(new FileReader(schemaFile))) {
                String line;
                while ((line = dataReader.readLine()) != null) {
                    String[] nameType = splitString(line);
                    Domaine d = new Domaine(toUpperCase(nameType[1]), Integer.parseInt(nameType[2]));
                    Attribut a = new Attribut(nameType[0], d);

                    data.add(a);

                }
            }

            try (BufferedReader dataReader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = dataReader.readLine()) != null) {
                    String[] splitLine = splitCSV(line);
                    ArrayList<Object> value = new ArrayList<Object>();
                    Object a = null;
                    for (int i = 0; i < splitLine.length; i++) {

                        if (data.get(i).getDomaine().getType_attribut().equals("Integer")) {
                            a = Integer.parseInt(splitLine[i]);
                            value.add(a);
                        } else if (data.get(i).getDomaine().getType_attribut().equals("Double")) {
                            a = Double.parseDouble(splitLine[i]);
                            value.add(a);
                        } else if (data.get(i).getDomaine().getType_attribut().equals("Float")) {
                            a = Float.parseFloat(splitLine[i]);
                            value.add(a);
                        } else if (data.get(i).getDomaine().getType_attribut().equals("String")) {
                            a = splitLine[i];
                            value.add(a);
                        }

                    }

                    Nuplets temp = new Nuplets(data, value);

                    lines.add(temp);

                }
            }

            Relation finalReation = new Relation(relation.getNom_relation(), data, lines);

            return finalReation;

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return null;
    }

    public List<String> describeTable() {
        try {
            tableName = relation.getNom_relation().toLowerCase();
            List<String> table = new ArrayList<String>();

            File schemaFile = new File(directoryPath, tableName + ".schema");

            try (BufferedReader dataReader = new BufferedReader(new FileReader(schemaFile))) {
                String line;

                table.add("\nNom de la table : " + tableName);
                while ((line = dataReader.readLine()) != null) {
                    String[] nameType = splitString(line);

                    Domaine d = new Domaine(toUpperCase(nameType[1]), Integer.parseInt(nameType[2]));
                    Attribut a = new Attribut(nameType[0], d);

                    table.add(a.getNom_attribut() + " : " + a.getDomaine().getType_attribut() + "("
                            + a.getDomaine().getLimite() + ")");
                }
            }

            return table;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String dropTable () {
        tableName = relation.getNom_relation().toLowerCase();

        File dataFile = new File(directoryPath, tableName + ".data");
        File schemaFile = new File(directoryPath, tableName + ".schema");


        if (dataFile.delete() &&  schemaFile.delete()) {
            return "\nQUERY OK , Suppression terminé";
        }

        return "\nErreur lors de la suppression de la table";
    }

    public static String[] splitString(String input) {
        return input.split("\\s+"); // Divise sur un ou plusieurs espaces
    }

    public static String[] splitCSV(String input) {
        // Supprimer les guillemets simples uniquement s'ils existent
        input = input.replace("'", "");
        // Diviser la chaîne en fonction de la virgule
        return input.split(",");
    }

    public static String toUpperCase(String input) {
        return input.toUpperCase();
    }

}
