package llvm.instruction;

import llvm.Instruction;

public class BrInstr extends Instruction {
    private LLRegister goLabelReg;
    private LLRegister condReg;
    private LLRegister trueLabelReg;
    private LLRegister falseLabelReg;
    private BrType brType;

    public BrInstr(LLRegister goLabelReg) {
        this.goLabelReg = goLabelReg;
        this.brType = BrType.GO;
    }

    public BrInstr(LLRegister condReg, LLRegister trueLabelReg, LLRegister falseLabelReg) {
        this.condReg = condReg;
        this.trueLabelReg = trueLabelReg;
        this.falseLabelReg = falseLabelReg;
        this.brType = BrType.CONDITION;
    }

    @Override
    public String toString() {
        if (brType == BrType.CONDITION) {
            return "br i1 %" + condReg.getId() + ", label %" + trueLabelReg.getId() + ", label %" + falseLabelReg.getId();
        } else {
            return "br label %" + goLabelReg.getId();
        }
    }
}

enum BrType {
    CONDITION,
    GO,
}
