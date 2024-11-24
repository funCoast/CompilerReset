package llvm.instruction;

import llvm.Instruction;

public class PutIntInstr extends Instruction {
    LLRegister register;

    public PutIntInstr(LLRegister register) {
        this.register = register;
    }

    @Override
    public String toString() {
        // call void @putint(i32 %7)
        return "call void @putint(i32 %" + register.getId() + ')';
    }
}
