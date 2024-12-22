package relation;

import java.util.*;

public class Nuplets 
{
    private ArrayList <Attribut> attributs;
    private ArrayList <Object> valeurs;

    public Nuplets (ArrayList <Object> valeurs)
    {
        setValeurs(valeurs);
    }

    public Nuplets(ArrayList<Attribut> attributs, ArrayList<Object> valeurs) throws Exception
    {
        if (verificationValeurs(attributs, valeurs))
        {
            setAttributs(attributs);
            setValeurs(valeurs);
        }
    }

    
    public static ArrayList<Nuplets> removeDoublon(ArrayList<Nuplets> nuplets) 
    {
        ArrayList<Nuplets> result = new ArrayList<>();

        for (Nuplets nuplet : nuplets) 
        {
            if (!contient(result, nuplet)) 
            {
                result.add(nuplet);
            }
        }
        return result;
    }
    

    public static ArrayList <Nuplets> suppression (ArrayList <Nuplets> nuplets, Nuplets n)
    {
        ArrayList <Nuplets> result = new ArrayList<Nuplets>();

        for (int i=0;i<nuplets.size();i++)
        {
            if (!nuplets.get(i).estEgal(n) && !contient(result, nuplets.get(i)))
            {
                result.add(nuplets.get(i));
            }
        }
        return result;
    }


    public static boolean contient (ArrayList <Nuplets> nupls , Nuplets nupl)
    {
        for (int i=0;i<nupls.size();i++)
        {
            if (nupls.get(i).estEgal(nupl))
            {
                return true;
            }
        }
        return false;
    }

    
    public boolean estEgal (Nuplets nupl)
    {
        ArrayList <Object> nupl1 = this.getValeurs();
        ArrayList <Object> nupl2 = nupl.getValeurs();

        for (int i=0;i<nupl1.size();i++)
        {
            if (!nupl1.get(i).equals(nupl2.get(i)))
            {
                return false;
            }
        }
        return true;
    }


    public ArrayList<Attribut> getAttributs() {
        return attributs;
    }

    
    public void setAttributs(ArrayList<Attribut> attributs) {
        this.attributs = attributs;
    }


    public ArrayList<Object> getValeurs() {
        return valeurs;
    }
    

    public void setValeurs(ArrayList<Object> valeurs) {
        this.valeurs = valeurs;
    }


    public boolean verificationValeurs (ArrayList <Attribut> at , ArrayList <Object> val) throws Exception
    {
        if (at.size()!=val.size())
        {
            throw new Exception ("ERROR : Le nombre d'attribut et le nombre de données ne correspondent pas \n" +
            " Le nombre d'attribut "+at.size()+ " Le nombre de valeur " +val.size());
        }

        for (int i=0;i<at.size();i++)
        {
            verification(at.get(i), val.get(i));
        }
        return true;
    }


  
    public boolean verifNombre (double nombre,int limite) throws Exception
    {
        if (nombre > limite && limite != 0)
        {
            throw new Exception("ERROR : Le nombre entré est trop grand " + nombre + " limite : " + limite );
        }
        return true;
    }


    public boolean verifString (String caract, int limite) throws Exception
    {
        if (caract.length() > limite && limite != 0)
        {
            throw new Exception ("ERROR : Chaine de caractère trop long " + caract);
        }
        return true;
    }



    public boolean verification(Attribut t, Object o) throws Exception 
    {
        Domaine att = t.getDomaine();
    
        // Vérification du type d'attribut.
        String typeAttribut = att.getType_attribut();
        int limites = att.getLimite();
    
        boolean result = false;
        
            if ("String".equalsIgnoreCase(typeAttribut))  {
                // Vérification pour les chaînes de caractères.
                result = verifString((String) o, limites) ;
            } else if ("Integer".equalsIgnoreCase(typeAttribut) || "Double".equalsIgnoreCase(typeAttribut)) {
                // Vérification pour les nombres.
                if (o instanceof Integer) 
                {
                    return verifNombre((int) o, limites) ;
                } else if (o instanceof Double) 
                {
                    return verifNombre((double) o, limites) ;
                }
            }
        return result;
    }





}
