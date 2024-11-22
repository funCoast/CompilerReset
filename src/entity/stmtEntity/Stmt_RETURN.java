package entity.stmtEntity;

import entity.expression.Exp;
import entity.Stmt;

public class Stmt_RETURN extends Stmt {
    Exp exp;
    int returnLine;

    public Stmt_RETURN(Exp exp, int returnLine) {
        this.exp = exp;
        this.returnLine = returnLine;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean hasReturnValue() {
        return (exp != null);
    }

    public int getReturnLine() {
        return returnLine;
    }
}
