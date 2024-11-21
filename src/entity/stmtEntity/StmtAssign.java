package entity.stmtEntity;

import entity.expression.Exp;
import entity.LVal;
import entity.Stmt;

public class StmtAssign extends Stmt {
    LVal lVal;
    Exp exp;

    public StmtAssign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }
}
