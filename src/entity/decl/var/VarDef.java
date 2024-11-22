package entity.decl.var;

import entity.expression.Exp;

import java.util.ArrayList;

public class VarDef {
    String Ident;
    boolean isArray;
    Exp constExp;
    InitVal initVal;

    public VarDef(String ident, boolean isArray, Exp constExp, InitVal initVal) {
        Ident = ident;
        this.isArray = isArray;
        this.constExp = constExp;
        this.initVal = initVal;
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
}
