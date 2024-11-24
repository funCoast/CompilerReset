package llvm;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructionArrayList;

    public BasicBlock() {
        this.instructionArrayList = new ArrayList<>();
    }

    public ArrayList<Instruction> getInstructionArrayList() {
        return instructionArrayList;
    }

    public void insertInstr(Instruction instruction) {
        this.instructionArrayList.add(instruction);
    }
}
