package relation;

import java.util.*;

public class Attribut {
    
    private String nom_attribut;
    private Domaine domaine;

    
    public Attribut(String nom_attribut, Domaine domaine) {
        setDomaine(domaine);
        setNom_attribut(nom_attribut);
    }

    public Attribut () { }

    public Domaine getDomaine() {
        return domaine;
    }

    public void setDomaine(Domaine domaine) {
        this.domaine = domaine;
    }

    public String getNom_attribut() {
        return nom_attribut;
    }

    public void setNom_attribut(String nom_attribut) {
        this.nom_attribut = nom_attribut;
    }


}
