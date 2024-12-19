package endend;

import frontend.CompError;
import llvm.*;
import llvm.Module;
import llvm.instruction.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Translator {
    private CompError compError;
    private Module compModule;
    private StringBuilder outputMips;
    private MipsValueManager mipsValueManager;
    private String funcName;
    private int icmpCount;


    public Translator(Module compModule,  CompError compError) {
        this.compError = compError;
        this.compModule = compModule;
        this.outputMips = new StringBuilder();
        this.icmpCount = 0;
    }

    public void translate() {
        if (!compError.isEmpty()) {
            compError.output();
            return;
        }
        translateGlobalVariable();
        translateStringConstValue();
        //
        outputMips.append(".text\n");
        outputMips.append("\tj main\n");
        ArrayList<Function> functionArrayList = compModule.getFunctionArrayList();
        for (Function function : functionArrayList) {
            outputMips.append(translateFunction(function));
            outputMips.append("\n");
        }
        outputMips.append(translateMainFunction());
        outputMipsCode();
    }

    private void translateGlobalVariable() {
        ArrayList<GlobalVariable> globalVariableArrayList = compModule.getGlobalVariableArrayList();
        if (!globalVariableArrayList.isEmpty()) {
            outputMips.append(".data\n");
        }
        for (GlobalVariable globalVariable : globalVariableArrayList) {
            outputMips.append("\t").append(globalVariable.getMipsCode()).append("\n");
        }
    }

    private void translateStringConstValue() {
        ArrayList<StringConstValue> stringConstValueArrayList = compModule.getStringConstValueArrayList();
        if (outputMips.isEmpty() && !stringConstValueArrayList.isEmpty()) {
            outputMips.append(".data\n");
        }
        for (StringConstValue stringConstValue : stringConstValueArrayList) {
            outputMips.append("\t").append(stringConstValue.getMipsCode()).append("\n");
        }
    }

    private String translateFunction(Function function) {
        this.mipsValueManager = new MipsValueManager();
        ArrayList<BasicBlock> basicBlockArrayList = function.getBasicBlockArrayList();
        this.funcName = function.getName();
        ArrayList<Argument> argumentArrayList = function.getArgumentArrayList();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(funcName).append("_Ori:").append("\n");
        //
        stringBuilder.append("\tsubu $sp, $sp, 4\n");
        stringBuilder.append("\tsw $ra, 0($sp)\n");
        for (Argument argument : argumentArrayList) {
            if (argument.getArgumentReg().getRegisterType() == RegisterType.P_POINT) {
                mipsValueManager.putNewValue(argument.getId(), 1, true);
                mipsValueManager.setIsParamArray(argument.getId());
            } else {
                mipsValueManager.putNewValue(argument.getId(), 1, false);
            }
        }
        mipsValueManager.meetLabel();
        //
        for (BasicBlock block : basicBlockArrayList) {
            int labelId = block.getLabelRegister().getId();
            if (labelId != argumentArrayList.size()) {
                mipsValueManager.meetLabel();
                stringBuilder.append(funcName).append("_").append(block.getLabelRegister().getId()).append(":\n");
            }
            stringBuilder.append(translateBasicBlock(block));
        }
        BasicBlock newestBlock = basicBlockArrayList.get(basicBlockArrayList.size() - 1);
        if (!(newestBlock.getNewestInstr() instanceof ReturnInstr)) {
            stringBuilder.append("\tlw $ra, 0($sp)\n");
            stringBuilder.append("\taddu $sp, $sp, 4\n");
            stringBuilder.append("\tjr $ra\n");
        }
        return stringBuilder.toString();
    }

    private String translateMainFunction() {
        this.mipsValueManager = new MipsValueManager();
        Function mainFunction = compModule.getMainFunction();
        ArrayList<BasicBlock> basicBlockArrayList = mainFunction.getBasicBlockArrayList();
        this.funcName = mainFunction.getName();
        ArrayList<Argument> argumentArrayList = mainFunction.getArgumentArrayList();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(funcName).append(":\n");
        mipsValueManager.meetLabel();
        // stringBuilder.append(funcName).append("_Ori:").append("\n");
        for (BasicBlock block : basicBlockArrayList) {
            int labelId = block.getLabelRegister().getId();
            if (labelId != argumentArrayList.size()) {
                mipsValueManager.meetLabel();
                stringBuilder.append(funcName).append("_").append(block.getLabelRegister().getId()).append(":\n");
            }
            stringBuilder.append(translateBasicBlock(block));
        }
        return stringBuilder.toString();
    }

    private String translateBasicBlock(BasicBlock block) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Instruction> instructionArrayList = block.getInstructionArrayList();

        for (Instruction instruction : instructionArrayList) {
            String instrMipsCode = translateInstruction(instruction);
            if (instrMipsCode != null) { // no need, because some instr no output
                stringBuilder.append(instrMipsCode);
            }
        }
        if (instructionArrayList.isEmpty()) {
            stringBuilder.append("\tlw $ra, 0($sp)\n");
            stringBuilder.append("\taddu $sp, $sp, 4\n");
            stringBuilder.append("\tjr $ra\n");
        }
        return stringBuilder.toString();
    }

    private String translateInstruction(Instruction instruction) {
        if (instruction instanceof AllocaInstr) {
            translateAllocaInstr((AllocaInstr) instruction);
        } else if (instruction instanceof StoreInstr) {
            return translateStoreInstr((StoreInstr) instruction);
        } else if (instruction instanceof LoadInstr) {
            return translateLoadInstr((LoadInstr) instruction);
        } else if (instruction instanceof BinaryInstr) {
            return translateBinaryInstr((BinaryInstr) instruction);
        } else if (instruction instanceof GetArrayAdrInstr) {
            return translateGetArrayAdrInstr((GetArrayAdrInstr) instruction);
        } else if (instruction instanceof BrInstr) {
            return translateBrInstr((BrInstr) instruction);
        } else if (instruction instanceof IcmpInstr) {
            return translateIcmpInstr((IcmpInstr) instruction);
        } else if (instruction instanceof CallInstr) {
            return translateCallInstr((CallInstr) instruction);
        } else if (instruction instanceof ReturnInstr) {
            return translateReturnInstr((ReturnInstr) instruction);
        } else if (instruction instanceof GetCharInstr) {
            return translateGetCharInstr((GetCharInstr) instruction);
        } else if (instruction instanceof GetIntInstr) {
            return translateGetIntInstr((GetIntInstr) instruction);
        } else if (instruction instanceof PutChInstr) {
            return translatePutChInstr((PutChInstr) instruction);
        } else if (instruction instanceof PutIntInstr) {
            return translatePutIntInstr((PutIntInstr) instruction);
        } else if (instruction instanceof PutStrInstr) {
            return translatePutStrInstr((PutStrInstr) instruction);
        } else if (instruction instanceof TruncInstr) {
            return translateTruncInstr((TruncInstr) instruction);
        } else if (instruction instanceof ZextInstr) {
            return translateZextInstr((ZextInstr) instruction);
        }
        return null;
    }

    private void translateAllocaInstr(AllocaInstr allocaInstr) {
        int size = allocaInstr.getSize();
        int nameId = allocaInstr.getLlRegister().getId();
        if (size != 0) {
            mipsValueManager.putNewValue(nameId, size, allocaInstr.getLlRegister().getRegisterType() == RegisterType.P_POINT);
        } else {
            mipsValueManager.putNewValue(nameId, 1, allocaInstr.getLlRegister().getRegisterType() == RegisterType.P_POINT);
        }
    }

    private String translateStoreInstr(StoreInstr storeInstr) {
        StringBuilder stringBuilder = new StringBuilder();
        LLRegister valueReg = storeInstr.getValueReg();
        LLRegister targetReg = storeInstr.getPointReg();
        RegisterType valueRegisterType = valueReg.getRegisterType();
        int targetId = targetReg.getId();
        if (valueRegisterType == RegisterType.NUM || valueRegisterType == RegisterType.CHAR) {
            int value = storeInstr.getValueReg().getRealValue();
            stringBuilder.append("\tli $t0, " + value + "\n");
            stringBuilder.append(swT0ToReg(targetId, 0, targetReg));
        } else {
            if (!mipsValueManager.hasRecordId(targetId)) {
                mipsValueManager.putNewValue(targetId, 1, valueRegisterType == RegisterType.P_POINT);
            }
            // lw value
            if (valueRegisterType == RegisterType.P_POINT) {
                mipsValueManager.setIsParamArray(targetId); // do as paramArray
                // only lw value, not search for address
                stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(storeInstr.getValueReg().getId())).append("($sp)\n");
                stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(targetId)).append("($sp)\n");
                return stringBuilder.toString();
            }else {
                stringBuilder.append(lwToReg("$t0", "$t1", valueReg, 0));
            }
            // sw target
            stringBuilder.append(swT0ToReg(targetId, 0, targetReg));
        }
        return stringBuilder.toString();
    }

    private String translateLoadInstr(LoadInstr loadInstr) {
        LLRegister pointReg = loadInstr.getPointReg();
        LLRegister targetReg = loadInstr.getValueReg();
        StringBuilder stringBuilder = new StringBuilder();
        if (mipsValueManager.isParamArray(loadInstr.getPointReg().getId())) {
            if (!mipsValueManager.hasRecordId(targetReg.getId())) {
                mipsValueManager.putNewValue(targetReg.getId(), 1, false);
            }
            stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(pointReg.getId())).append("($sp)\n");
            stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(targetReg.getId())).append("($sp)\n");
            mipsValueManager.setIsParamArray(loadInstr.getValueReg().getId());
            return stringBuilder.toString();
        }
        if (mipsValueManager.isGlobal(loadInstr.getPointReg().getId())) {
            if (!mipsValueManager.hasRecordId(targetReg.getId())) {
                mipsValueManager.putNewValue(targetReg.getId(), 1, false);
            }
            stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(pointReg.getId())).append("($sp)\n");
            stringBuilder.append("\tlw $t0, 0($t0)\n");
            stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(targetReg.getId())).append("($sp)\n");
            return stringBuilder.toString();
        }
        if (loadInstr.getPointReg().getRegisterType() == RegisterType.GLOBAL) {
            if (loadInstr.getPointReg().getValueArrayList() != null && !loadInstr.getPointReg().getValueArrayList().isEmpty()) {
                stringBuilder.append("\tla $t0, ").append(pointReg.getName()).append("\n");
                stringBuilder.append(swT0ToReg(targetReg.getId(), 0, targetReg));
                return stringBuilder.toString();
            } else {
                int nameId = loadInstr.getValueReg().getId();
                mipsValueManager.putNewValue(nameId, 1, false);
                stringBuilder.append("\tla $t0, ").append(loadInstr.getPointReg().getName() + "\n");
                stringBuilder.append("\tlw $t0, 0($t0)\n");
                stringBuilder.append("\tsw $t0, " + mipsValueManager.getValueOffset(nameId) + "($sp)\n");
                return stringBuilder.toString();
            }
        } else if (mipsValueManager.isAddress(loadInstr.getPointReg().getId())) {
            if (mipsValueManager.isParamArray(loadInstr.getPointReg().getId())) {
                int nameId = loadInstr.getValueReg().getId();
                mipsValueManager.putNewValue(nameId, 1, true);
                mipsValueManager.setIsParamArray(nameId);
                stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(loadInstr.getPointReg().getId())).append("($sp)\n");
                stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
            } else {
                int nameId = loadInstr.getValueReg().getId();
                mipsValueManager.putNewValue(nameId, 1, false);
                stringBuilder.append(lwToReg("$t0", "$t1", loadInstr.getPointReg(), 0));
                stringBuilder.append("\tsw $t0, " + mipsValueManager.getValueOffset(nameId) + "($sp)\n");
            }
            return stringBuilder.toString();
        }
        int nameId = loadInstr.getValueReg().getId();
        int trueId = loadInstr.getPointReg().getId();
        if (!mipsValueManager.hasRecordId(nameId)) {
            mipsValueManager.putNewValue(nameId, 1, false);
        }
        mipsValueManager.putOldValue(nameId, trueId);
        return null;
    }

    private String translateBinaryInstr(BinaryInstr binaryInstr) {
        StringBuilder stringBuilder = new StringBuilder();
        int resultId = binaryInstr.getRetReg().getId();
        if (!mipsValueManager.hasRecordId(resultId)) {
            mipsValueManager.putNewValue(resultId, 1, false);
        }
        //
        InstructionType instructionType = binaryInstr.getInstructionType();
        stringBuilder.append(lwToReg("$t0", "$t2", binaryInstr.getOptAReg(), 0));
        stringBuilder.append(lwToReg("$t1", "$t2", binaryInstr.getOptBReg(), 0));
        if (instructionType == InstructionType.add) {
            stringBuilder.append("\taddu $t0, $t0, $t1\n");
        } else if (instructionType == InstructionType.sub) {
            stringBuilder.append("\tsubu $t0, $t0, $t1\n");
        } else if (instructionType == InstructionType.mul) {
            stringBuilder.append("\tmult $t0, $t1\n");
            stringBuilder.append("\tmflo $t0\n");
        } else if (instructionType == InstructionType.sdiv) {
            stringBuilder.append("\tdiv $t0, $t1\n");
            stringBuilder.append("\tmflo $t0\n");
        } else if (instructionType == InstructionType.srem) {
            stringBuilder.append("\tdiv $t0, $t1\n");
            stringBuilder.append("\tmfhi $t0\n");
        }
        stringBuilder.append(swT0ToReg(resultId, 0, binaryInstr.getRetReg()));
        return stringBuilder.toString();
    }

    private String translateGetArrayAdrInstr(GetArrayAdrInstr instruction) {
        LLRegister arrayRegister = instruction.getArrayReg();
        StringBuilder stringBuilder = new StringBuilder();
        LLRegister retReg = instruction.getRetReg();
        if (!mipsValueManager.hasRecordId(retReg.getId())) {
            mipsValueManager.putNewValue(retReg.getId(), 1, false);
        }
        if (mipsValueManager.isParamArray(arrayRegister.getId())) {
            LLRegister indexReg = instruction.getIndexReg();
            stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(arrayRegister.getId())).append("($sp)\n");
            if (indexReg.getRegisterType() == RegisterType.NUM || indexReg.getRegisterType() == RegisterType.CHAR) {
                stringBuilder.append("\taddu $t0, $t0, " + indexReg.getRealValue() * 4 + "\n");
            } else {
                stringBuilder.append(lwToReg("$t1", "$t2", indexReg, 0));
                stringBuilder.append("\tsll $t1, $t1, 2\n");
                stringBuilder.append("\taddu $t0, $t0, $t1\n");
            }
            stringBuilder.append(swT0ToReg(retReg.getId(), 0, retReg));
            mipsValueManager.setIsAddress(retReg.getId());
            return stringBuilder.toString();
        }
        if (arrayRegister.getRegisterType() == RegisterType.GLOBAL) {
            // use global Array
            stringBuilder.append("\tla $t0, " + arrayRegister.getName() + "\n");
            LLRegister indexReg = instruction.getIndexReg();
            if (indexReg.getRegisterType() == RegisterType.NUM || indexReg.getRegisterType() == RegisterType.CHAR) {
                stringBuilder.append("\taddu $t0, $t0, " + indexReg.getRealValue() * 4 + "\n");
            } else {
                stringBuilder.append(lwToReg("$t1", "$t2", indexReg, 0));
                stringBuilder.append("\tsll $t1, $t1, 2\n");
                stringBuilder.append("\taddu $t0, $t0, $t1\n");
            }
            stringBuilder.append(swT0ToReg(retReg.getId(), 0, retReg));
            mipsValueManager.setIsGlobal(retReg.getId());
            return stringBuilder.toString();
        } else {
            // use point Array
            LLRegister indexReg = instruction.getIndexReg();
            if (indexReg.getRegisterType() == RegisterType.NUM || indexReg.getRegisterType() == RegisterType.CHAR) {
                int arrayId = instruction.getArrayReg().getId();
                int nameId = instruction.getRetReg().getId();
                int index = instruction.getIndexReg().getRealValue();
                if (mipsValueManager.isAddress(arrayId)) {
                    if (mipsValueManager.isParamArray(arrayId)) {
                        stringBuilder.append("\tlw $t1, ").append(mipsValueManager.getValueOffset(arrayId)).append("($sp)\n");
                        stringBuilder.append("\taddi $t0, $t1, ").append(index * 4).append("\n");
                        stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
                        mipsValueManager.setIsAddress(nameId);
                        return stringBuilder.toString();
                    } else {
                        if (!mipsValueManager.hasRecordId(nameId)) {
                            mipsValueManager.putNewValue(nameId, 1, false);
                        }
                        mipsValueManager.putDirect(nameId, mipsValueManager.getValueOffset(arrayId) + index * 4, true);
                    }
                } else {
                    int offset = mipsValueManager.getArrayOffset(arrayId, index);
                    if (!mipsValueManager.hasRecordId(nameId)) {
                        mipsValueManager.putNewValue(nameId, 1, false);
                    }
                    mipsValueManager.putDirect(nameId, offset, false);
                }
                return null;
            } else {
                stringBuilder.append(lwToReg("$t0", "$t1", indexReg, 0));
                stringBuilder.append("\tsll $t0, $t0, 2\n");
                stringBuilder.append("\taddu $t0, $t0, " + (mipsValueManager.getValueOffset(arrayRegister.getId())) + "\n");
                stringBuilder.append("\taddu $t0, $t0, $sp\n");
                stringBuilder.append(swT0ToReg(retReg.getId(), 0, retReg));
                mipsValueManager.setIsAddress(instruction.getRetReg().getId());
                return stringBuilder.toString();
            }
        }
    }

    private String translateBrInstr(BrInstr instruction) {
        if (instruction.hasCond()) {
            int trueId = instruction.getTrueLabelReg().getId();
            int falseId = instruction.getFalseLabelReg().getId();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(lwToReg("$t0", "$t1", instruction.getCondReg(), 0));
            int cmpNum = getIcmpCount();
            String labelName = funcName + "_Cmp_" + cmpNum;
            stringBuilder.append("\tbeq $t0, $zero, " + labelName + "\n");
            stringBuilder.append("\tj ").append(this.funcName).append("_").append(trueId).append("\n");
            stringBuilder.append(labelName + ":\n");
            stringBuilder.append("\tj ").append(this.funcName).append("_").append(falseId).append("\n");
            return stringBuilder.toString();
        } else {
            int labelId = instruction.getGoLabelReg().getId();
            return "\tj " + this.funcName + "_" + labelId + "\n";
        }
    }

    private String translateIcmpInstr(IcmpInstr instruction) {
        String cmpType = instruction.getCmpType();
        StringBuilder stringBuilder = new StringBuilder();
        int resultId = instruction.getResultReg().getId();
        if (!mipsValueManager.hasRecordId(resultId)) {
            mipsValueManager.putNewValue(resultId, 1, false);
        }
        LLRegister resultReg = instruction.getResultReg();
        mipsValueManager.putNewValue(resultId, 1, false);
        stringBuilder.append(lwToReg("$t0", "$t1", instruction.getLeftReg(), 0));
        stringBuilder.append(lwToReg("$t1", "$t2", instruction.getRightReg(), 0));
        int cmpNum = getIcmpCount();
        String aLabelName = funcName + "_Cmp_A_" + cmpNum;
        String bLabelName = funcName + "_Cmp_B_" + cmpNum;
        if (cmpType.equals("eq")) {
            stringBuilder.append("\tbeq $t0, $t1, " + bLabelName + "\n");
        } else if (cmpType.equals("ne")) {
            stringBuilder.append("\tbne $t0, $t1, " + bLabelName + "\n");
        } else if (cmpType.equals("sgt")) {
            stringBuilder.append("\tsubu $t0, $t0, $t1\n");
            stringBuilder.append("\tbgtz $t0, " + bLabelName + "\n");
        } else if (cmpType.equals("sge")) {
            stringBuilder.append("\tsubu $t0, $t0, $t1\n");
            stringBuilder.append("\tbgez $t0, " + bLabelName + "\n");
        } else if (cmpType.equals("slt")) {
            stringBuilder.append("\tsubu $t0, $t0, $t1\n");
            stringBuilder.append("\tbltz $t0, " + bLabelName + "\n");
        } else if (cmpType.equals("sle")) {
            stringBuilder.append("\tsubu $t0, $t0, $t1\n");
            stringBuilder.append("\tblez $t0, " + bLabelName + "\n");
        }
        stringBuilder.append("\tli $t0, 0\n");
        stringBuilder.append(swT0ToReg(resultId, 0, resultReg));
        stringBuilder.append("\tj " + aLabelName + "\n");
        stringBuilder.append(bLabelName).append(":\n");
        stringBuilder.append("\tli $t0, 1\n");
        stringBuilder.append(swT0ToReg(resultId, 0, resultReg));
        stringBuilder.append(aLabelName).append(":\n");
        return stringBuilder.toString();
    }

    private String translateCallInstr(CallInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<LLRegister> argumentRegArrayList = instruction.getArgumentRegArrayList();
        // deal $sp
        for (int i = 0; i < argumentRegArrayList.size(); i++) {
            if (argumentRegArrayList.get(i).getRegisterType() == RegisterType.NUM
                    || argumentRegArrayList.get(i).getRegisterType() == RegisterType.CHAR) {
                stringBuilder.append("\tli $t0, ").append(argumentRegArrayList.get(i).getRealValue()).append("\n");
                stringBuilder.append("\tsw $t0, " + (-4 * (mipsValueManager.getUsedWord() + i) - 8) + "($sp)\n");
                continue;
            }
            int localId = argumentRegArrayList.get(i).getId();
            int localOffset = mipsValueManager.getValueOffset(localId);
            if (!mipsValueManager.isGlobal(localId) && argumentRegArrayList.get(i).getRegisterType() == RegisterType.POINT) {
                if (mipsValueManager.isAddress(localId)) {
                    stringBuilder.append("\tlw $t0, ").append(mipsValueManager.getValueOffset(localId)).append("($sp)\n");
                } else {
                    stringBuilder.append("\taddi $t0, $sp, ").append(localOffset).append("\n");
                }
            } else if (argumentRegArrayList.get(i).getRegisterType() == RegisterType.GLOBAL) {
                LLRegister argumentReg = argumentRegArrayList.get(i);
                stringBuilder.append("\tla $t0, ").append(argumentReg.getName()).append("\n");
            } else {
                stringBuilder.append("\tlw $t0, " + mipsValueManager.getValueOffset(localId) + "($sp)\n");
            }
            // store argument
            stringBuilder.append("\tsw $t0, " + (-4 * (mipsValueManager.getUsedWord() + i) - 8) + "($sp)\n");
            // mipsValueManager.down4Address(i);
        }
        stringBuilder.append("\tsubu $sp, $sp, " + 4 * mipsValueManager.getUsedWord() + "\n");
        int maxNum = mipsValueManager.maxAddr();
        stringBuilder.append("\tjal ").append(instruction.getName()).append("_Ori\n");
        // back to here:
        stringBuilder.append("\taddu $sp, $sp, " + 4 * mipsValueManager.getUsedWord() + "\n");
        if (instruction.getLlRegister() != null) {
            int vId = instruction.getLlRegister().getId();
            if (!mipsValueManager.hasRecordId(vId)) {
                mipsValueManager.putNewValue(vId, 1, false);
            }
            stringBuilder.append("\tmove $t0, $v0\n");
            stringBuilder.append(swT0ToReg(vId, 0, instruction.getLlRegister()));
        }
        return stringBuilder.toString();
    }

    private String translateReturnInstr(ReturnInstr instruction) {
        if (funcName.equals("main")) {
            return "\tli $v0, 10\n" + "\tsyscall\n";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            if (instruction.getRetReg() != null) {
                if (instruction.getRetReg().getRegisterType() == RegisterType.NUM ||
                instruction.getRetReg().getRegisterType() == RegisterType.CHAR) {
                    stringBuilder.append("\tli $v0, " + instruction.getRetReg().getRealValue() + "\n");
                } else {
                    stringBuilder.append(lwToReg("$v0", "$t0", instruction.getRetReg(), 0));
                }
            }
            stringBuilder.append("\tlw $ra, 0($sp)\n").append("\taddu $sp, $sp, 4\n");
            stringBuilder.append("\tjr $ra\n");
            return stringBuilder.toString();
        }
    }

    private String translateGetCharInstr(GetCharInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tli $v0, 12\n").append("\tsyscall\n").append("\tmove $t0, $v0\n");
        int targetId = instruction.getRegister().getId();
        if (!mipsValueManager.hasRecordId(targetId)) {
            mipsValueManager.putNewValue(targetId, 1, false);
        }
        stringBuilder.append(swT0ToReg(targetId, 0, instruction.getRegister()));
        return stringBuilder.toString();
    }

    private String translateGetIntInstr(GetIntInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tli $v0, 5\n").append("\tsyscall\n").append("\tmove $t0, $v0\n");
        int targetId = instruction.getRegister().getId();
        if (!mipsValueManager.hasRecordId(targetId)) {
            mipsValueManager.putNewValue(targetId, 1, false);
        }
        stringBuilder.append(swT0ToReg(targetId, 0, instruction.getRegister()));
        return stringBuilder.toString();
    }

    private String translatePutChInstr(PutChInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lwToReg("$a0", "$t0", instruction.getRegister(), 0));
        stringBuilder.append("\tli $v0, 11\n").append("\tsyscall\n");
        return stringBuilder.toString();
    }

    private String translatePutIntInstr(PutIntInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lwToReg("$a0", "$t0", instruction.getRegister(), 0));
        stringBuilder.append("\tli $v0, 1\n").append("\tsyscall\n");
        return stringBuilder.toString();
    }

    private String translatePutStrInstr(PutStrInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        int stringId = instruction.getStringId();
        stringBuilder.append("\tli $v0, 4\n").append("\tla $a0, str_" + stringId +  "\n").append("\tsyscall\n");
        return stringBuilder.toString();
    }

    private String translateTruncInstr(TruncInstr instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        int retRegId = instruction.getRetReg().getId();
        if (!mipsValueManager.hasRecordId(retRegId)) {
            mipsValueManager.putNewValue(retRegId, 1, false);
        }
        stringBuilder.append(lwToReg("$t0", "$t1", instruction.getCutReg(), 0));
        stringBuilder.append("\tand $t0, $t0, 0xFF\n");
        stringBuilder.append(swT0ToReg(retRegId, 0, instruction.getRetReg()));
        return stringBuilder.toString();
    }

    private String translateZextInstr(ZextInstr instruction) {
        if (instruction.getExtRegister().getRegisterType() == RegisterType.NUM || instruction.getExtRegister().getRegisterType() == RegisterType.CHAR) {
            if (!mipsValueManager.hasRecordId(instruction.getRetRegister().getId())) {
                mipsValueManager.putNewValue(instruction.getRetRegister().getId(), 1, false);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\tli $t0, ").append(instruction.getExtRegister().getRealValue()).append("\n");
            stringBuilder.append(swT0ToReg( instruction.getRetRegister().getId(), 0, instruction.getRetRegister()));
            return stringBuilder.toString();
        }
        int retRegId = instruction.getRetRegister().getId();
        int extRegId = instruction.getExtRegister().getId();
        if (!mipsValueManager.hasRecordId(retRegId)) {
            mipsValueManager.putNewValue(retRegId, 1, false);
        }
        mipsValueManager.putOldValue(retRegId, extRegId);
        return null;
    }

    private String swT0ToReg(int nameId, int index, LLRegister targetReg) {
        StringBuilder stringBuilder = new StringBuilder();
        if (mipsValueManager.isGlobal(targetReg.getId())) {
            stringBuilder.append("\tlw $t1, ").append(mipsValueManager.getValueOffset(targetReg.getId())).append("($sp)\n");
            stringBuilder.append("\tsw $t0, 0($t1)\n");
            return stringBuilder.toString();
        }
        if (mipsValueManager.isAddress(nameId)) {
            stringBuilder.append("\tlw $t1, ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
            if (index == 0) {
                stringBuilder.append("\tsw $t0, ").append("0($t1)\n");
            } else {
                stringBuilder.append("\tsw $t0, ").append(-4 * index).append("($t1)\n");
            }
        } else if (targetReg.getRegisterType() == RegisterType.GLOBAL) {
            stringBuilder.append("\tla $t1, ").append(targetReg.getName()).append("\n");
            if (index == 0) {
                stringBuilder.append("\tsw $t0, ").append("0($t1)\n");
            } else {
                stringBuilder.append("\tsw $t0, ").append(-4 * index).append("($t1)\n");
            }
        } else {
            stringBuilder.append("\tsw $t0, ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
        }
        return stringBuilder.toString();
    }

    private String lwToReg(String lwRegName, String validRegName, LLRegister valueReg, int index) {
        if (valueReg.getRegisterType() == RegisterType.NUM || valueReg.getRegisterType() == RegisterType.CHAR) {
            return "\tli " + lwRegName + ", " + valueReg.getRealValue() + "\n";
        }
        int nameId = valueReg.getId();
        StringBuilder stringBuilder = new StringBuilder();
        if (mipsValueManager.isAddress(nameId)) {
            stringBuilder.append("\tlw ").append(validRegName).append(", ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
            if (index == 0) {
                stringBuilder.append("\tlw ").append(lwRegName).append(", ").append("0(" + validRegName + ")\n");
            } else {
                stringBuilder.append("\tlw ").append(lwRegName).append(", ").append(-4 * index).append("(" + validRegName +  ")\n");
            }
        } else if (valueReg.getRegisterType() == RegisterType.GLOBAL) {
            stringBuilder.append("\tla ").append(validRegName).append(", ").append(valueReg.getName()).append("\n");
            stringBuilder.append("\tlw ").append(lwRegName).append(", ").append(index).append("(").append(validRegName).append(")\n");
        } else {
            stringBuilder.append("\tlw ").append(lwRegName).append(", ").append(mipsValueManager.getValueOffset(nameId)).append("($sp)\n");
        }
        return stringBuilder.toString();
    }

    private int getIcmpCount() {
        return icmpCount++;
    }

    private void outputMipsCode() {
        try (FileWriter writer = new FileWriter("mips.txt", false)) {
            writer.write("");// 清空文件内容
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter writer = new FileWriter("mips.txt", true)) { // 追加模式
            writer.write(outputMips.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
