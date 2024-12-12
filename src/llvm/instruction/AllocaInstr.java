package llvm.instruction;

import llvm.Instruction;

public class AllocaInstr extends Instruction {
    private LLRegister llRegister;
    private int size;

    public AllocaInstr(LLRegister llRegister, int size) {
        this.llRegister = llRegister;
        this.size = size;
    }

    public LLRegister getLlRegister() {
        return llRegister;
    }

    @Override
    public String toString() {
        if (size > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("%" + llRegister.getId() + " = " + "alloca ");
            stringBuilder.append("[" + size + " x " + llRegister.getValueType().toBasic() + "]");
            return stringBuilder.toString();
        } else {
            return "%" + llRegister.getId() + " = " + "alloca " + llRegister.getValueType();
        }
    }
}
