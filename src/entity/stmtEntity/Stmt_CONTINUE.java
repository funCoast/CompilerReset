package entity.stmtEntity;

import entity.Stmt;

public class Stmt_CONTINUE extends Stmt {
    private int continueLine;

    public Stmt_CONTINUE(int continueLine) {
        this.continueLine = continueLine;
    }

    public int getContinueLine() {
        return continueLine;
    }
}
