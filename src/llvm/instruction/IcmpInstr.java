package llvm.instruction;

import entity.expression.Compare;
import llvm.Instruction;
import llvm.RetType;

public class IcmpInstr extends Instruction {
    private LLRegister resultReg;
    private LLRegister leftReg;
    private LLRegister rightReg;
    private RetType regType;
    private IcmpType cmpType;

    public IcmpInstr(LLRegister resultReg, LLRegister leftReg, LLRegister rightReg, RetType regType, Compare compare) {
        this.resultReg = resultReg;
        this.leftReg = leftReg;
        this.rightReg = rightReg;
        this.regType = regType;
        if (compare == Compare.EQUAL) {
            this.cmpType = IcmpType.eq;
        } else if (compare == Compare.NOT_EQUAL) {
            this.cmpType = IcmpType.ne;
        } else if (compare == Compare.BIG_THAN) {
            this.cmpType = IcmpType.sgt;
        } else if (compare == Compare.BIG_OR_EQUAL) {
            this.cmpType = IcmpType.sge;
        } else if (compare == Compare.SMALL_THAN) {
            this.cmpType = IcmpType.slt;
        } else if (compare == Compare.SMALL_OR_EQUAL) {
            this.cmpType = IcmpType.sle;
        }
    }

    @Override
    public String toString() {
        return "%" + resultReg.getId() + " = icmp " + cmpType + " " + regType + leftReg + "," + rightReg;
    }

    public String getCmpType() {
        return cmpType.toString();
    }

    public LLRegister getResultReg() {
        return resultReg;
    }

    public LLRegister getLeftReg() {
        return leftReg;
    }

    public LLRegister getRightReg() {
        return rightReg;
    }

    public RetType getRegType() {
        return regType;
    }
}

enum IcmpType {
    eq,
    ne,
    ugt,
    uge,
    ult,
    ule,
    sgt,
    sge,
    slt,
    sle,
}
