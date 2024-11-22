package entity;

import entity.expression.Exp;
public class LVal{
    String Ident;
    Exp exp;
    boolean isArray;

    public LVal(String ident, Exp exp, boolean isArray) {
        this.Ident = ident;
        this.exp = exp;
        this.isArray = isArray;
    }

    public String getIdent() {
        return Ident;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean isArray() {
        return isArray;
    }
}
