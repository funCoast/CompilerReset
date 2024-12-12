package llvm.instruction;

import llvm.Instruction;

public class TruncInstr extends Instruction {
    LLRegister retReg;
    LLRegister cutReg;

    public TruncInstr(LLRegister retReg, LLRegister cutReg) {
        this.retReg = retReg;
        this.cutReg = cutReg;
    }

    @Override
    public String toString() {
        // %5 = trunc i32 %4 to i8
        return "%" + retReg.getId() + " = trunc i32" + cutReg + " to i8";
    }
}
