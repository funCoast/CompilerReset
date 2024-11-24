package llvm.instruction;

import llvm.Instruction;
import llvm.InstructionType;
import llvm.RetType;

public class StoreInstr extends Instruction {
    private LLRegister valueReg;
    private LLRegister pointReg;
    private InstructionType instructionType;

    public StoreInstr(LLRegister valueReg, LLRegister pointReg) {
        this.pointReg = pointReg;
        this.valueReg = valueReg;
        this.instructionType = InstructionType.STORE;
    }

    public LLRegister getValueReg() {
        return valueReg;
    }

    public LLRegister getPointReg() {
        return pointReg;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    @Override
    public String toString() {
        if (pointReg.getRegisterType() == RegisterType.POINT) {
            return "store " + valueReg.getValueType() + valueReg + ", " + pointReg.getValueType() + pointReg;
        } else {
            return "store " + valueReg.getValueType() + valueReg + ", " + pointReg.getValueType() + pointReg;
        }
    }
}
