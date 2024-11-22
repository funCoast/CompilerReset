package entity.funcdef;

import entity.Block;
import middleend.IdentType;

import java.util.ArrayList;

public class FuncDef {
    private FuncType funcType;
    private String Ident;
    private ArrayList<FuncFParam> funcFParamArrayList;
    private Block block;
    private int identLine;

    public FuncDef(FuncType funcType, String ident, ArrayList<FuncFParam> funcFParamArrayList,
                   Block block, int identLine) {
        this.funcType = funcType;
        Ident = ident;
        this.funcFParamArrayList = funcFParamArrayList;
        this.block = block;
        this.identLine = identLine;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public String getIdent() {
        return Ident;
    }

    public ArrayList<FuncFParam> getFuncFParamArrayList() {
        return funcFParamArrayList;
    }

    public Block getBlock() {
        return block;
    }

    public int getIdentLine() {
        return identLine;
    }

    public ArrayList<IdentType> getParamIdentTypes() {
        ArrayList<IdentType> identTypeArrayList = new ArrayList<>();
        for (FuncFParam funcFParam : funcFParamArrayList) {
            identTypeArrayList.add(funcFParam.getbType().getIdentType());
        }
        return identTypeArrayList;
    }
}
