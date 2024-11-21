package entity.stmtEntity;

import entity.LVal;

public class Stmt_GetChar {
    LVal lVal;

    public Stmt_GetChar(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getlVal() {
        return lVal;
    }
}
