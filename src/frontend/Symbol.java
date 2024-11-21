package frontend;

public class Symbol {
    private int tableId;

    private String name;

    private IdentType identType;

    private FuncInfo funcInfo;

    public Symbol(int tableId, String name, int BType, boolean isConst, boolean isArray, boolean isFunc, FuncInfo funcInfo) {
        this.tableId = tableId;
        this.name = name;
        if (isFunc) { //Func
            //TODO
            if (BType == 1) {
                identType = IdentType.IntFunc;
            } else if (BType == 2) {
                identType = IdentType.CharFunc;
            } else if (BType == 3) {
                identType = IdentType.VoidFunc;
            }
            this.funcInfo = funcInfo;
        } else if (isConst) { // const
            if (isArray) { // Array
                if (BType == 1) {
                    identType = IdentType.ConstIntArray;
                } else if (BType == 2) {
                    identType = IdentType.ConstCharArray;
                }
            } else {
                if (BType == 1) {
                    identType = IdentType.ConstInt;
                } else if (BType == 2) {
                    identType = IdentType.ConstChar;
                }
            }
        } else { // not Const
            if (isArray) { // Array
                if (BType == 1) {
                    identType = IdentType.IntArray;
                } else if (BType == 2) {
                    identType = IdentType.CharArray;
                }
            } else {
                if (BType == 1) {
                    identType = IdentType.Int;
                } else if (BType == 2) {
                    identType = IdentType.Char;
                }
            }
        }
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
