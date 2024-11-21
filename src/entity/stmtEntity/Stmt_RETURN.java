package entity.stmtEntity;

import entity.expression.Exp;
import entity.Stmt;

public class Stmt_RETURN extends Stmt {
    Exp exp;

    public Stmt_RETURN(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }
}
