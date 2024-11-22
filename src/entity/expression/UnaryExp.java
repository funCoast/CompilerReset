package entity.expression;

import java.util.ArrayList;

public class UnaryExp {
    private ArrayList<Operation> operationArrayList;
    private boolean isFuncCall;
    private PrimaryExp primaryExp;
    private String Ident;
    private int identLine;
    private ArrayList<Exp> funcRParams;

    public UnaryExp(ArrayList<Operation> operationArrayList, boolean isFuncCall,
                    PrimaryExp primaryExp, String ident, ArrayList<Exp> funcRParams,
                    int identLine) {
        this.operationArrayList = operationArrayList;
        this.isFuncCall = isFuncCall;
        this.primaryExp = primaryExp;
        this.Ident = ident;
        this.identLine = identLine;
        this.funcRParams = funcRParams;
    }

    public ArrayList<Operation> getOperationArrayList() {
        return operationArrayList;
    }

    public boolean isFuncCall() {
        return isFuncCall;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public String getIdent() {
        return Ident;
    }

    public ArrayList<Exp> getFuncRParams() {
        return funcRParams;
    }

    public int getIdentLine() {
        return identLine;
    }
}
