package entity.funcdef;

import entity.Block;

import java.util.ArrayList;

public class FuncDef {
    FuncType funcType;
    String Ident;
    ArrayList<FuncFParam> funcFParamArrayList;
    Block block;

    public FuncDef(FuncType funcType, String ident,
                   ArrayList<FuncFParam> funcFParamArrayList, Block block) {
        this.funcType = funcType;
        Ident = ident;
        this.funcFParamArrayList = funcFParamArrayList;
        this.block = block;
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
}
