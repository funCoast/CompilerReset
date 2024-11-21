package entity.decl.var;

import entity.BType;
import entity.decl.Decl;

import java.util.ArrayList;

public class VarDecl extends Decl {
    BType bType;
    ArrayList<VarDef> varDefArrayList;

    public VarDecl(BType bType, ArrayList<VarDef> varDefArrayList) {
        this.bType = bType;
        this.varDefArrayList = varDefArrayList;
    }

    public BType getbType() {
        return bType;
    }

    public ArrayList<VarDef> getVarDefArrayList() {
        return varDefArrayList;
    }
}
