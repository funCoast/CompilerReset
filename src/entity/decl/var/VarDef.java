package entity.decl.var;

import entity.expression.Exp;

import java.util.ArrayList;

public class VarDef {
    String Ident;
    boolean isArray;
    ArrayList<Exp> expArrayList;
    InitVal initVal;

    public VarDef(String ident, boolean isArray, ArrayList<Exp> expArrayList) {
        Ident = ident;
        this.isArray = isArray;
        this.expArrayList = expArrayList;
    }

    public VarDef(String ident, boolean isArray, ArrayList<Exp> expArrayList, InitVal initVal) {
        Ident = ident;
        this.isArray = isArray;
        this.expArrayList = expArrayList;
        this.initVal = initVal;
    }

    public String getIdent() {
        return Ident;
    }

    public boolean isArray() {
        return isArray;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }

    public InitVal getInitVal() {
        return initVal;
    }
}
