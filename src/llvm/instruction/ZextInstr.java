package llvm.instruction;

import llvm.Instruction;

public class ZextInstr extends Instruction {
    LLRegister retRegister;
    LLRegister extRegister;

    public ZextInstr (LLRegister retRegister, LLRegister extRegister) {
        this.retRegister = retRegister;
        this.extRegister = extRegister;
    }

    @Override
    public String toString() {
        //%8 = zext i8 %6 to i32
        return "%" + retRegister.getId() + " = zext i8 %" + extRegister.getId() + " to i32";
    }
}
