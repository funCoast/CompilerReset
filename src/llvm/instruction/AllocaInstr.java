package llvm.instruction;

import llvm.Instruction;

public class AllocaInstr extends Instruction {
    LLRegister llRegister;

    public AllocaInstr(LLRegister llRegister) {
        this.llRegister = llRegister;
    }

    public LLRegister getLlRegister() {
        return llRegister;
    }

    @Override
    public String toString() {
        return "%" + llRegister.getId() + " = " + "alloca " + llRegister.getValueType();
    }
}
