package entity.expression;

import java.util.ArrayList;

public class LOrExp {
    ArrayList<LAndExp> lAndExpArrayList;

    public LOrExp(ArrayList<LAndExp> lAndExpArrayList) {
        this.lAndExpArrayList = lAndExpArrayList;
    }
}
