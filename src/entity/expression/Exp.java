package entity.expression;

import java.util.ArrayList;

public class Exp { // simplify addExp By Exp
    ArrayList<MulExp> mulExpArrayList;
    ArrayList<Operation> operationArrayList;

    public Exp(ArrayList<MulExp> mulExpArrayList, ArrayList<Operation> operationArrayList) {
        this.mulExpArrayList = mulExpArrayList;
        this.operationArrayList = operationArrayList;
    }

    public ArrayList<MulExp> getMulExpArrayList() {
        return mulExpArrayList;
    }

    public ArrayList<Operation> getOperationArrayList() {
        return operationArrayList;
    }
}
