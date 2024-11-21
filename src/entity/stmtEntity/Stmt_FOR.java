package entity.stmtEntity;

import entity.Stmt;
import entity.expression.LOrExp;

public class Stmt_FOR extends Stmt{
    StmtAssign stmtForInit;
    LOrExp cond;
    StmtAssign stmtForDo;

    public Stmt_FOR(StmtAssign stmtForInit, LOrExp cond, StmtAssign stmtForDo) {
        this.stmtForInit = stmtForInit;
        this.cond = cond;
        this.stmtForDo = stmtForDo;
    }

    public StmtAssign getStmtForInit() {
        return stmtForInit;
    }

    public LOrExp getCond() {
        return cond;
    }

    public StmtAssign getStmtForDo() {
        return stmtForDo;
    }
}
