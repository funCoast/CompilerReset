package entity.stmtEntity;

import entity.expression.Exp;
import entity.Stmt;

public class StmtExp extends Stmt {
    Exp exp;

    public StmtExp(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }
}
