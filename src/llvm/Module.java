package llvm;

import java.util.ArrayList;

public class Module {
    private ArrayList<GlobalVariable> globalVariableArrayList;
    private ArrayList<StringConstValue> stringConstValueArrayList;
    private ArrayList<Function> functionArrayList;
    private Function mainFunction;

    public Module() {
        this.functionArrayList = new ArrayList<>();
        this.stringConstValueArrayList = new ArrayList<>();
        this.globalVariableArrayList = new ArrayList<>();
        this.mainFunction = new Function("main", RetType.i32);
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public ArrayList<GlobalVariable> getGlobalVariableArrayList() {
        return globalVariableArrayList;
    }

    public ArrayList<Function> getFunctionArrayList() {
        return functionArrayList;
    }

    public void insertGlobalValue(GlobalVariable globalVariable) {
        this.globalVariableArrayList.add(globalVariable);
    }

    public void insertFunction(Function function) {
        this.functionArrayList.add(function);
    }

    public void setMainFunction(Function mainFunction) {
        this.mainFunction = mainFunction;
    }

    public ArrayList<StringConstValue> getStringConstValueArrayList() {
        return stringConstValueArrayList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (true) {
            stringBuilder.append("declare i32 @getint()\n");
            stringBuilder.append("declare i32 @getchar()\n");
            stringBuilder.append("declare void @putint(i32)\n");
            stringBuilder.append("declare void @putch(i32)\n");
            stringBuilder.append("declare void @putstr(i8*)\n");
        }
        stringBuilder.append('\n');
        for (GlobalVariable globalVariable : globalVariableArrayList) {
            stringBuilder.append(globalVariable.toString() + '\n');
        }
        stringBuilder.append('\n');
        for (StringConstValue stringConstValue : stringConstValueArrayList) {
            stringBuilder.append(stringConstValue.toString() + '\n');
        }
        for (Function function : functionArrayList) {
            stringBuilder.append(function.toString());

            stringBuilder.append('\n');
        }
        stringBuilder.append(mainFunction.toString());
        return stringBuilder.toString();
    }
}
