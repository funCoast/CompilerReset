package llvm.instruction;

import llvm.Instruction;

public class GetIntInstr extends Instruction {
    LLRegister register;

    public GetIntInstr(LLRegister register) {
        this.register = register;
    }

    public LLRegister getRegister() {
        return register;
    }

    @Override
    public String toString() {
        return "%" + register.getId() + " = call i32 @getint()";
    }
}
