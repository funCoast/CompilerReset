package llvm.instruction;

import llvm.Instruction;

public class PutChInstr extends Instruction {
    LLRegister register;

    public PutChInstr(LLRegister register) {
        this.register = register;
    }

    public LLRegister getRegister() {
        return register;
    }

    @Override
    public String toString() {
        //call void @putch(i8 %8)
        return "call void @putch(i32" + register + ")";
    }
}
