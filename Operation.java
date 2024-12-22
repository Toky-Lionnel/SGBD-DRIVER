package operation;

import java.util.*;
import relation.*;


public class Operation 
{
    public Operation () {}

    public static Relation projection (ArrayList <String> att, Relation rel) throws Exception
    {
        int [] indexes = new int[att.size()];

        for (int i = 0; i < indexes.length; i++)
        {
            indexes[i] = -1;
        }

        String [] attributs = att.toArray(new String[att.size()]);
        ArrayList <String> att_relation = rel.getAttributs_relations();
        ArrayList <Attribut> relation_attribut = new ArrayList<Attribut>();
        
        for (int i=0;i<attributs.length;i++)
        {
            indexes[i] = att_relation.indexOf(attributs[i].toLowerCase());
            
            relation_attribut.add(rel.getAtt().get(indexes[i]));

            if (indexes[i] == -1)
            {
                throw new Exception ("Attribut "+attributs[i]+ " non valide"); 
            }
        }

        // Les nuplets dans la relation 
        ArrayList <Nuplets> nupl_projete = new ArrayList<Nuplets>();

        for (int i=0;i<rel.getNupl().size();i++)
        {
            ArrayList <Object> valeurs = new ArrayList<Object>();

            for (int j=0;j<attributs.length;j++)
            {
                valeurs.add(rel.getNupl().get(i).getValeurs().get(indexes[j]));
            }

            nupl_projete.add(new Nuplets(relation_attribut, valeurs));
        }

        return new Relation("Projection", relation_attribut, Nuplets.removeDoublon(nupl_projete));
    }


    public static Relation selection (String attribut, Object valeur, String comparateur,Relation rel) throws Exception
    {
        // index de l'attribut à trouver 
        ArrayList <String> attributs = rel.getAttributs_relations();

        int index = attributs.indexOf(attribut.toLowerCase());

        if (index == -1)
        {
            throw new Exception("Attribut non valide : " +attribut.toUpperCase() + "\n" +attributs);
        }

        // nuplets qui satisfont les conditions 
        ArrayList <Nuplets> result = new ArrayList<Nuplets>();

        for (int i = 0; i < rel.getNupl().size(); i++)
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
                if (rel.getNupl().get(i).getValeurs().get(index).getClass().getSimpleName().equals("Integer"))
                {
                    value = (int) rel.getNupl().get(i).getValeurs().get(index);
                }

                else if (rel.getNupl().get(i).getValeurs().get(index).getClass().getSimpleName().equals("Double"))
                {
                    value = (double) rel.getNupl().get(i).getValeurs().get(index);
                }

                if (comparateur.equals("=") )
                {
                    if (rel.getNupl().get(i).getValeurs().get(index) == valeur)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals("<") ) {
                    if (value < val)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals("<=")) {
                    if (value <= val)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals(">=")) {
                    if (value >= val)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals(">") ) {
                    if (value > val)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals("!=") ) {
                    if (value != val)
                    {
                        result.add(rel.getNupl().get(i));
                    }
                }
            }
            else {
                if (comparateur.equals("="))
                {
                    if (rel.getNupl().get(i).getValeurs().get(index).equals(valeur))
                    {
                        result.add(rel.getNupl().get(i));
                    }
                } else if (comparateur.equals("!=") ) {
                    if (!rel.getNupl().get(i).getValeurs().get(index).equals(valeur))
                    {
                        result.add(rel.getNupl().get(i));
                    }
                }
            }
        }

        return new Relation("Selection",rel.getAtt(),Nuplets.removeDoublon(result));
    }


    public static Relation selectionMultiple (String requete, Relation rel) throws Exception
    {
        String [] allRequete = transformeRequete(requete);

        Relation resultat = new Relation("Resultat", null, null);
        resultat = rel;
        
        for (int i=0;i<allRequete.length;i++)
        {
            String [] requetediv = diviseRequete(allRequete[i]);
            resultat = selection(requetediv[0], transformObject(requetediv[2]), requetediv[1], resultat);
        }

        return resultat;
    }


    public static String [] transformeRequete (String requete)
    {
        String[] conditions = requete.split("\\) \\(");
            
        List<String> resultats = new ArrayList<>();
        for (String condition : conditions) {
            resultats.add(condition.replace("(", "").replace(")", "").trim());
        }
        
        return resultats.toArray(new String[resultats.size()]);
    }


    public static Object transformObject(String valeur) {

        if (valeur != null) {
            valeur = valeur.trim().replace("'", "").replace("\"", "");
        }
        
        try {
            return Integer.valueOf(valeur);
        } catch (NumberFormatException e) {
        }
        
        try {
            return Double.valueOf(valeur);
        } catch (NumberFormatException e) {
        }
        
        return valeur;
    }
    

    public static String [] diviseRequete (String requete)
    {
        String[] operators = {"!=", "<=", ">=", "=", "<", ">"};
        String operator = null;

        for (String op : operators) {
            if (requete.contains(op)) {
                operator = op;
                break;
            }
        }

        if (operator == null) {
            System.out.println("Aucun opérateur trouvé dans la condition.");
            return null;
        }

        int operatorIndex = requete.indexOf(operator);
        String attribut = requete.substring(0, operatorIndex).trim(); 
        String valeur = requete.substring(operatorIndex + operator.length()).trim(); 


        String [] resultats = {attribut, operator, valeur};
        return resultats;
    }



    // format d'un UPDATE : UPDATE table_name SET valeur = 5 WHERE colonne = 6
    public static Relation updateValeur (String requete)
    {


        return null;
    }




    
}
