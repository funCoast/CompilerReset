package entity.stmtEntity;

import entity.expression.Exp;
import entity.LVal;
import entity.Stmt;

public class Stmt_Assign extends Stmt {
    LVal lVal;
    Exp exp;

    public Stmt_Assign(LVal lVal, Exp exp) {
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
