package entity;

import entity.expression.Exp;
public class LVal{
    private String Ident;
    private Exp exp;
    private boolean isArray;
    private int identLine;

    public LVal(String ident, Exp exp, boolean isArray, int identLine) {
        this.Ident = ident;
        this.exp = exp;
        this.isArray = isArray;
        this.identLine = identLine;
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

    public int getIdentLine() {
        return identLine;
    }
}
