package entity.stmtEntity;

import entity.Stmt;
import entity.expression.LOrExp;

public class Stmt_FOR extends Stmt{
    Stmt_Assign stmtForInit;
    LOrExp cond;
    Stmt_Assign stmtForAdd;
    Stmt stmtDo;

    public Stmt_FOR(Stmt_Assign stmtForInit, LOrExp cond, Stmt_Assign stmtForAdd, Stmt stmtDo) {
        this.stmtForInit = stmtForInit;
        this.cond = cond;
        this.stmtForAdd = stmtForAdd;
        this.stmtDo = stmtDo;
    }

    public Stmt_Assign getStmtForInit() {
        return stmtForInit;
    }

    public LOrExp getCond() {
        return cond;
    }

    public Stmt_Assign getStmtForAdd() {
        return stmtForAdd;
    }

    public Stmt getStmtDo() {
        return stmtDo;
    }

    public boolean hasInit() {
        return (stmtForInit != null);
    }

    public boolean hasCond() {
        return (cond != null);
    }

    public boolean hasAdd() {
        return (stmtForAdd != null);
    }
}
