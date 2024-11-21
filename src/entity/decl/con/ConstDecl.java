package entity.decl.con;

import entity.BType;
import entity.decl.Decl;

import java.util.ArrayList;

public class ConstDecl extends Decl {
    BType bType;
    ArrayList<ConstDef> constDefArrayList;

    public ConstDecl(BType bType, ArrayList<ConstDef> constDefArrayList) {
        this.bType = bType;
        this.constDefArrayList = constDefArrayList;
    }

    public BType getbType() {
        return bType;
    }

    public ArrayList<ConstDef> getConstDefArrayList() {
        return constDefArrayList;
    }
}
