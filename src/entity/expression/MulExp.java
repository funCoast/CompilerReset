package entity.expression;

import java.util.ArrayList;

public class MulExp {
    ArrayList<UnaryExp> unaryExpArrayList;
    ArrayList<Operation> operationArrayList;

    public MulExp(ArrayList<UnaryExp> unaryExpArrayList, ArrayList<Operation> operationArrayList) {
        this.unaryExpArrayList = unaryExpArrayList;
        this.operationArrayList = operationArrayList;
    }

    public ArrayList<UnaryExp> getUnaryExpArrayList() {
        return unaryExpArrayList;
    }

    public ArrayList<Operation> getOperationArrayList() {
        return operationArrayList;
    }
}
