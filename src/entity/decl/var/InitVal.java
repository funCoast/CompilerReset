package entity.decl.var;

import entity.expression.Exp;

import java.util.ArrayList;

public class InitVal {
    String StringConst;
    ArrayList<Exp> expArrayList;

    public InitVal(String stringConst, ArrayList<Exp> expArrayList) {
        StringConst = stringConst;
        this.expArrayList = expArrayList;
    }

    public String getStringConst() {
        return StringConst;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }
}
