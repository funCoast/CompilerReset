package entity.expression;

import java.util.ArrayList;

public class EqExp {
    private ArrayList<RelExp> relExpArrayList;
    private ArrayList<Compare> compareArrayList;

    public EqExp(ArrayList<RelExp> relExpArrayList, ArrayList<Compare> compareArrayList) {
        this.relExpArrayList = relExpArrayList;
        this.compareArrayList = compareArrayList;
    }

    public ArrayList<RelExp> getRelExpArrayList() {
        return relExpArrayList;
    }

    public ArrayList<Compare> getCompareArrayList() {
        return compareArrayList;
    }
}
