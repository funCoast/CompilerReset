package llvm.instruction;

import llvm.Instruction;
import llvm.RetType;

public class ReturnInstr extends Instruction {
    LLRegister retReg;
    RetType retType;

    public ReturnInstr(LLRegister retReg, RetType retType) {
        this.retReg = retReg;
        this.retType = retType;
    }

    public LLRegister getRetReg() {
        return retReg;
    }

    public RetType getRetType() {
        return retType;
    }

    @Override
    public String toString() {
        if (retType != RetType.VOID) {
            return "ret " + retType + retReg;
        } else {
            return "ret void";
        }
    }
}
