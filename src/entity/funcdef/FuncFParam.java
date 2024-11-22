package entity.funcdef;

import entity.BType;

public class FuncFParam {
    private BType bType;
    private String Ident;
    private int identLine;

    public FuncFParam(BType bType, String ident, int identLine) {
        this.bType = bType;
        Ident = ident;
        this.identLine = identLine;
    }

    public BType getbType() {
        return bType;
    }

    public String getIdent() {
        return Ident;
    }

    public int getIdentLine() {
        return identLine;
    }
}
