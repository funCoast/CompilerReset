package middleend;

import llvm.instruction.LLRegister;

public class Symbol {
    private int tableId;

    private String name;

    private IdentType identType;

    private FuncInfo funcInfo;

    private LLRegister llRegister;

    private int arraySize;

    public Symbol(int tableId, String name, IdentType identType, FuncInfo funcInfo , int arraySize) {
        this.tableId = tableId;
        this.name = name;
        this.identType = identType;
        this.funcInfo = funcInfo;
        this.arraySize = arraySize;
    }

    public void setLlRegister(LLRegister llRegister) {
        this.llRegister = llRegister;
    }

    public LLRegister getLlRegister() {
        return llRegister;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public String getName() {
        return name;
    }

    public boolean isConst() {
        return identType == IdentType.ConstIntArray || identType == IdentType.ConstCharArray
                || identType == IdentType.ConstInt || identType == IdentType.ConstChar;
    }

    public FuncInfo getFuncInfo() {
        return funcInfo;
    }

    public int getTableId() {
        return tableId;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }
}
