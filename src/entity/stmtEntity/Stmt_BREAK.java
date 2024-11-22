package entity.stmtEntity;

import entity.Stmt;

public class Stmt_BREAK extends Stmt {
    private int breakLine;

    public Stmt_BREAK(int breakLine) {
        this.breakLine = breakLine;
    }

    public int getBreakLine() {
        return breakLine;
    }
}
