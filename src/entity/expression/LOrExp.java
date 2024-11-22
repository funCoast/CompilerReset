package entity.expression;

import java.util.ArrayList;

public class LOrExp {
    private ArrayList<LAndExp> lAndExpArrayList;

    public LOrExp(ArrayList<LAndExp> lAndExpArrayList) {
        this.lAndExpArrayList = lAndExpArrayList;
    }

    public ArrayList<LAndExp> getlAndExpArrayList() {
        return lAndExpArrayList;
    }
}
