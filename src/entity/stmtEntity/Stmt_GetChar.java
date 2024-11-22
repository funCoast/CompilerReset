package entity.stmtEntity;

import entity.LVal;
import entity.Stmt;

public class Stmt_GetChar extends Stmt {
    LVal lVal;

    public Stmt_GetChar(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getlVal() {
        return lVal;
    }
}
