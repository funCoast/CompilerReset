package llvm.instruction;

import llvm.Instruction;

public class GetCharInstr extends Instruction {
    LLRegister register;

    public GetCharInstr(LLRegister register) {
        this.register = register;
    }

    @Override
    public String toString() {
        return "%" + register.getId() + " = call i32 @getchar()";
    }
}
