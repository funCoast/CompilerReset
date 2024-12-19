package llvm.instruction;

import llvm.Instruction;
import llvm.RetType;

public class GetArrayAdrInstr extends Instruction {
    private LLRegister arrayReg;
    private LLRegister retReg;
    private RetType retType;
    private LLRegister indexReg;
    private int arraySize;
    private getArrayInstrType arrayInstrType;

    public GetArrayAdrInstr(LLRegister arrayReg, LLRegister retReg, RetType retType, LLRegister indexReg) {
        this.arrayReg = arrayReg;
        this.retReg = retReg;
        this.retType = retType;
        this.indexReg = indexReg;
        this.arrayInstrType = getArrayInstrType.UnknownSize;
    }

    public GetArrayAdrInstr(LLRegister arrayReg, LLRegister retReg, RetType retType, LLRegister indexReg, int arraySize) {
        this.arrayReg = arrayReg;
        this.retReg = retReg;
        this.retType = retType;
        this.indexReg = indexReg;
        this.arraySize = arraySize;
        this.arrayInstrType = getArrayInstrType.KnowSize;
    }

    @Override
    public String toString() {
        // %3 = getelementptr i32, i32* %2, i32 3
        if (this.arrayInstrType == getArrayInstrType.UnknownSize) {
            return "%" + retReg.getId() + " = getelementptr " + retType + ", " + retType + arrayReg + ", " + indexReg.getValueType() + indexReg;
        } else {
            // %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3
            return "%" + retReg.getId() + " = getelementptr [" + arraySize + " x " + retType + "], [" + arraySize + " x " + retType + "]" + arrayReg + ", i32 0, " + indexReg.getValueType() + indexReg;
        }
    }

    public boolean knowIndex() {
        if (arrayInstrType == getArrayInstrType.UnknownSize) {
            return false;
        } else {
            return true;
        }
    }

    public LLRegister getArrayReg() {
        return arrayReg;
    }

    public LLRegister getRetReg() {
        return retReg;
    }

    public RetType getRetType() {
        return retType;
    }

    public LLRegister getIndexReg() {
        return indexReg;
    }

    public int getArraySize() {
        return arraySize;
    }

    public getArrayInstrType getArrayInstrType() {
        return arrayInstrType;
    }

    private enum getArrayInstrType {
        UnknownSize,
        KnowSize,
    }
}

