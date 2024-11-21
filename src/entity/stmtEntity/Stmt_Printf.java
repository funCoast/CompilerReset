package entity.stmtEntity;

import entity.expression.Exp;

import java.util.ArrayList;

public class Stmt_Printf {
    String stringConst;
    ArrayList<Exp> expArrayList;

    public Stmt_Printf(String stringConst, ArrayList<Exp> expArrayList) {
        this.stringConst = stringConst;
        this.expArrayList = expArrayList;
    }

    public String getStringConst() {
        return stringConst;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }
}
