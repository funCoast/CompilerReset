package entity.decl.con;

import entity.expression.Exp;

import java.util.ArrayList;

public class ConstDef {
    String Ident;
    boolean isArray;
    Exp constExp;
    ConstInitVal constInitVal;

    public ConstDef(String ident, boolean isArray, Exp constExp, ConstInitVal constInitVal) {
        Ident = ident;
        this.isArray = isArray;
        this.constExp = constExp;
        this.constInitVal = constInitVal;
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
}
