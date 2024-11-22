package entity.funcdef;

import entity.BType;

public class FuncFParam {
    BType bType;
    String Ident;

    public FuncFParam(BType bType, String ident) {
        this.bType = bType;
        Ident = ident;
    }

    public BType getbType() {
        return bType;
    }

    public String getIdent() {
        return Ident;
    }
}
