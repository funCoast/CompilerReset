package entity.decl.var;

import entity.expression.Exp;

import java.util.ArrayList;

public class VarDef {
    private String Ident;
    private boolean isArray;
    private Exp constExp;
    private InitVal initVal;
    private int identLine;


    public VarDef(String ident, boolean isArray, Exp constExp, InitVal initVal, int identLine) {
        Ident = ident;
        this.isArray = isArray;
        this.constExp = constExp;
        this.initVal = initVal;
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

    public InitVal getInitVal() {
        return initVal;
    }

    public int getIdentLine() {
        return identLine;
    }
}
