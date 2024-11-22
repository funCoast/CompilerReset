package entity;

import middleend.IdentType;

public class BType {
    IdentType identType;

    public BType(IdentType identType) {
        this.identType = identType;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public IdentType setArray() {
        if (identType == IdentType.Int) {
            return IdentType.IntArray;
        } else if (identType == IdentType.Char) {
            return IdentType.CharArray;
        } else if (identType == IdentType.ConstInt) {
            return IdentType.ConstIntArray;
        } else if (identType == IdentType.ConstChar) {
            return IdentType.ConstCharArray;
        }
        return null;
    }
}
