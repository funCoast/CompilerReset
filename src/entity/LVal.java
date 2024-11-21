package entity;

import entity.expression.Exp;
public class LVal {
    String Ident;
    Exp exp;

    public LVal(String ident, Exp exp) {
        Ident = ident;
        this.exp = exp;
    }

    public String getIdent() {
        return Ident;
    }

    public Exp getExp() {
        return exp;
    }
}
