package entity.decl.con;

import entity.expression.Exp;

import java.util.ArrayList;

public class ConstDef {
    String Ident;
    boolean isArray;
    ArrayList<Exp> expArrayList;
    ConstInitVal constInitVal;

    public ConstDef(String ident, boolean isArray,
                    ArrayList<Exp> expArrayList,
                    ConstInitVal constInitVal) {
        Ident = ident;
        this.isArray = isArray;
        this.expArrayList = expArrayList;
        this.constInitVal = constInitVal;
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

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }
}
