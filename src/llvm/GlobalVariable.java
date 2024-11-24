package llvm;

public class GlobalVariable extends GlobalValue {
    String name;
    int valueInt;
    char valueChar;
    RetType retType;

    public GlobalVariable(String name, int valueInt, char valueChar, RetType retType) {
        this.name = name;
        this.valueInt = valueInt;
        this.valueChar = valueChar;
        this.retType = retType;
    }

    @Override
    public String toString() {
        if (retType == RetType.i32) {
            return "@" + name + " = dso_local global " + retType + " " + valueInt;
        } else {
            return "@" + name + " = dso_local global " + retType +  " " + (int) valueChar;
        }
    }
}
