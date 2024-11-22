package entity.stmtEntity;

import entity.Stmt;
import entity.expression.Exp;

import java.util.ArrayList;

public class Stmt_Printf extends Stmt {
    private int printfLine;
    private String stringConst;
    private ArrayList<Exp> expArrayList;

    public Stmt_Printf(String stringConst, ArrayList<Exp> expArrayList, int printfLine) {
        this.stringConst = stringConst;
        this.expArrayList = expArrayList;
        this.printfLine = printfLine;
    }

    public String getStringConst() {
        return stringConst;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }

    public int getPrintfLine() {
        return printfLine;
    }

    public boolean hasDescribe() {
        return !expArrayList.isEmpty();
    }
}
