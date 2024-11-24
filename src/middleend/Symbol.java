package middleend;

import llvm.instruction.LLRegister;

public class Symbol {
    private int tableId;

    private String name;

    private IdentType identType;

    private FuncInfo funcInfo;

    private int value;

    private LLRegister llRegister;

    public Symbol(int tableId, String name, IdentType identType, FuncInfo funcInfo) {
        this.tableId = tableId;
        this.name = name;
        this.identType = identType;
        this.funcInfo = funcInfo;
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

    public void setValue(int value) {
        if (identType == IdentType.Char || identType == IdentType.ConstChar) {
            this.value = value & 0xFF;
        } else if (identType == IdentType.Int || identType == IdentType.ConstInt){
            this.value = value;
        }
    }
}
