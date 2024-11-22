package middleend;

public class Symbol {
    private int tableId;

    private String name;

    private IdentType identType;

    private FuncInfo funcInfo;

    public Symbol(int tableId, String name, IdentType identType, FuncInfo funcInfo) {
        this.tableId = tableId;
        this.name = name;
        this.identType = identType;
        this.funcInfo = funcInfo;
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
}
