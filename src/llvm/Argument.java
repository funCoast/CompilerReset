package llvm;

import llvm.instruction.LLRegister;

import java.util.HashMap;

public class Argument {
    private int id;
    private RetType valueType;
    LLRegister argumentReg;

    public Argument(int id, RetType valueType, LLRegister argumentReg) {
        this.id = id;
        this.valueType = valueType;
        this.argumentReg = argumentReg;
    }

    public int getId() {
        return id;
    }

    public RetType getValueType() {
        return valueType;
    }

    public LLRegister getArgumentReg() {
        return argumentReg;
    }

    @Override
    public String toString() {
        return valueType + argumentReg.toString();
    }
}
