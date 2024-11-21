package entity.expression;

import java.util.ArrayList;

public class RelExp {
    ArrayList<Exp> expArrayList;
    ArrayList<Compare> compareArrayList;

    public RelExp(ArrayList<Exp> expArrayList, ArrayList<Compare> compareArrayList) {
        this.expArrayList = expArrayList;
        this.compareArrayList = compareArrayList;
    }
}
