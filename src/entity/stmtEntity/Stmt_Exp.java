package entity.stmtEntity;

import entity.expression.Exp;
import entity.Stmt;

public class Stmt_Exp extends Stmt {
    Exp exp;
    boolean hasExp;

    public Stmt_Exp(Exp exp, boolean hasExp) {
        this.exp = exp;
        this.hasExp = hasExp;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean isHasExp() {
        return hasExp;
    }
}
