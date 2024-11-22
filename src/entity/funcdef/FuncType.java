package entity.funcdef;

import middleend.IdentType;

public class FuncType {
    IdentType identType;

    public FuncType(IdentType identType) {
        this.identType = identType;
    }

    public IdentType getIdentType() {
        return identType;
    }
}
