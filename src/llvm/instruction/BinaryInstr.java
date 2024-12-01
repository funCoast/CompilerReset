package llvm.instruction;

import llvm.Instruction;
import llvm.InstructionType;
import llvm.RetType;
import llvm.ValueType;

public class BinaryInstr extends Instruction {
    private LLRegister retReg;
    private LLRegister optAReg;
    private LLRegister optBReg;
    private InstructionType instructionType;

    public BinaryInstr(LLRegister retReg, LLRegister optAReg, LLRegister optBReg, InstructionType instructionType) {
        this.retReg = retReg;
        this.optAReg = optAReg;
        this.optBReg = optBReg;
        this.instructionType = instructionType;
    }

    public LLRegister getRetReg() {
        return retReg;
    }

    public LLRegister getOptAReg() {
        return optAReg;
    }

    public LLRegister getOptBReg() {
        return optBReg;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    @Override
    public String toString() {
        return "%" + retReg.getId() + " = " + instructionType + " i32" + optAReg + "," + optBReg;
    }


}
