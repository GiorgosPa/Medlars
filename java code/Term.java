/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medlars_collection;

import java.util.ArrayList;

/**
 *
 * @author Giorgos
 */
public class Term {
    private String def;
    private ArrayList<String> synonym;
    private String is_a;

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getIs_a() {
        return is_a;
    }

    public void setIs_a(String is_a) {
        this.is_a = is_a;
    }

    public ArrayList<String> getSynonym() {
        return synonym;
    }

    public void setSynonym(String synonym) {
        this.synonym.add(synonym);
    }

    public Term(String def, ArrayList<String> synonym, String is_a) {
        this.def = def;
        this.synonym = synonym;
        this.is_a = is_a;
    }

    @Override
    public String toString() {
        return "Term{" + "def=" + def + ", synonym=" + synonym + ", is_a=" + is_a + '}';
    }
    
    
    
}
