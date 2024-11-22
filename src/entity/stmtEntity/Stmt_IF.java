package entity.stmtEntity;

import entity.Stmt;
import entity.expression.LOrExp;

public class Stmt_IF extends Stmt {
    LOrExp cond;
    Stmt stmtIf;
    Stmt stmtElse;

    public Stmt_IF(LOrExp cond, Stmt stmtIf, Stmt stmtElse) {
        this.cond = cond;
        this.stmtIf = stmtIf;
        this.stmtElse = stmtElse;
    }

    public LOrExp getCond() {
        return cond;
    }

    public Stmt getStmtIf() {
        return stmtIf;
    }

    public Stmt getStmtElse() {
        return stmtElse;
    }

    public boolean hasElse() {
        return (stmtElse != null);
    }
}
