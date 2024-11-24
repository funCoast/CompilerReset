package llvm.instruction;

import llvm.Instruction;
import llvm.InstructionType;

public class LoadInstr extends Instruction {
    private LLRegister valueReg;
    private LLRegister pointReg;
    private InstructionType instructionType;

    public LoadInstr(LLRegister valueReg, LLRegister pointReg, InstructionType instructionType) {
        this.valueReg = valueReg;
        this.pointReg = pointReg;
        this.instructionType = instructionType;
    }

    @Override
    public String toString() {
        return "%" + valueReg.getId() + " = " + "load " + valueReg.getValueType() + ", " + pointReg.getValueType() + pointReg;
    }
}
