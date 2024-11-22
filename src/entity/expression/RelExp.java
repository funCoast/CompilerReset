package entity.expression;

import java.util.ArrayList;

public class RelExp {
    private ArrayList<Exp> expArrayList;
    private ArrayList<Compare> compareArrayList;

    public RelExp(ArrayList<Exp> expArrayList, ArrayList<Compare> compareArrayList) {
        this.expArrayList = expArrayList;
        this.compareArrayList = compareArrayList;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }

    public ArrayList<Compare> getCompareArrayList() {
        return compareArrayList;
    }
}
