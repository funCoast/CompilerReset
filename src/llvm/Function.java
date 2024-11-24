package llvm;

import java.util.ArrayList;

public class Function extends GlobalValue {
    private ArrayList<Argument> argumentArrayList;
    private ArrayList<BasicBlock> basicBlockArrayList;
    private String name;
    private RetType retType;

    public Function(String name, RetType retType) {
        this.argumentArrayList = new ArrayList<>();
        this.basicBlockArrayList = new ArrayList<>();
        this.name = name;
        this.retType = retType;
    }

    public ArrayList<Argument> getArgumentArrayList() {
        return argumentArrayList;
    }

    public ArrayList<BasicBlock> getBasicBlockArrayList() {
        return basicBlockArrayList;
    }

    public void insertBasicBlock(BasicBlock block) {
        this.basicBlockArrayList.add(block);
    }

    public void insertArgument(Argument argument) {
        this.argumentArrayList.add(argument);
    }

    @Override
    public String toString() {
        String ret = retType.toString();
        if (ret.equals("VOID")) {
            ret = "void";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("define dso_local " + ret + " @" + name + "(");
        boolean onlyOneArgument = true;
        for (Argument argument : argumentArrayList) {
            if (!onlyOneArgument) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(argument);
            onlyOneArgument = false;
        }
        stringBuilder.append(") {\n");
        for (BasicBlock block : basicBlockArrayList) {
            for (Instruction instruction : block.getInstructionArrayList()) {
                stringBuilder.append("\t" + instruction.toString() + '\n');
            }
        }
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }
}
