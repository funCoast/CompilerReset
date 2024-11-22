package entity;

import entity.decl.Decl;
import entity.funcdef.FuncDef;

import java.util.ArrayList;

public class CompUnit {
    ArrayList<Decl> declArrayList;

    ArrayList<FuncDef> funcDefArrayList;

    MainFuncDef mainFuncDef;

    public CompUnit(ArrayList<Decl> declArrayList, ArrayList<FuncDef> funcDefArrayList, MainFuncDef mainFuncDef) {
        this.declArrayList = declArrayList;
        this.funcDefArrayList = funcDefArrayList;
        this.mainFuncDef = mainFuncDef;
    }

    public ArrayList<Decl> getDeclArrayList() {
        return declArrayList;
    }

    public ArrayList<FuncDef> getFuncDefArrayList() {
        return funcDefArrayList;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }
}
