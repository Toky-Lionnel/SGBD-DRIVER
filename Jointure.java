package operation;

import java.util.ArrayList;
import relation.*;

public class Jointure {
    
    public static Relation produitCartesien(Relation relation1, Relation relation2) throws Exception {
        
        // Création de la liste des attributs combinés
        ArrayList<Attribut> attributs = new ArrayList<>();

        String temp = "";

        // Modifier les attributs de relation1
        for (int i = 0; i < relation1.getAtt().size(); i++) {
            String originalName = relation1.getAtt().get(i).getNom_attribut(); // Conservez le nom original
            temp = relation1.getNom_relation() + "." + originalName;
            relation1.getAtt().get(i).setNom_attribut(temp);
        }
        
        temp = "";  // Réinitialiser temp
        
        // Modifier les attributs de relation2
        for (int j = 0; j < relation2.getAtt().size(); j++) {
            String originalName = relation2.getAtt().get(j).getNom_attribut(); // Conservez le nom original
            temp = relation2.getNom_relation() + "." + originalName;
            relation2.getAtt().get(j).setNom_attribut(temp);
        }
        

        attributs.addAll(relation1.getAtt());
        attributs.addAll(relation2.getAtt());

        // Liste pour les nuplets du produit cartésien
        ArrayList<Nuplets> nuplets = new ArrayList<>();
    
        // Construction des nuplets avec des boucles for normales
        for (int i = 0; i < relation1.getNupl().size(); i++) 
        {
            Nuplets nuplet1 = relation1.getNupl().get(i);
            for (int j = 0; j < relation2.getNupl().size(); j++) 
            {
                Nuplets nuplet2 = relation2.getNupl().get(j);
                
                // Création d'une liste de valeurs combinées
                ArrayList<Object> valeurs = new ArrayList<>();
                valeurs.addAll(nuplet1.getValeurs());
                valeurs.addAll(nuplet2.getValeurs());
                
                // Ajout du nouveau nuplet
                nuplets.add(new Nuplets(attributs, valeurs));
            }
        }

    
        // Retourner la relation sans suppression de doublons
        return new Relation("Produit cartésien", attributs, nuplets);
    }


    public static Relation tetaJointure (Relation relation1, Relation relation2, ArrayList <String> attributs, String comparateur) throws Exception
    {
        Relation produit = produitCartesien(relation1, relation2);

        int index1 = produit.getAttributs_relations().indexOf(attributs.get(0));
        int index2 = produit.getAttributs_relations().indexOf(attributs.get(1));

        ArrayList <Object> valeur1 = getValeurAttribut(index1, produit);
        ArrayList <Object> valeur2 = getValeurAttribut(index2, produit);

        ArrayList <Nuplets> result = new ArrayList<Nuplets>();

        for (int i=0;i<valeur1.size();i++)
        {
            if (checkValeurTetaJointure(valeur1.get(i), valeur2.get(i), comparateur))
            {
                result.add(produit.getNupl().get(i));
            }
        }

        return new Relation("Teta Jointure", produit.getAtt(), Nuplets.removeDoublon(result));
    }



    public static boolean checkValeurTetaJointure(Object obj1, Object obj2, String comparateur) {
        // Si les objets sont des nombres (Integer, Double, etc.)
        if (obj1 instanceof Number && obj2 instanceof Number) {
            double val1 = ((Number) obj1).doubleValue();
            double val2 = ((Number) obj2).doubleValue();
    
            switch (comparateur) {
                case "=":
                    return val1 == val2;
                case "!=":
                    return val1 != val2;
                case "<=":
                    return val1 <= val2;
                case ">=":
                    return val1 >= val2;
                case ">":
                    return val1 > val2;
                case "<":
                    return val1 < val2;
                default:
                    return false;
            }
        } 
        // Pour les autres objets (par exemple, des chaînes ou autres objets comparables)
        else {
            switch (comparateur) {
                case "=":
                    return obj1.equals(obj2);
                case "!=":
                    return !obj1.equals(obj2);
                default:
                    return false;
            }
        }
    }
    

