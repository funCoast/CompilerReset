package entity.expression;

import java.util.ArrayList;

public class EqExp {
    ArrayList<RelExp> relExpArrayList;
    ArrayList<Compare> compareArrayList;

    public EqExp(ArrayList<RelExp> relExpArrayList, ArrayList<Compare> compareArrayList) {
        this.relExpArrayList = relExpArrayList;
        this.compareArrayList = compareArrayList;
    }
}
