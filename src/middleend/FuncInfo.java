package middleend;

import llvm.Function;

import java.util.ArrayList;

public class FuncInfo {
    private int paramNum;

    private ArrayList<IdentType> paramTypes;

    private Function function;

    public FuncInfo(int paramNum, ArrayList<IdentType> paramTypes, Function function) {
        this.paramNum = paramNum;
        this.paramTypes = paramTypes;
        this.function =function;
    }

    public int getParamNum() {
        return paramNum;
    }

    public ArrayList<IdentType> getParamTypes() {
        return paramTypes;
    }

    public boolean checkCons(FuncInfo funcInfo) {
        for (int i = 0; i < paramTypes.size(); i++) {
            IdentType typeA = paramTypes.get(i);
            IdentType typeB = funcInfo.getParamTypes().get(i);
            if (typeA == IdentType.Char || typeA == IdentType.ConstChar || typeA == IdentType.ConstInt
                    || typeA == IdentType.CharFunc || typeA == IdentType.IntFunc) {
                typeA = IdentType.Int;
            }
            if (typeB == IdentType.Char || typeB == IdentType.ConstChar || typeB == IdentType.ConstInt
                    || typeB == IdentType.CharFunc || typeB == IdentType.IntFunc) {
                typeB = IdentType.Int;
            }
            if (typeA == IdentType.ConstIntArray) {
                typeA = IdentType.IntArray;
            }
            if (typeA == IdentType.ConstCharArray) {
                typeA = IdentType.CharArray;
            }
            if (typeB == IdentType.ConstIntArray) {
                typeB = IdentType.IntArray;
            }
            if (typeB == IdentType.ConstCharArray) {
                typeB = IdentType.CharArray;
            }
            if (typeA != typeB) {
                return false;
            }
        }
        return true;
    }
}
