package entity.decl.con;

import entity.expression.Exp;

import java.util.ArrayList;

public class ConstDef {
    private String Ident;
    private boolean isArray;
    private Exp constExp;
    private ConstInitVal constInitVal;
    private int identLine;


    public ConstDef(String ident, boolean isArray, Exp constExp, ConstInitVal constInitVal, int identLine) {
        this.Ident = ident;
        this.isArray = isArray;
        this.constExp = constExp;
        this.constInitVal = constInitVal;
        this.identLine = identLine;

    }

    public String getIdent() {
        return Ident;
    }

    public boolean isArray() {
        return isArray;
    }

    public Exp getConstExp() {
        return constExp;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }

    public int getIdentLine() {
        return identLine;
    }
}
