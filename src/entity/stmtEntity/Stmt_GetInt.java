package entity.stmtEntity;

import entity.LVal;

public class Stmt_GetInt {
    LVal lVal;

    public Stmt_GetInt(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getlVal() {
        return lVal;
    }
}
