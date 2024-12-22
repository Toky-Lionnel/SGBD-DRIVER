package relation;


import java.util.*;
import operation.*;


public class Update { 
    
    public static Relation updateTable (Relation relation , String condition, String update) throws Exception
    {
        String [] requete = Operation.diviseRequete(condition);
        String [] conditionUpdate = Operation.diviseRequete(update);


        int index = -1;
        int updateIndex = -1;

        index = relation.getAttributs_relations().indexOf(requete[0].toLowerCase());

        updateIndex = relation.getAttributs_relations().indexOf(conditionUpdate[0].toLowerCase());


        if (index == -1 || updateIndex == -1) {
            throw new Exception ("Attribut non valide : " +requete[0]);
        }

        // requete[0] -> attribut  // requete[1] -> operateur // requete [2] -> valeur
        
        Object valeur = Operation.transformObject(requete[2]);

        Object updateValeur = Operation.transformObject(conditionUpdate[2]);

        String comparateur = requete[1];

        
        for (int i=0;i<relation.getNupl().size();i++)
        {
            if (valeur.getClass().getSimpleName().equals("Integer") || valeur.getClass().getSimpleName().equals("Double"))
            {
                // valeur à comparer 
                double val = 0;
                if (valeur.getClass().getSimpleName().equals("Integer"))  {
                    val = (int) valeur;
                }

                else if (valeur.getClass().getSimpleName().equals("Double"))  {
                    val = (double) valeur;
                }

                // valeur anatin'ilay table
                double value = 0;
                if (relation.getNupl().get(i).getValeurs().get(index).getClass().getSimpleName().equals("Integer"))
                {
                    value = (int) relation.getNupl().get(i).getValeurs().get(index);
                }

                else if (relation.getNupl().get(i).getValeurs().get(index).getClass().getSimpleName().equals("Double"))
                {
                    value = (double) relation.getNupl().get(i).getValeurs().get(index);
                }

                if (comparateur.equals("=") )
                {
                    if (relation.getNupl().get(i).getValeurs().get(index) == valeur) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);

                    }
                } else if (comparateur.equals("<") ) {
                    if (value < val) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                } else if (comparateur.equals("<=")) {
                    if (value <= val) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                } else if (comparateur.equals(">=")) {
                    if (value >= val) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                } else if (comparateur.equals(">") ) {
                    if (value > val) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                } else if (comparateur.equals("!=") ) {
                    if (value != val) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                }
            }
            else {
                if (comparateur.equals("="))
                {
                    if (relation.getNupl().get(i).getValeurs().get(index).equals(valeur)) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                } else if (comparateur.equals("!=") ) {
                    if (!relation.getNupl().get(i).getValeurs().get(index).equals(valeur)) {
                        relation.getNupl().get(i).getValeurs().set(updateIndex, updateValeur);
                    }
                }
            }
        }

        return new Relation("Selection",relation.getAtt(),Nuplets.removeDoublon(relation.getNupl()));
    }


    public static Relation deleteTable (Relation relation, String condition) throws Exception
    {
        if (condition == "") {
            return new Relation(relation.getNom_relation(),relation.getAtt(),new ArrayList<Nuplets>());
        }


        String [] requete = Operation.diviseRequete(condition);

        int index = -1;

        index = relation.getAttributs_relations().indexOf(requete[0].toLowerCase());

        if (index == -1) {
            throw new Exception ("Attribut non valide : " +requete[0]);
        }

        Object valeur = Operation.transformObject(requete[2]);

        String comparateur = requete[1];
        Iterator<Nuplets> iterator = relation.getNupl().iterator();

        while (iterator.hasNext()) {
            Nuplets currentNupl = iterator.next();
            Object tableValue = currentNupl.getValeurs().get(index);

            boolean conditionMet = false;

            if (valeur instanceof Number && tableValue instanceof Number) {
                // Comparaison pour les valeurs numériques
                double val = ((Number) valeur).doubleValue();
                double value = ((Number) tableValue).doubleValue();

                if (comparateur.equals("=")) {
                    conditionMet = (value == val);
                } else if (comparateur.equals("!=")) {
                    conditionMet = (value != val);
                } else if (comparateur.equals("<")) {
                    conditionMet = (value < val);
                } else if (comparateur.equals("<=")) {
                    conditionMet = (value <= val);
                } else if (comparateur.equals(">=")) {
                    conditionMet = (value >= val);
                } else if (comparateur.equals(">")) {
                    conditionMet = (value > val);
                }
            } else {
                // Comparaison pour les chaînes de caractères ou autres objets
                if (comparateur.equals("=")) {
                    conditionMet = tableValue.equals(valeur);
                } else if (comparateur.equals("!=")) {
                    conditionMet = !tableValue.equals(valeur);
                }
            }

            // Suppression si la condition est remplie
            if (conditionMet) {
                iterator.remove();
            }
        }

        return new Relation(relation.getNom_relation(),relation.getAtt(),relation.getNupl());
    }

}


