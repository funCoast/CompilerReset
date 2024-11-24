package llvm.instruction;

import llvm.Instruction;
import llvm.StringConstValue;

public class PutStrInstr extends Instruction {
    StringConstValue stringConstValue;

    public PutStrInstr(StringConstValue stringConstValue) {
        this.stringConstValue = stringConstValue;
    }

    @Override
    public String toString() {
        int size = stringConstValue.getSize();
        int id = stringConstValue.getId();
        //call void @putstr(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i64 0, i64 0))
        return "call void @putstr(i8* getelementptr inbounds ([" + size + " x i8], [" + size
                + " x i8]* @.str." + id + ", i64 0, i64 0))";
    }
}