    public static Relation jointure (Relation relation1, Relation relation2) throws Exception
    {
        ArrayList <String> attributCommun = getAttributCommun(relation1, relation2);  
        Relation produitCart = produitCartesien(relation1, relation2);
        String cartesienne = produitCart.afficheRelationToString();

        //System.out.println("cart \n" + cartesienne);

        ArrayList <Integer> nuplets = new ArrayList<Integer>();
        
        for (int j = 0; j < attributCommun.size(); j++)
        {
            int [] indexes = new int[2];  
            int index = 0;
 
            for (int i = 0; i < produitCart.getAttributs_relations().size(); i++)
            {
                String name1 = relation1.getNom_relation() + "." + attributCommun.get(j);
                String name2 = relation2.getNom_relation() + "." + attributCommun.get(j);
                if (produitCart.getAttributs_relations().get(i).equals(name1) ||
                    produitCart.getAttributs_relations().get(i).equals(name2))
                {
                    indexes [index] = i;
                    index++;
                }
            }

            ArrayList <Object> valeur1 = getValeurAttribut(indexes[0],produitCart);
            ArrayList <Object> valeur2 = getValeurAttribut(indexes[1],produitCart);

            for (int k=0;k<valeur1.size();k++)
            {
                if (checkValeur(valeur1.get(k), valeur2.get(k)))
                {
                    nuplets.add(k);
                }
            }
        }

        ArrayList <Integer> indice_result = countOccurence(nuplets, attributCommun.size());
        ArrayList <Nuplets> result = new ArrayList<Nuplets>();

        for (int i=0;i<indice_result.size();i++)
        {
            result.add(produitCart.getNupl().get(indice_result.get(i)));   
        }

        return new Relation("Jointure", produitCart.getAtt(), Nuplets.removeDoublon(result));    
    }



    public static boolean checkValeur (Object obj1, Object obj2)
    {
        if (obj1.getClass().getSimpleName().equals("Integer") || obj1.getClass().getSimpleName().equals("Double"))
        {
            if ((int) obj1 != (int) obj2)
            {
                return false;
            }
        } else {
            if (!obj1.equals((String)obj2))
            {
                return false;
            }
        }
        return true;
    }



    public static ArrayList <String> getAttributCommun (Relation rel1, Relation rel2)
    {
        ArrayList <String> att_rel1 = rel1.getAttributs_relations();
        ArrayList <String> rm1Prefix = removePrefix(att_rel1, rel1.getNom_relation());

        ArrayList <String> att_rel2 = rel2.getAttributs_relations();
        ArrayList <String> rm2Prefix = removePrefix(att_rel2, rel2.getNom_relation());

        ArrayList <String> resultat = new ArrayList<String>();

        for (int i = 0; i < rm2Prefix.size(); i++)
        {
            
            if (rm1Prefix.contains(rm2Prefix.get(i)))
            {
                resultat.add(rel2.getAttributs_relations().get(i));
            }

        }
        return resultat;
    } 

    public static ArrayList <String> removePrefix (ArrayList<String> NameList, String relation_name)
    {
        ArrayList <String> resultat = new ArrayList<String>();
        for (String name : NameList)
        {
            resultat.add(removeRelationPrefix(name, relation_name));
        }
        return resultat;
    } 

    public static String removeRelationPrefix(String input,String relation_name) {
        if (input == null) {
            return null; // Gestion des entrées null
        }
        String prefix = relation_name + ".";
        if (input.startsWith(prefix)) {
            return input.substring(prefix.length()); // Supprime "nom_de_relation."
        }
        return input; // Retourne la chaîne inchangée si le préfixe n'est pas présent
    }


    public static ArrayList <Object> getValeurAttribut (int index, Relation rel)
    {
        ArrayList <Object> valeur = new ArrayList<Object>();
        for (int i=0;i<rel.getNupl().size();i++)
        {
            valeur.add(rel.getNupl().get(i).getValeurs().get(index));
        }

        return valeur;
    }



    public static ArrayList <Integer> countOccurence (ArrayList <Integer> nombres, int max)
    {
        ArrayList <Integer> resultats = new ArrayList<Integer>();

        for (int i=0;i<nombres.size();i++)
        {
            int count = 0;

            if (!resultats.contains(nombres.get(i)))
            {
                for (int j=0;j<nombres.size();j++)
                {        
                    if (nombres.get(j) == nombres.get(i))
                    {
                        count++;
                    }
                }
                if (count == max && !resultats.contains(nombres.get(i)))
                {
                    resultats.add(nombres.get(i));
                }
            }
        }
        return resultats;
    }


}
