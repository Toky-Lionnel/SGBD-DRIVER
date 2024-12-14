package relation;

import java.util.ArrayList;

public class Relation 
{
    private String nom_relation;
    private ArrayList <Attribut> att;
    private ArrayList <Nuplets> nupl;

    
    public Relation(String nom_relation, ArrayList<Attribut> att, ArrayList<Nuplets> nupl) {
        setNom_relation(nom_relation);
        setAtt(att);
        setNupl(nupl);
    }

    public Relation (String nom_relation, ArrayList<Attribut> att) {
        setNom_relation(nom_relation);
        setAtt(att);
    }

    public Relation (String nom_relation) {
        setNom_relation(nom_relation);
    }

    public Relation () {}

    public ArrayList<Nuplets> getNupl() {
        return nupl;
    }

    public void setNupl(ArrayList<Nuplets> nupl) {
        this.nupl = nupl;
    }

    public ArrayList<Attribut> getAtt() {
        return att;
    }

    public void setAtt(ArrayList<Attribut> att) {
        this.att = att;
    }

    
    public String getNom_relation() {
        return nom_relation;
    }

    public void setNom_relation(String nom_relation) {
        this.nom_relation = nom_relation;
    }


    public String afficheRelationToString() {

        if (this.nupl.size() == 0) 
        {
            return "EMPTY SET";
        }


        StringBuilder result = new StringBuilder();
    

        // Déterminer la largeur de chaque colonne
        int[] colWidths = new int[this.att.size()];
        for (int i = 0; i < this.att.size(); i++) {
            colWidths[i] = this.att.get(i).getNom_attribut().length();
        }
    
        for (int i = 0; i < this.nupl.size(); i++) {
            for (int j = 0; j < this.nupl.get(i).getValeurs().size(); j++) {
                int valueLength = this.nupl.get(i).getValeurs().get(j).toString().length();
                if (valueLength > colWidths[j]) {
                    colWidths[j] = valueLength;
                }
            }
        }
    
        // Ligne supérieure
        StringBuilder topBorder = new StringBuilder("+");
        for (int width : colWidths) {
            topBorder.append("-".repeat(width + 2)).append("+");
        }
        result.append(topBorder.toString()).append("\n");
    
        // Afficher les en-têtes
        StringBuilder header = new StringBuilder("|");
        for (int i = 0; i < this.att.size(); i++) {
            header.append(" ")
                  .append(String.format("%-" + colWidths[i] + "s", this.att.get(i).getNom_attribut()))
                  .append(" |");
        }
        result.append(header.toString()).append("\n");
    
        // Ligne de séparation
        result.append(topBorder.toString()).append("\n");
    
        // Afficher les données
        for (int i = 0; i < this.nupl.size(); i++) {
            StringBuilder row = new StringBuilder("|");
            for (int j = 0; j < this.nupl.get(i).getValeurs().size(); j++) {
                row.append(" ")
                   .append(String.format("%-" + colWidths[j] + "s", this.nupl.get(i).getValeurs().get(j).toString()))
                   .append(" |");
            }
            result.append(row.toString()).append("\n");
        }
    
        // Ligne finale
        result.append(topBorder.toString());
    
        return result.toString();
    }
    
    
    
    public void printElements() {
        //System.out.println("Relation : " + this.nom_relation);

        //System.out.println("Attributs :");
        System.out.println();
        if (this.att != null) {
            String att = "[";
            for (Attribut attr : this.att) {
                att += attr.getNom_attribut() + "  ";
            }
            att = att.substring(0, att.length() - 2);
            att += "]";
            System.out.println(att);
        } else {
            System.out.println("Relation vide");
        }

        System.out.println();
        if (this.nupl != null) {
            for (Nuplets nuplet : this.nupl) {

                System.out.println(nuplet.getValeurs());
            }
        } else {
            System.out.println("Valeurs vide");
        }

        System.out.println();
    }


    public ArrayList <String> getAttributs_relations ()
    {
        ArrayList <String> attr = new ArrayList<String>();

        for (int i=0;i<this.att.size();i++)
        {
            attr.add(this.att.get(i).getNom_attribut().toLowerCase());
        }

        return attr;
    }


    public static Object castStringToObject(String value, String targetType) throws Exception {
        switch (targetType.toUpperCase()) {
            case "INTEGER":
            case "INT":
                return Integer.valueOf(value);
            case "FLOAT":
                return Float.valueOf(value);
            case "DOUBLE":
                return Double.valueOf(value);
            case "STRING":
                return value; // Pas besoin de conversion
            default:
                throw new Exception("Type inconnu : " + targetType);
        }
    }


    public static ArrayList <Object> StringToObject (ArrayList <String> valeurs, ArrayList <String> type) throws Exception
    {
        ArrayList <Object> result = new ArrayList<Object>();

        for (int i=0;i<valeurs.size();i++)
        {
            try {
                result.add(castStringToObject(valeurs.get(i), type.get(i)));
            } catch (NumberFormatException e) {
                throw new Exception("\nLa valeur : " +valeurs.get(i)+ " n'est pas compatible avec "+type.get(i));
            }
        }   

        return result;
    }


    public ArrayList <String> getTypeAttributs (ArrayList <Attribut> attributs)
    {
        ArrayList <String> result = new ArrayList<>();

        for (int i=0;i<attributs.size();i++)
        {
            result.add(attributs.get(i).getDomaine().getType_attribut());
        }

        return result;
    }

    
}
