package entity;

import frontend.IdentType;

public class BType {
    IdentType identType;

    public BType(IdentType identType) {
        this.identType = identType;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public void setArray() {
        if (identType == IdentType.Int) {
            identType = IdentType.IntArray;
        } else if (identType == IdentType.Char) {
            identType = IdentType.CharArray;
        } else if (identType == IdentType.ConstInt) {
            identType = IdentType.ConstIntArray;
        } else if (identType == IdentType.ConstChar) {
            identType = IdentType.ConstCharArray;
        }
    }
}
