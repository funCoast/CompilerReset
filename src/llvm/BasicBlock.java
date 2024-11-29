package llvm;

import llvm.instruction.LLRegister;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private LLRegister labelRegister;
    private ArrayList<Instruction> instructionArrayList;

    public BasicBlock(LLRegister labelRegister) {
        this.labelRegister = labelRegister;
        this.instructionArrayList = new ArrayList<>();
    }

    public ArrayList<Instruction> getInstructionArrayList() {
        return instructionArrayList;
    }

    public LLRegister getLabelRegister() {
        return labelRegister;
    }

    public void insertInstr(Instruction instruction) {
        this.instructionArrayList.add(instruction);
    }

    public void setLabelRegister(LLRegister labelRegister) {
        this.labelRegister = labelRegister;
    }

    public Instruction getNewestInstr() {
        if (!instructionArrayList.isEmpty()) {
            return instructionArrayList.get(instructionArrayList.size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Instruction instruction : instructionArrayList) {
            stringBuilder.append("\t" + instruction.toString() + '\n');
        }
        return stringBuilder.toString();
    }
}
