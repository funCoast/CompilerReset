package entity.expression;

import java.util.ArrayList;

public class UnaryExp {
    ArrayList<Operation> operationArrayList;
    boolean isFuncCall;
    PrimaryExp primaryExp;
    String Ident;
    ArrayList<Exp> funcRParams;

    public UnaryExp(ArrayList<Operation> operationArrayList, boolean isFuncCall,
                    PrimaryExp primaryExp, String ident, ArrayList<Exp> funcRParams) {
        this.operationArrayList = operationArrayList;
        this.isFuncCall = isFuncCall;
        this.primaryExp = primaryExp;
        Ident = ident;
        this.funcRParams = funcRParams;
    }
}
