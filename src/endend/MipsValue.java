package endend;

public class MipsValue {
    private int nameId; // only name
    private int actualAdr; // actually for offset
    private int valueSize;
    private boolean isAddress;
    private boolean isParamArray;
    private boolean isGlobal;

    public MipsValue(int nameId, int actualAdr, int valueSize, boolean isAddress) {
        this.nameId = nameId;
        this.actualAdr = actualAdr;
        this.valueSize = valueSize;
        this.isAddress = isAddress;
        this.isParamArray = false;
        this.isGlobal = false;
    }

    public int getNameId() {
        return nameId;
    }

    public int getActualAdr() {
        return actualAdr;
    }

    public int getValueSize() {
        return valueSize;
    }

    public boolean isAddress() {
        return isAddress;
    }

    public void setActualAdr(int actualAdr) {
        this.actualAdr = actualAdr;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public void setValueSize(int valueSize) {
        this.valueSize = valueSize;
    }

    public void setAddress(boolean address) {
        isAddress = address;
    }

    public boolean isParamArray() {
        return isParamArray;
    }

    public void setParamArray(boolean paramArray) {
        isParamArray = paramArray;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }
}
