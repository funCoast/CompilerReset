package entity.expression;

import java.util.ArrayList;

public class LAndExp {
    ArrayList<EqExp> expArrayList;

    public LAndExp(ArrayList<EqExp> expArrayList) {
        this.expArrayList = expArrayList;
    }

    public ArrayList<EqExp> getExpArrayList() {
        return expArrayList;
    }
}
