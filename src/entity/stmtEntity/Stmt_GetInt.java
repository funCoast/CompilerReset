package entity.stmtEntity;

import entity.LVal;
import entity.Stmt;

public class Stmt_GetInt extends Stmt {
    LVal lVal;

    public Stmt_GetInt(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getlVal() {
        return lVal;
    }
}
