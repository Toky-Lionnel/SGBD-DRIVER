package function;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import relation.*;

public class Function {

    public Function() {
    }

    public static String readConf(String cheminFichier, String need) {
        try {

            String separateur = " ";
            String result = "";

            try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {

                String ligne;
                /*
                 * Exemple : PORT : 5000
                 */
                while ((ligne = br.readLine()) != null) {
                    String[] valeurs = ligne.split(separateur);

                    if (valeurs[0].equalsIgnoreCase(need)) {
                        result = valeurs[valeurs.length - 1];
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static Relation triage(Relation initial, String Attribut_name, String type) 
    {
        // type : ASC, DESC

        try {
            double temp;
            Object remplacant;

            int index = initial.getAttributs_relations().indexOf(Attribut_name);

            if (type.equalsIgnoreCase("DESC")) {
                for (int i = 0; i < initial.getNupl().size() - 1; i++) {
                    for (int j = 0; j < initial.getNupl().size() - i - 1; j++) {

                        double compare1 = Double
                                .parseDouble(initial.getNupl().get(j).getValeurs().get(index).toString());
                        double compare2 = Double
                                .parseDouble(initial.getNupl().get(j + 1).getValeurs().get(index).toString());

                        if (compare1 < compare2) {
                            temp = compare1;
                            remplacant = initial.getNupl().get(j).getValeurs().get(index);
                            compare1 = compare2;
                            initial.getNupl().get(j).getValeurs().set(index, compare2);
                            compare2 = temp;
                            initial.getNupl().get(j + 1).getValeurs().set(index, remplacant);
                        }
                    }
                }
            } else if (type.equalsIgnoreCase("ASC")) {
                for (int i = 0; i < initial.getNupl().size() - 1; i++) {
                    for (int j = 0; j < initial.getNupl().size() - i - 1; j++) {

                        double compare1 = Double
                                .parseDouble(initial.getNupl().get(j).getValeurs().get(index).toString());
                        double compare2 = Double
                                .parseDouble(initial.getNupl().get(j + 1).getValeurs().get(index).toString());

                        if (compare1 > compare2) {
                            temp = compare1;
                            remplacant = initial.getNupl().get(j).getValeurs().get(index);
                            compare1 = compare2;
                            initial.getNupl().get(j).getValeurs().set(index, compare2);
                            compare2 = temp;
                            initial.getNupl().get(j + 1).getValeurs().set(index, remplacant);
                        }
                    }
                }
            }
            return initial;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Relation getSum(Relation initial, String attribut_name) {
        Relation sum = new Relation();

        try {

            String attribute = extractAttribute(attribut_name);
            int index = initial.getAttributs_relations().indexOf(attribute);
            double somme = 0;

            for (int i = 0; i < initial.getNupl().size(); i++) {
                somme += Double.parseDouble(initial.getNupl().get(i).getValeurs().get(index).toString());
            }
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(somme);

            ArrayList<Attribut> attributes = new ArrayList<>();
            attributes.add(new Attribut(attribut_name, initial.getAtt().get(index).getDomaine()));

            ArrayList<Nuplets> nuplets = new ArrayList<>();
            nuplets.add(new Nuplets(attributes, objects));

            sum = new Relation("SUM", attributes, nuplets);

            return sum;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sum;
    }

    public static Relation getAvg(Relation initial, String attribut_name) {
        Relation avg = new Relation();

        try {

            String attribute = extractAttribute(attribut_name);
            int index = initial.getAttributs_relations().indexOf(attribute);
            double somme = 0;
            double average = 0;
            for (int i = 0; i < initial.getNupl().size(); i++) {
                somme += Double.parseDouble(initial.getNupl().get(i).getValeurs().get(index).toString());
            }

            average = somme / initial.getNupl().size();

            ArrayList<Object> objects = new ArrayList<>();
            objects.add(average);

            ArrayList<Attribut> attributes = new ArrayList<>();
            attributes.add(new Attribut(attribut_name, initial.getAtt().get(index).getDomaine()));

            ArrayList<Nuplets> nuplets = new ArrayList<>();
            nuplets.add(new Nuplets(attributes, objects));

            avg = new Relation("AVG", attributes, nuplets);

            return avg;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return avg;
    }

    public static Relation getMinOrMax (Relation initial, String attribut_name) {
        Relation minOrMax = new Relation();
        
        try {
            double temp;
            Object remplacant;
            String relation_name = "";
            double result = 0;

            String attribute = extractAttribute(attribut_name);
            int index = initial.getAttributs_relations().indexOf(attribute);

            String function = extractFunction(attribut_name);

            initial = triage(initial, attribute, "ASC");

            if (function.equalsIgnoreCase("MIN")) {
                relation_name = "MIN";
                result = Double.parseDouble(initial.getNupl().get(0).getValeurs().get(index).toString());
            }
            else if (function.equalsIgnoreCase("MAX")) {
                relation_name  = "MAX";
                result = Double.parseDouble(initial.getNupl().get(initial.getNupl().size() - 1).getValeurs().get(index).toString());
            }

            ArrayList<Object> objects = new ArrayList<>();
            objects.add(result);

            ArrayList<Attribut> attributes = new ArrayList<>();
            attributes.add(new Attribut(attribut_name, initial.getAtt().get(index).getDomaine()));

            ArrayList<Nuplets> nuplets = new ArrayList<>();
            nuplets.add(new Nuplets(attributes, objects));

            minOrMax = new Relation(relation_name, attributes, nuplets);

            return minOrMax;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return minOrMax;
    }

    // MIN(Attribute) -> Attribute
    public static String extractAttribute(String expression) {
        // Définir une expression régulière pour capturer le contenu entre parenthèses
        Pattern pattern = Pattern.compile("\\(\\s*(\\w+)\\s*\\)");
        Matcher matcher = pattern.matcher(expression);

        // Vérifier si une correspondance est trouvée
        if (matcher.find()) {
            return matcher.group(1); // Retourne le contenu entre parenthèses
        }
        return null; // Aucun attribut trouvé
    }

    // MIN(Attribute) -> MIN
    public static String extractFunction(String expression) {
        // Définir une expression régulière pour capturer le nom de la fonction
        Pattern pattern = Pattern.compile("^(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(expression);

        // Vérifier si une correspondance est trouvée
        if (matcher.find()) {
            return matcher.group(1); // Retourne le nom de la fonction
        }
        return null; // Aucune fonction trouvée
    }

}
