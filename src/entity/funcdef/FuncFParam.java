package entity.funcdef;

import entity.BType;

public class FuncFParam {
    private BType bType;
    private String Ident;
    private int identLine;
    private boolean isArray;

    public FuncFParam(BType bType, String ident, int identLine, boolean isArray) {
        this.bType = bType;
        Ident = ident;
        this.identLine = identLine;
        this.isArray = isArray;
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

    public boolean isArray() {
        return isArray;
    }
}
