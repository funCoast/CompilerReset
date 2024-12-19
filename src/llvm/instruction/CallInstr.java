package llvm.instruction;

import llvm.Instruction;
import llvm.RetType;

import java.util.ArrayList;
import java.util.Objects;

public class CallInstr extends Instruction {
    private RetType retType;
    private LLRegister llRegister;
    private String name;
    private ArrayList<LLRegister> argumentRegArrayList;

    public CallInstr(RetType retType, LLRegister llRegister, String name, ArrayList<LLRegister> argumentRegArrayList) {
        this.retType = retType;
        this.llRegister = llRegister;
        this.name = name;
        this.argumentRegArrayList = argumentRegArrayList;
    }

    public RetType getRetType() {
        return retType;
    }

    public LLRegister getLlRegister() {
        return llRegister;
    }

    public String getName() {
        return name;
    }

    public ArrayList<LLRegister> getArgumentRegArrayList() {
        return argumentRegArrayList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (llRegister != null) {
            stringBuilder.append("%" + llRegister.getId() + " = ");
        }
        String ret = retType.toString();
        if (Objects.equals(ret, "VOID")) {
            ret = "void";
        }
        stringBuilder.append("call " + ret + " @" + name + "(");
        boolean onlyOne = true;
        for (LLRegister llRegister : argumentRegArrayList) {
            if (!onlyOne) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(llRegister.getValueType()).append(llRegister.toString());
            onlyOne = false;
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
