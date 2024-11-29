package llvm;

import java.util.ArrayList;

public class GlobalVariable extends GlobalValue {
    String name;
    int valueInt;
    char valueChar;
    RetType retType;
    ArrayList<Integer> integerArrayList;
    int stringMaxSize;
    boolean isArray;

    public GlobalVariable(String name, int valueInt, char valueChar, RetType retType) {
        this.name = name;
        this.valueInt = valueInt;
        this.valueChar = valueChar;
        this.retType = retType;
        this.isArray = false;
    }

    public GlobalVariable(String name, RetType retType, ArrayList<Integer> integerArrayList) {
        this.name = name;
        this.retType = retType;
        this.integerArrayList = integerArrayList;
        this.isArray = true;
    }

    private boolean isArrayAllZero() {
        for (Integer i : integerArrayList) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (isArray) {
            if (retType == RetType.i32) {
                if (this.isArrayAllZero()) {
                    return "@" + name + " = dso_local global [" + integerArrayList.size() + " x i32] zeroinitializer";
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("@" + name + " = dso_local global [" + integerArrayList.size() + " x i32] [");
                    stringBuilder.append("i32 " + integerArrayList.get(0));
                    for (int i = 1; i < integerArrayList.size(); i++) {
                        stringBuilder.append(", i32 " + integerArrayList.get(i));
                    }
                    stringBuilder.append("]");
                    return stringBuilder.toString();
                }
            } else {
                // @c = dso_local global [8 x i8] [i8 102, i8 111, i8 111, i8 98, i8 97, i8 114, i8 0, i8 0]
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("@" + name + " = dso_local global [" + integerArrayList.size() + " x i8] [");
                stringBuilder.append("i8 " + integerArrayList.get(0));
                for (int i = 1; i < integerArrayList.size(); i++) {
                    stringBuilder.append(", i8 " + integerArrayList.get(i));
                }
                stringBuilder.append("]");
                return stringBuilder.toString();
            }
        } else {
            if (retType == RetType.i32) {
                return "@" + name + " = dso_local global " + retType + " " + valueInt;
            } else {
                return "@" + name + " = dso_local global " + retType + " " + (int) valueChar;
            }
        }
    }
}
