package entity;

import entity.decl.Decl;
import entity.funcdef.FuncDef;

import java.util.ArrayList;

public class CompUnit {
    ArrayList<Decl> declArrayList;

    ArrayList<FuncDef> funcDefArrayList;

    MainFuncDef mainFuncDef;

    public CompUnit() {
        this.funcDefArrayList = new ArrayList<>();
        this.declArrayList = new ArrayList<>();
        this.mainFuncDef = new MainFuncDef();
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
