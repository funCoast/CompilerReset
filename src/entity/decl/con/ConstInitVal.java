package entity.decl.con;

import entity.expression.Exp;

import java.util.ArrayList;

public class ConstInitVal {
    String StringConst;
    ArrayList<Exp> expArrayList;

    public ConstInitVal(String stringConst) {
        StringConst = stringConst;
    }

    public ConstInitVal(ArrayList<Exp> expArrayList) {
        this.expArrayList = expArrayList;
    }

    public String getStringConst() {
        return StringConst;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }
}
