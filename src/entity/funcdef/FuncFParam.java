package entity.funcdef;

import entity.BType;

public class FuncFParam {
    BType bType;
    String Ident;
    boolean isArray;

    public FuncFParam(BType bType, String ident, boolean isArray) {
        this.bType = bType;
        Ident = ident;
        this.isArray = isArray;
    }

    public BType getbType() {
        return bType;
    }

    public String getIdent() {
        return Ident;
    }

    public boolean isArray() {
        return isArray;
    }
}
