package relation;

import java.util.*;


public class Domaine 
{
    private String type_attribut;
    private int limite;


    public Domaine(String type_attribut, int limite) throws Exception
    {
        setType_attribut(type_attribut);
        setLimite(limite);
    }

    public Domaine () {}


    public int getLimite() {
        return limite;
    }


    public void setLimite(int limite) {
        this.limite = limite;
    }


    public String getType_attribut() {
        return type_attribut;
    }


    public void setType_attribut(String type_attribut) throws Exception 
    {

        if (type_attribut.equalsIgnoreCase((String) "INT") || type_attribut.equalsIgnoreCase((String) "INTEGER"))
        {
            this.type_attribut = "Integer";
        } else if ( type_attribut.equalsIgnoreCase((String) "DOUBLE") )
        {
            this.type_attribut = "Double";
        } else if (type_attribut.equalsIgnoreCase((String) "STRING"))
        {
            this.type_attribut = "String";
        } else if (type_attribut.equalsIgnoreCase((String) "FLOAT"))
        {
            this.type_attribut = "Float";
        }  else {
            throw new Exception("ERROR : Type attribut non valide : " + type_attribut);
        }
    }


}