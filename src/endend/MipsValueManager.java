package endend;

import java.util.ArrayList;

public class MipsValueManager {
    private ArrayList<MipsValue> recordValue;
    private int offsetForId;
    private int usedWord;

    public MipsValueManager() {
        this.recordValue = new ArrayList<>();
        offsetForId = 1;
        usedWord = 0;
    }

    public ArrayList<MipsValue> getRecordValue() {
        return recordValue;
    }

    public int getUsedWord() {
        return usedWord;
    }

    public void putNewValue(int nameId, int valueSize, boolean isAddress) {
        // alloc a new
        usedWord = usedWord + valueSize;
        MipsValue mipsValue = new MipsValue(nameId, -4 * (nameId + offsetForId + valueSize - 1), valueSize, isAddress);
        recordValue.add(mipsValue);
        offsetForId = offsetForId + valueSize - 1; // id will grow 1
    }

    public void putOldValue(int nameId, int trueId) {
        recordValue.remove(search(nameId));
        // load a value
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == trueId) {
                MipsValue newMipsValue = new MipsValue(nameId, mipsValue.getActualAdr(), 1, mipsValue.isAddress());
                recordValue.add(newMipsValue);
                offsetForId = offsetForId - 1; // use newId store old one, should - 1
                return;
            }
        }
    }

    public void putDirect(int nameId, int offset, boolean isAddress) {
        if (hasRecordId(nameId)) {
            freshValue(nameId, offset, isAddress);
            return;
        }
        // direct record a value
        recordValue.add(new MipsValue(nameId, offset, 1, isAddress));
        offsetForId = offsetForId - 1; // waste 1 value
    }

    public void meetLabel() {
        offsetForId = offsetForId - 1;
    }

    public int getValueOffset(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue.getActualAdr();
            }
        }
        return 0;
    }

    public int getValueSize(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue.getValueSize();
            }
        }
        return 0;
    }

    public int getArrayOffset(int nameId, int index) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                int startAdr = mipsValue.getActualAdr();
                return startAdr + index * 4;
            }
        }
        return 0;
    }

    public boolean hasRecordId(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return true;
            }
        }
        return false;
    }

    public boolean isAddress(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue.isAddress();
            }
        }
        return false;
    }

    public void freshValue(int nameId, int offset, boolean isAddress) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                mipsValue.setActualAdr(offset);
                mipsValue.setAddress(isAddress);
            }
        }
    }

    public void down4Address(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                mipsValue.setActualAdr(mipsValue.getActualAdr() - 4);
            }
        }
    }

    public boolean isParamArray(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue.isParamArray();
            }
        }
        return false;
    }

    public void setIsParamArray(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                mipsValue.setParamArray(true);
            }
        }
    }

    public void setIsAddress(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                mipsValue.setAddress(true);
            }
        }
    }

    public void setIsGlobal(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                mipsValue.setGlobal(true);
            }
        }
    }

    public boolean isGlobal(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue.isGlobal();
            }
        }
        return false;
    }

    public MipsValue search(int nameId) {
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getNameId() == nameId) {
                return mipsValue;
            }
        }
        return null;
    }

    public int maxAddr() {
        int address = 0;
        for (MipsValue mipsValue : recordValue) {
            if (mipsValue.getActualAdr() < address) {
                address = mipsValue.getActualAdr();
            }
        }
        return address;
    }
}
