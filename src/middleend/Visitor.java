package middleend;

import entity.*;
import entity.decl.Decl;
import entity.decl.con.ConstDecl;
import entity.decl.con.ConstDef;
import entity.decl.con.ConstInitVal;
import entity.decl.var.InitVal;
import entity.decl.var.VarDecl;
import entity.decl.var.VarDef;
import entity.expression.*;
import entity.funcdef.FuncDef;
import entity.funcdef.FuncFParam;
import entity.stmtEntity.*;
import frontend.CompError;
import llvm.*;
import llvm.Module;
import llvm.instruction.*;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

public class Visitor {
    private CompUnit compUnit;
    private SymbolTable curSymbolTable;
    private ArrayList<SymbolTable> symbolTableArrayList;
    private CompError compError;
    private boolean output4Correct = true;
    private boolean output4Error = true;
    private int tableIdTop;
    private int countForCircle;
    private ArrayList<SymbolTable> outputSymbols;
    //middle code generate:
    private Module module;
    private ArrayList<String> outputLLVMCodes;
    private BasicBlock curBasicBlock;
    private int regIdCount;
    private boolean isGlobal;
    private int stringConstIdCount;

    public Visitor(CompUnit compUnit, CompError compError) {
        this.compUnit = compUnit;
        this.compError = compError;
        this.symbolTableArrayList = new ArrayList<>();
        this.tableIdTop = 0;
        this.countForCircle = 0;
        this.outputSymbols = new ArrayList<>();
        this.module = new Module();
        this.outputLLVMCodes = new ArrayList<>();
        this.regIdCount = 0;
        this.isGlobal = true;
        this.stringConstIdCount = 0;
    }


    public Module visit() {
        if (!compError.isEmpty()) {
            output4Correct = false;
        }
        visitCompUnit();
        if (output4Correct) {
            output4Error = false;
        }
        output2File4Symbol();
        output2File4LLVM();
        return this.module;
    }

    private void visitCompUnit() {
        pushNewTable();
        ArrayList<Decl> declArrayList = compUnit.getDeclArrayList();
        ArrayList<FuncDef> funcDefArrayList = compUnit.getFuncDefArrayList();
        for (Decl decl : declArrayList) {
            visitDecl(decl);
        }
        for (FuncDef funcDef : funcDefArrayList) {
            visitFuncDef(funcDef);
        }
        isGlobal = false;
        regIdCount = 0; // reset
        visitMainFuncDef(compUnit.getMainFuncDef());
        popCurTable();
    }

    private void visitDecl(Decl decl) {
        if (decl instanceof ConstDecl) {
            visitConstDecl((ConstDecl) decl);
        } else if (decl instanceof VarDecl) {
            visitVarDecl((VarDecl) decl);
        }
    }

    private void visitVarDecl(VarDecl decl) {
        BType bType = decl.getbType();
        ArrayList<VarDef> varDefArrayList = decl.getVarDefArrayList();
        for (VarDef varDef : varDefArrayList) {
            visitVarDef(varDef, bType);
        }
    }

    private void visitVarDef(VarDef varDef, BType bType) {
        String ident = varDef.getIdent();
        int identLine = varDef.getIdentLine();
        searchInCurTable(ident, identLine); // can deal define repeat error
        IdentType identType;
        if (varDef.isArray()) {
            identType = bType.setArray();
        } else {
            identType = bType.getIdentType();
        }
        Symbol symbol = new Symbol(symbolTableArrayList.indexOf(curSymbolTable) + 1, ident, identType, null);
        insertSymbol(symbol);
        // ^^^ insert symbol
        if (varDef.isArray()) {
            visitExp(varDef.getConstExp(), null);
        }
        if (varDef.getInitVal() != null) {
            if (isGlobal) {
                LLRegister register = visitInitVal(varDef.getInitVal());
                symbol.setValue(register.getRealValue());
                if (symbol.getIdentType() == IdentType.Char) {
                    LLRegister llRegister = new LLRegister(getRegId());
                    llRegister.setRegister(0, (char) register.getRealValue(),RetType.i8, RegisterType.GLOBAL);
                    symbol.setLlRegister(llRegister);
                    GlobalVariable globalVariable = new GlobalVariable(ident, 0, (char) register.getRealValue(), RetType.i8);
                    module.insertGlobalValue(globalVariable);
                } else {
                    LLRegister llRegister = new LLRegister(getRegId());
                    llRegister.setRegister(register.getRealValue(), '0', RetType.i32, RegisterType.GLOBAL);
                    symbol.setLlRegister(llRegister);
                    GlobalVariable globalVariable = new GlobalVariable(ident, register.getRealValue(), '0', RetType.i32);
                    module.insertGlobalValue(globalVariable);
                }
            } else {
                LLRegister targetRegister = new LLRegister(getRegId());
                RetType targetType = getValueRetType(bType.getIdentType());
                targetRegister.setRegister(0, '0', targetType, RegisterType.POINT);
                curBasicBlock.insertInstr(new AllocaInstr(targetRegister));
                LLRegister saveRegister = visitInitVal(varDef.getInitVal());
                RetType getType = saveRegister.getValueType();
                if (targetType == RetType.i32 && getType == RetType.i8) {
                    LLRegister register = new LLRegister(getRegId());
                    register.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
                    curBasicBlock.insertInstr(new ZextInstr(register, saveRegister));
                    curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
                    symbol.setValue(register.getRealValue());
                    symbol.setLlRegister(targetRegister);
                } else if (targetType == RetType.i8 && getType == RetType.i32) {
                    LLRegister register = new LLRegister(getRegId());
                    register.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
                    curBasicBlock.insertInstr(new TruncInstr(register, saveRegister));
                    curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
                    symbol.setValue(register.getRealValue());
                    symbol.setLlRegister(targetRegister);
                } else {
                    symbol.setValue(saveRegister.getRealValue());
                    symbol.setLlRegister(targetRegister);
                    curBasicBlock.insertInstr(new StoreInstr(saveRegister, targetRegister));
                }
            }
        } else {
            LLRegister register = new LLRegister(getRegId());
            register.setRegister(0, '0', getValueRetType(bType.getIdentType()), RegisterType.POINT);
            symbol.setValue(register.getRealValue());
            symbol.setLlRegister(register);
            curBasicBlock.insertInstr(new AllocaInstr(register));
        }
    }

    private LLRegister visitInitVal(InitVal initVal) {
        LLRegister saveRegister = new LLRegister(-1);
        if (initVal.isString()) {

        } else {
            ArrayList<Exp> expArrayList = initVal.getExpArrayList();
            for (Exp exp : expArrayList) {
                visitExp(exp, saveRegister);
            }
        }
        return saveRegister;
    }

    private void visitConstDecl(ConstDecl decl) {
        BType bType = decl.getbType();
        ArrayList<ConstDef> constDefArrayList = decl.getConstDefArrayList();
        for (ConstDef constDef : constDefArrayList) {
            visitConstDef(constDef, bType);
        }
    }

    private void visitConstDef(ConstDef constDef, BType bType) {
        String ident = constDef.getIdent();
        int identLine = constDef.getIdentLine();
        searchInCurTable(ident, identLine); // can deal define repeat error
        IdentType identType;
        if (constDef.isArray()) {
            identType = bType.setArray();
        } else {
            identType = bType.getIdentType();
        }
        Symbol symbol = new Symbol(symbolTableArrayList.indexOf(curSymbolTable) + 1, ident, identType, null);
        insertSymbol(symbol);
        // ^^^ insert symbol
        if (constDef.isArray()) {
            visitExp(constDef.getConstExp(), null);
        }
        LLRegister register = visitConstInitVal(constDef.getConstInitVal());
        if (symbol.getIdentType() == IdentType.Char) {
            LLRegister llRegister = new LLRegister(getRegId());
            llRegister.setRegister(0, (char) register.getRealValue(),RetType.i8, RegisterType.GLOBAL);
            symbol.setLlRegister(llRegister);
            GlobalVariable globalVariable = new GlobalVariable(ident, 0, (char) register.getRealValue(), RetType.i8);
            module.insertGlobalValue(globalVariable);
        } else {
            LLRegister llRegister = new LLRegister(getRegId());
            llRegister.setRegister(register.getRealValue(), '0', RetType.i32, RegisterType.GLOBAL);
            symbol.setLlRegister(llRegister);
            GlobalVariable globalVariable = new GlobalVariable(ident, register.getRealValue(), '0', RetType.i32);
            module.insertGlobalValue(globalVariable);
        }
    }

    private LLRegister visitConstInitVal(ConstInitVal constInitVal) {
        LLRegister saveRegister = new LLRegister(-1);
        if (constInitVal.isString()) {

        } else {
            ArrayList<Exp> expArrayList = constInitVal.getExpArrayList();
            for (Exp exp : expArrayList) {
                visitExp(exp, saveRegister);
            }
        }
        return saveRegister;
    }

    private void visitFuncDef(FuncDef funcDef) {
        String ident = funcDef.getIdent();
        int identLine = funcDef.getIdentLine();
        searchInCurTable(ident, identLine); // can deal define repeat error
        IdentType identType = funcDef.getFuncType().getIdentType();
        Function function = new Function(ident, getFuncRetType(identType));
        int paramNum = funcDef.getFuncFParamArrayList().size();
        ArrayList<IdentType> paramTypes = funcDef.getParamIdentTypes();
        FuncInfo funcInfo = new FuncInfo(paramNum, paramTypes, function);
        Symbol symbol = new Symbol(symbolTableArrayList.indexOf(curSymbolTable) + 1, ident, identType, funcInfo);
        insertSymbol(symbol);
        module.getFunctionArrayList().add(function);
        // ^^^ insert symbol
        regIdCount = -1;
        pushNewTable();
        BasicBlock basicBlock = new BasicBlock();
        curBasicBlock = basicBlock;
        function.insertBasicBlock(basicBlock);
        ArrayList<FuncFParam> funcFParamArrayList = funcDef.getFuncFParamArrayList();
        for (FuncFParam funcFParam : funcFParamArrayList) {
            LLRegister argumentReg = new LLRegister(getRegId());
            RetType retType = getValueRetType(funcFParam.getbType().getIdentType());
            argumentReg.setRegister(0, '0', retType, RegisterType.TEMP);
            function.insertArgument(new Argument(argumentReg.getId(), retType, argumentReg));
        }
        getRegId(); // set for BasicBlock
        for (int i = 0; i < funcFParamArrayList.size(); i++) {
            visitFuncFParam(funcFParamArrayList.get(i), function.getArgumentArrayList().get(i));
        }

        visitBlock(funcDef.getBlock(), identType, true);
        popCurTable();
    }

    private void visitFuncFParam(FuncFParam funcFParam, Argument argument) {
        String ident = funcFParam.getIdent();
        int identLine = funcFParam.getIdentLine();
        searchInCurTable(ident, identLine);
        RetType argueRetType = getValueRetType(funcFParam.getbType().getIdentType());
        Symbol argueSymbol = new Symbol(symbolTableArrayList.indexOf(curSymbolTable) + 1,
                funcFParam.getIdent(), funcFParam.getbType().getIdentType(), null);
        //
        LLRegister pointRegister = new LLRegister(getRegId());
        pointRegister.setRegister(0, '0', argueRetType, RegisterType.POINT);
        curBasicBlock.insertInstr(new AllocaInstr(pointRegister));
        // define pointReg to store argument value
        curBasicBlock.insertInstr(new StoreInstr(argument.getArgumentReg(), pointRegister));
        // insert targetReg to use Argument
        argueSymbol.setLlRegister(pointRegister);
        insertSymbol(argueSymbol);
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        curBasicBlock = new BasicBlock();
        regIdCount = 0; // reset idCount
        module.getMainFunction().insertBasicBlock(curBasicBlock);
        pushNewTable();
        visitBlock(mainFuncDef.getBlock(), IdentType.IntFunc, true);
        popCurTable();
    }

    private void visitBlock(Block block, IdentType identType, boolean needReturn) {
        ArrayList<BlockItem> blockItemArrayList = block.getBlockItemArrayList();
        for (BlockItem blockItem : blockItemArrayList) {
            visitBlockItem(blockItem, identType);
        }
        if (needReturn && identType != IdentType.VoidFunc) {
            if (blockItemArrayList.isEmpty()) {
                dealError('g', block.getrBraceLine());
            } else {
                BlockItem finalItem = blockItemArrayList.get(blockItemArrayList.size() - 1);
                if (!(finalItem instanceof Stmt_RETURN)) {
                    dealError('g', block.getrBraceLine());
                }
            }
        }
    }

    private void visitBlockItem(BlockItem blockItem, IdentType identType) {
        if (blockItem instanceof Decl) {
            visitDecl((Decl) blockItem);
        } else { // it's stmt
            visitStmt((Stmt) blockItem, identType);
        }
    }

    private void visitStmt(Stmt blockItem, IdentType identType) {
        if (blockItem instanceof Stmt_Assign) {
            visitStmt_Assign((Stmt_Assign) blockItem);
        } else if (blockItem instanceof Stmt_Exp) {
            visitStmt_Exp((Stmt_Exp) blockItem);
        } else if (blockItem instanceof Block) {
            pushNewTable();
            visitBlock((Block) blockItem, identType, false);
            popCurTable();
        } else if (blockItem instanceof Stmt_IF) {
            visitStmt_IF((Stmt_IF) blockItem, identType);
        } else if (blockItem instanceof Stmt_FOR) {
            visitStmt_FOR((Stmt_FOR) blockItem, identType);
        } else if (blockItem instanceof Stmt_BREAK) {
            visitStmt_BREAK(((Stmt_BREAK) blockItem).getBreakLine());
        } else if (blockItem instanceof Stmt_CONTINUE) {
            visitStmt_CONTINUE(((Stmt_CONTINUE) blockItem).getContinueLine());
        } else if (blockItem instanceof Stmt_RETURN) {
            visitStmt_RETURN((Stmt_RETURN) blockItem, identType);
        } else if (blockItem instanceof Stmt_GetInt) {
            visitStmt_GetInt((Stmt_GetInt) blockItem);
        } else if (blockItem instanceof Stmt_GetChar) {
            visitStmt_GetChar((Stmt_GetChar) blockItem);
        } else if (blockItem instanceof Stmt_Printf) {
            visitStmt_Printf((Stmt_Printf) blockItem);
        }
    }

    private void visitStmt_Assign(Stmt_Assign blockItem) {
        LLRegister primaryRegister = new LLRegister(-1);
        // middle:vvv
        String ident = blockItem.getlVal().getIdent();
        int identLine = blockItem.getlVal().getIdentLine();
        Symbol symbol = searchHasDefine(ident, identLine);// get symbol
        LLRegister targetRegister = symbol.getLlRegister();
        // alloca new point Reg
        visitLVal(blockItem.getlVal(), true, primaryRegister, false);
        LLRegister expRegister = new LLRegister(-1);
        visitExp(blockItem.getExp(), expRegister);
        // get save Reg
        symbol.setValue(expRegister.getRealValue());
        if (targetRegister.getValueType() == RetType.i32 && expRegister.getValueType() == RetType.i8) {
            LLRegister register = new LLRegister(getRegId());
            register.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
            curBasicBlock.insertInstr(new ZextInstr(register, expRegister));
            curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
        } else if (targetRegister.getValueType() == RetType.i8 && expRegister.getValueType() == RetType.i32) {
            LLRegister register = new LLRegister(getRegId());
            register.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
            curBasicBlock.insertInstr(new TruncInstr(register, expRegister));
            curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
        } else {
            curBasicBlock.insertInstr(new StoreInstr(expRegister, targetRegister));
        }
    }

    private void visitStmt_Exp(Stmt_Exp blockItem) {
        if (blockItem.isHasExp()) {
            LLRegister expRegister = new LLRegister(-1);
            visitExp(blockItem.getExp(), expRegister);
        }
    }

    private void visitStmt_IF(Stmt_IF blockItem, IdentType identType) {
        LOrExp cond = blockItem.getCond();
        visitLOrExp(cond);
        Stmt stmtIf = blockItem.getStmtIf();
        visitStmt(stmtIf, identType);
        if (blockItem.hasElse()) {
            Stmt stmtElse = blockItem.getStmtElse();
            visitStmt(stmtElse, identType);
        }
    }

    private void visitStmt_FOR(Stmt_FOR blockItem, IdentType identType) {
        if (blockItem.hasInit()) {
            visitStmt_Assign(blockItem.getStmtForInit());
        }
        if (blockItem.hasCond()) {
            visitLOrExp(blockItem.getCond());
        }
        if (blockItem.hasAdd()) {
            visitStmt_Assign(blockItem.getStmtForAdd());
        }
        this.countForCircle++;
        visitStmt(blockItem.getStmtDo(), identType);
        this.countForCircle--;
    }

    private void visitStmt_BREAK(int callLine) {
        if (countForCircle <= 0) {
            dealError('m', callLine);
        }
    }

    private void visitStmt_CONTINUE(int callLine) {
        if (countForCircle <= 0) {
            dealError('m', callLine);
        }
    }

    private void visitStmt_RETURN(Stmt_RETURN blockItem, IdentType identType) {
        LLRegister expRegister = new LLRegister(-1);
        RetType retType = getFuncRetType(identType);
        if (blockItem.hasReturnValue()) {
            if (identType == IdentType.VoidFunc) {
                dealError('f', blockItem.getReturnLine());
            }
            visitExp(blockItem.getExp(), expRegister);
            if (expRegister.getValueType() == RetType.i32 && retType == RetType.i8) {
                LLRegister register = new LLRegister(getRegId());
                register.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
                curBasicBlock.insertInstr(new TruncInstr(register, expRegister));
                curBasicBlock.insertInstr(new ReturnInstr(register, retType));
            } else if (expRegister.getValueType() == RetType.i8 && retType == RetType.i32) {
                LLRegister register = new LLRegister(getRegId());
                register.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
                curBasicBlock.insertInstr(new ZextInstr(register, expRegister));
                curBasicBlock.insertInstr(new ReturnInstr(register, retType));
            } else {
                curBasicBlock.insertInstr(new ReturnInstr(expRegister, retType));
            }
        } else {
            curBasicBlock.insertInstr(new ReturnInstr(expRegister, retType));
        }
    }

    private void visitStmt_GetInt(Stmt_GetInt blockItem) {
        LLRegister primaryRegister = new LLRegister(-1);
        // middle:vvv
        String ident = blockItem.getlVal().getIdent();
        int identLine = blockItem.getlVal().getIdentLine();
        Symbol symbol = searchHasDefine(ident, identLine);// get symbol
        LLRegister targetRegister = symbol.getLlRegister();
        // alloca new point Reg
        visitLVal(blockItem.getlVal(), true, primaryRegister, false);
        LLRegister saveRegister = new LLRegister(getRegId());
        saveRegister.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
        curBasicBlock.insertInstr(new GetIntInstr(saveRegister));
        // get save Reg
        symbol.setValue(saveRegister.getRealValue());

        if (targetRegister.getValueType() == RetType.i32 && saveRegister.getValueType() == RetType.i8) {
            LLRegister register = new LLRegister(getRegId());
            register.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
            curBasicBlock.insertInstr(new ZextInstr(register, saveRegister));
            curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
        } else if (targetRegister.getValueType() == RetType.i8 && saveRegister.getValueType() == RetType.i32) {
            LLRegister register = new LLRegister(getRegId());
            register.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
            curBasicBlock.insertInstr(new TruncInstr(register, saveRegister));
            curBasicBlock.insertInstr(new StoreInstr(register, targetRegister));
        } else {
            curBasicBlock.insertInstr(new StoreInstr(saveRegister, targetRegister));
        }
    }

    private void visitStmt_GetChar(Stmt_GetChar blockItem) {
        LLRegister primaryRegister = new LLRegister(-1);
        // middle:vvv
        String ident = blockItem.getlVal().getIdent();
        int identLine = blockItem.getlVal().getIdentLine();
        Symbol symbol = searchHasDefine(ident, identLine);// get symbol
        LLRegister targetRegister = symbol.getLlRegister();
        // alloca new point Reg
        visitLVal(blockItem.getlVal(), true, primaryRegister, false); // visit LVal
        LLRegister saveRegister = new LLRegister(getRegId());
        saveRegister.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
        curBasicBlock.insertInstr(new GetCharInstr(saveRegister));

        if (targetRegister.getValueType() == RetType.i32) {
            curBasicBlock.insertInstr(new StoreInstr(saveRegister, targetRegister));
        } else {
            // get save Reg
            LLRegister cutRegister = new LLRegister(getRegId());
            cutRegister.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
            curBasicBlock.insertInstr(new TruncInstr(cutRegister, saveRegister));
            // cut the saveReg
            symbol.setValue(cutRegister.getRealValue());
            curBasicBlock.insertInstr(new StoreInstr(cutRegister, targetRegister));
        }
    }

    private void visitStmt_Printf(Stmt_Printf blockItem) {
        ArrayList<String> stringPieces = new ArrayList<>();
        ArrayList<Boolean> mark = new ArrayList<>();
        ArrayList<StringConstValue> stringConstValueArrayList = new ArrayList<>();
        ArrayList<RetType> describeRetTypes = new ArrayList<>();
        int expressionNum = countPrintfChar(blockItem.getStringConst(), stringPieces, mark, describeRetTypes);
        for (String string : stringPieces) {
            StringConstValue stringConstValue = new StringConstValue(getStringConstId(),
                    string.replace("\\n", "\\0A"), string.replace("\\n", "\n").length() + 1);
            module.getStringConstValueArrayList().add(stringConstValue);
            stringConstValueArrayList.add(stringConstValue);
        } // define String Const
        ArrayList<LLRegister> registerArrayList = new ArrayList<>();
        int getNum = 0;
        if (blockItem.hasDescribe()) {
            ArrayList<Exp> expArrayList = blockItem.getExpArrayList();
            for (Exp exp : expArrayList) {
                getNum++;
                LLRegister expRegister = new LLRegister(-1);
                visitExp(exp, expRegister);
                registerArrayList.add(expRegister);
            }
        }
        if (expressionNum != getNum) {
            dealError('l', blockItem.getPrintfLine());
            return;
        }
        // middle:
        if (blockItem.hasDescribe()) {
            int indexOfStr = 0;
            int indexOfReg = 0;
            int count = -1;
            for (boolean isReg : mark) {
                if (isReg) {
                    count++;
                    LLRegister register = registerArrayList.get(indexOfReg);
                    if (describeRetTypes.get(count) == RetType.i32) {
                        if (register.getValueType() == RetType.i8) {
                            LLRegister registerAdapt = new LLRegister(getRegId());
                            registerAdapt.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
                            curBasicBlock.insertInstr(new ZextInstr(registerAdapt, register));
                            curBasicBlock.insertInstr(new PutIntInstr(registerAdapt));
                        } else {
                            curBasicBlock.insertInstr(new PutIntInstr(register));
                        }
                    } else {
                        if (register.getValueType() == RetType.i8) {
                            LLRegister registerAdapt = new LLRegister(getRegId());
                            registerAdapt.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
                            curBasicBlock.insertInstr(new ZextInstr(registerAdapt, register));
                            curBasicBlock.insertInstr(new PutChInstr(registerAdapt));
                        } else {
                            curBasicBlock.insertInstr(new PutChInstr(register));
                        }
                    }
                    indexOfReg++;
                } else {
                    StringConstValue stringConstValue = stringConstValueArrayList.get(indexOfStr);
                    curBasicBlock.insertInstr(new PutStrInstr(stringConstValue));
                    indexOfStr++;
                }
            }
        } else {
            StringConstValue stringConstValue = stringConstValueArrayList.get(0);
            curBasicBlock.insertInstr(new PutStrInstr(stringConstValue));
        }
    }

    private IdentType visitLVal(LVal lVal, boolean needCheckConst, LLRegister lValRegister, boolean needGetValue) {
        String ident = lVal.getIdent();
        int identLine = lVal.getIdentLine();
        Symbol symbol = searchHasDefine(ident, identLine);
        if (symbol == null) {
            return IdentType.Char;
        }
        IdentType identType = symbol.getIdentType();
        if (needCheckConst) {
            if (symbol.isConst()) {
                dealError('h', identLine);
            }
        }
        if (lVal.isArray()) {
            if (lVal.getExp() != null) {
                LLRegister expRegister = null;
                visitExp(lVal.getExp(), expRegister);
                identType = goBasic(identType);
            }
        }
        //
        LLRegister llRegister = symbol.getLlRegister();
        if (needGetValue) {
            LLRegister saveRegister = new LLRegister(getRegId());
            saveRegister.assignByReg(llRegister); // careful not assign RegType
            saveRegister.setRegisterType(RegisterType.TEMP);
            lValRegister.setByReg(saveRegister);
            curBasicBlock.insertInstr(new LoadInstr(saveRegister, llRegister, InstructionType.LOAD));
        } else {
            lValRegister.setByReg(llRegister);
        }
        return identType;
    }

    private IdentType visitExp(Exp exp, LLRegister expRegister) {
        LLRegister mulRegisterLeft = new LLRegister(-1);
        LLRegister mulRegisterRight = new LLRegister(-1);
        LLRegister saveRegister = new LLRegister(-1);
        int count = 0;
        //
        IdentType identType = IdentType.Char;
        ArrayList<MulExp> mulExpArrayList = exp.getMulExpArrayList();
        ArrayList<Operation> operationArrayList = exp.getOperationArrayList();
        //
        for (MulExp mulExp : mulExpArrayList) {
            mulRegisterRight = new LLRegister(-1);
            IdentType identTypeTemp = visitMulExp(mulExp, identType, mulRegisterRight);
            identType = adaptIdentType(identType, identTypeTemp);
            //
            count++;
            if (count == 1) { // only 1 then just assign
                saveRegister.setByReg(mulRegisterRight);
                mulRegisterLeft = mulRegisterRight;
            } else if (count > 1) { // insert new instr when get 2 more
                InstructionType instructionType = getInstrType(operationArrayList.get(count - 2));
                if (isAllNumber(mulRegisterLeft, mulRegisterRight)) {
                    mulRegisterRight = combineNumber(mulRegisterLeft, mulRegisterRight, instructionType);
                    // like first visit:
                    saveRegister.setByReg(mulRegisterRight);
                    mulRegisterLeft = mulRegisterRight;
                } else {
                    LLRegister newRegister = new LLRegister(getRegId());
                    newRegister.setRegister(0, '0', getValueRetType(identType), RegisterType.TEMP);
                    curBasicBlock.insertInstr(new BinaryInstr(newRegister, mulRegisterLeft, mulRegisterRight, instructionType));
                    mulRegisterLeft = newRegister;
                    saveRegister.setByReg(newRegister);
                }
            }
        }
        expRegister.setByReg(saveRegister);
        return identType;
    }

    private IdentType visitMulExp(MulExp mulExp, IdentType identType, LLRegister mulRegister) {
        LLRegister unaryRegisterLeft = new LLRegister(-1);
        LLRegister unaryRegisterRight = new LLRegister(-1);
        LLRegister saveRegister = new LLRegister(-1);
        int count = 0;
        //
        IdentType identTypeReturn = identType;
        ArrayList<UnaryExp> unaryExpArrayList = mulExp.getUnaryExpArrayList();
        ArrayList<Operation> operationArrayList = mulExp.getOperationArrayList();
        //
        for (UnaryExp unaryExp : unaryExpArrayList) {
            unaryRegisterRight = new LLRegister(-1);
            IdentType identTypeTemp = visitUnaryExp(unaryExp, identTypeReturn, unaryRegisterRight);
            identTypeReturn = adaptIdentType(identTypeReturn, identTypeTemp);
            //
            count++;
            if (count == 1) { // only 1 then just assign
                saveRegister.setByReg(unaryRegisterRight);
                unaryRegisterLeft = unaryRegisterRight;
            } else if (count > 1) { // insert new instr when get 2 more
                InstructionType instructionType = getInstrType(operationArrayList.get(count - 2));
                if (isAllNumber(unaryRegisterLeft, unaryRegisterRight)) {
                    unaryRegisterRight = combineNumber(unaryRegisterLeft, unaryRegisterRight, instructionType);
                    // like first visit:
                    saveRegister.setByReg(unaryRegisterRight);
                    unaryRegisterLeft = unaryRegisterRight;
                } else {
                    LLRegister newRegister = new LLRegister(getRegId());
                    newRegister.setRegister(0, '0', getValueRetType(identTypeReturn), RegisterType.TEMP);
                    RetType retType = getValueRetType(identTypeReturn);
                    curBasicBlock.insertInstr(new BinaryInstr(newRegister, unaryRegisterLeft, unaryRegisterRight, instructionType));
                    unaryRegisterLeft = newRegister;
                    saveRegister.setByReg(newRegister);
                }
            }
        }
        mulRegister.setByReg(saveRegister);
        return identTypeReturn;
    }

    private IdentType visitUnaryExp(UnaryExp unaryExp, IdentType identTypeReturn, LLRegister unaryRegister) {
        ArrayList<Operation> operationArrayList = unaryExp.getOperationArrayList();
        Operation operation = simplyfyUnaryOp(operationArrayList); // this maybe Empty
        LLRegister primaryRegister = new LLRegister(-1);
        //
        if (unaryExp.isFuncCall()) {
            IdentType identType = null;
            String ident = unaryExp.getIdent();
            int identLine = unaryExp.getIdentLine();
            Symbol funcSymbol = searchHasDefine(ident, identLine);
            if (funcSymbol == null) {
                return IdentType.Char;
            }
            FuncInfo expectedFuncInfo = funcSymbol.getFuncInfo(); // expected
            ArrayList<IdentType> funcParamArrayList = expectedFuncInfo.getParamTypes();
            ArrayList<LLRegister> argumentRegList = new ArrayList<>();//---
            ArrayList<Exp> funcRParams = unaryExp.getFuncRParams();
            int paramNumGet = funcRParams.size();
            if (funcSymbol.getIdentType() == IdentType.IntFunc) {
                identType = IdentType.Int;
            } else if (funcSymbol.getIdentType() == IdentType.CharFunc) {
                identType = IdentType.Char;
            } else {
                identType =IdentType.Char;
            }
            ArrayList<IdentType> paramTypesGet = new ArrayList<>();
            for (int i = 0; i < funcRParams.size(); i++) {
                LLRegister expRegister = new LLRegister(-1); //---
                IdentType expIdentType = visitExp(funcRParams.get(i), expRegister);
                paramTypesGet.add(expIdentType);
                RetType expRetType = getValueRetType(expIdentType);
                RetType paramRetType = getValueRetType(funcParamArrayList.get(i));
                // adapt type
                if (paramRetType == RetType.i32 && expRetType == RetType.i8) {
                    LLRegister register = new LLRegister(getRegId());
                    register.setRegister(0, '0', RetType.i32, RegisterType.TEMP);
                    curBasicBlock.insertInstr(new ZextInstr(register, expRegister));
                    argumentRegList.add(register);
                } else if (paramRetType == RetType.i8 && expRetType == RetType.i32) {
                    LLRegister register = new LLRegister(getRegId());
                    register.setRegister(0, '0', RetType.i8, RegisterType.TEMP);
                    curBasicBlock.insertInstr(new TruncInstr(register, expRegister));
                    argumentRegList.add(register);
                } else {
                    argumentRegList.add(expRegister); //---
                }
            } // visit and get identType ArrayList
            if (expectedFuncInfo.getParamNum() != paramNumGet) {
                dealError('d', identLine);
                return IdentType.Char;
            } else if (!expectedFuncInfo.checkCons(new FuncInfo(paramNumGet, paramTypesGet, null))) {
                dealError('e', identLine);
                return IdentType.Char;
            } // deal error
            RetType retType = getFuncRetType(funcSymbol.getIdentType());
            LLRegister llRegister = null;
            if (retType != RetType.VOID) {
                llRegister = new LLRegister(getRegId());
                llRegister.setRegister(0, '0', retType, RegisterType.TEMP);
                unaryRegister.setByReg(llRegister);
            }
            curBasicBlock.insertInstr(new CallInstr(retType,llRegister , ident,argumentRegList));
            return adaptIdentType(identType, identTypeReturn);
        } else {
            LLRegister saveRegister = new LLRegister(-1);
            IdentType identType = visitPrimaryExp(unaryExp.getPrimaryExp(), primaryRegister);
            saveRegister.setByReg(primaryRegister);
            if (operation == Operation.SUB) {
                if (isNumber(primaryRegister)) { // try calculate:
                    primaryRegister.doNot();
                    saveRegister.setByReg(primaryRegister);
                } else {
                    LLRegister newRegister = new LLRegister(getRegId());
                    newRegister.setRegister(0, '0' , RetType.i32, RegisterType.TEMP);
                    LLRegister numRegister = new LLRegister(-1);
                    numRegister.setRegister(0, '0', RetType.i32, RegisterType.NUM);
                    curBasicBlock.insertInstr(new BinaryInstr(newRegister, numRegister, primaryRegister, InstructionType.sub));
                    saveRegister.setByReg(newRegister);
                }
            }
            unaryRegister.setByReg(saveRegister);
            return adaptIdentType(identType, identTypeReturn);
        }
    }

    private IdentType visitPrimaryExp(PrimaryExp primaryExp, LLRegister primaryRegister) {
        PriExpType priExpType = primaryExp.getPriExpType();
        if (priExpType == PriExpType.EXP) {
            return visitExp(primaryExp.getExp(), primaryRegister);
        } else if (priExpType == PriExpType.LVAL) {
            return visitLVal(primaryExp.getlVal(), false, primaryRegister, true);
        } else if (priExpType == PriExpType.NUM) {
            LLRegister saveRegister = new LLRegister(-1);
            saveRegister.setRegister(primaryExp.getNumber(), '0', RetType.i32, RegisterType.NUM);
            primaryRegister.setByReg(saveRegister);
            return IdentType.Int;
        } else if (priExpType == PriExpType.CHAR) {
            primaryRegister.setRegister(0, primaryExp.getCharacter(), RetType.i8, RegisterType.CHAR);
            return IdentType.Char;
        }
        return null;
    }

    private void visitLOrExp(LOrExp cond) {
        ArrayList<LAndExp> lAndExpArrayList = cond.getlAndExpArrayList();
        for (LAndExp lAndExp : lAndExpArrayList) {
            visitLAndExp(lAndExp);
        }
    }

    private void visitLAndExp(LAndExp lAndExp) {
        ArrayList<EqExp> eqExpArrayList = lAndExp.getExpArrayList();
        for (EqExp eqExp : eqExpArrayList) {
            visitEqExp(eqExp);
        }
    }

    private void visitEqExp(EqExp eqExp) {
        ArrayList<RelExp> relExpArrayList = eqExp.getRelExpArrayList();
        for (RelExp relExp : relExpArrayList) {
            visitRelExp(relExp);
        }
    }

    private void visitRelExp(RelExp relExp) {
        ArrayList<Exp> expArrayList = relExp.getExpArrayList();
        for (Exp exp : expArrayList) {
            LLRegister expRegister = null;
            visitExp(exp, expRegister);
        }
    }

    private void pushNewTable() {
        tableIdTop++;
        SymbolTable symbolTable = new SymbolTable(tableIdTop);
        this.symbolTableArrayList.add(symbolTable);
        this.curSymbolTable = symbolTable;
        outputSymbols.add(symbolTable);
    }

    private void popCurTable() {
        this.symbolTableArrayList.remove(symbolTableArrayList.size() - 1);
        if (!symbolTableArrayList.isEmpty()) {
            this.curSymbolTable = this.symbolTableArrayList.get(symbolTableArrayList.size() - 1);
        }
    }

    private void insertSymbol(Symbol symbol) {
        curSymbolTable.getSymbolArrayList().add(symbol);
    }

    private void searchInCurTable(String ident, int identLine) {
        boolean hasFind = false;
        for (Symbol symbol : curSymbolTable.getSymbolArrayList()) {
            if (symbol.getName().equals(ident)) {
                hasFind = true;
            }
        }
        if (hasFind) {
            dealError('b', identLine);
        }
    }

    private Symbol searchHasDefine(String ident, int identLine) {
        Symbol symbolGet = null;
        boolean hasFind = false;
        for (int i = symbolTableArrayList.size() - 1; i >= 0 ; i--) {
            if (!hasFind) {
                ArrayList<Symbol> symbolArrayList = symbolTableArrayList.get(i).getSymbolArrayList();
                for (Symbol symbol : symbolArrayList) {
                    if (symbol.getName().equals(ident)) {
                        symbolGet = symbol;
                        hasFind = true;
                        break;
                    }
                }
            }
        }
        if (!hasFind) {
            dealError('c', identLine);
        }
        return symbolGet;
    }

    private IdentType adaptIdentType(IdentType identTypeA, IdentType identTypeB) {
        if (isBasicType(identTypeA) && isArrayType(identTypeB)) {
            return identTypeB;
        }
        if (isArrayType(identTypeA) && isBasicType(identTypeB)) {
            return identTypeA;
        }
        if (isArrayType(identTypeA) && isArrayType(identTypeB)) {
            if (isConstType(identTypeA)) {
                return identTypeB;
            } else {
                return identTypeA;
            }
        }
        if (isBasicType(identTypeA) && isBasicType(identTypeB)) {
            if (identTypeA == IdentType.Int || identTypeA == IdentType.ConstInt ||
                    identTypeB == IdentType.Int || identTypeB == IdentType.ConstInt) {
                return IdentType.Int;
            }
            return IdentType.Char;
        }
        return null;
    }

    private boolean isBasicType(IdentType identType) {
        return identType == IdentType.Char || identType == IdentType.Int ||
                identType == IdentType.ConstChar || identType == IdentType.ConstInt;
    }

    private boolean isArrayType(IdentType identType) {
        return identType == IdentType.CharArray || identType == IdentType.IntArray ||
                identType == IdentType.ConstCharArray || identType == IdentType.ConstIntArray;
    }

    private boolean isConstType(IdentType identType) {
        if (identType == IdentType.ConstChar || identType == IdentType.ConstInt
                || identType == IdentType.ConstCharArray || identType == IdentType.ConstIntArray) {
            return true;
        }
        return false;
    }

    private IdentType goBasic(IdentType identType) {
        switch (identType) {
            case ConstCharArray:
                return IdentType.ConstChar;
            case ConstIntArray:
                return IdentType.ConstInt;
            case CharArray:
                return IdentType.Char;
            case IntArray:
                return IdentType.Int;
            default:
                return identType; // 如果不是数组类型，直接返回原类型
        }
    }

    private int countPrintfChar(String name, ArrayList<String> stringPieces, ArrayList<Boolean> mark, ArrayList<RetType> describeRetTypes) {
        int count = 0;
        int preI = 0;
        boolean isKey = false;
        name = name.substring(1, name.length() - 1);
        for (int i = 0; i < name.length(); i++) {
            char currentChar = name.charAt(i);
            if (currentChar == '%') {
                isKey = true;
            } else if (isKey && (currentChar == 'd' || currentChar == 'c')) {
                if (currentChar == 'd') {
                    describeRetTypes.add(RetType.i32);
                } else {
                    describeRetTypes.add(RetType.i8);
                }
                if (i != 1) {
                    stringPieces.add(name.substring(preI, i - 1));
                    mark.add(false);
                }
                count++;
                isKey = false;
                mark.add(true);
                preI = i + 1;
            } else {
                isKey = false;
            }
        }
        if (preI != name.length()) {
            stringPieces.add(name.substring(preI));
            mark.add(false);
        }
        return count;
    }

    private RetType getFuncRetType(IdentType identType) {
        if (identType == IdentType.IntFunc) {
            return RetType.i32;
        } else if (identType == IdentType.CharFunc) {
            return RetType.i8;
        } else {
            return RetType.VOID;
        }
    }

    private RetType getValueRetType(IdentType identType) {
        if (identType == IdentType.Int || identType == IdentType.ConstInt) {
            return RetType.i32;
        } else {
            return RetType.i8;
        }
    }

    private InstructionType getInstrType(Operation operation) {
        if (operation == Operation.ADD) {
            return InstructionType.add;
        } else if (operation == Operation.SUB) {
            return InstructionType.sub;
        } else if (operation == Operation.MUL) {
            return InstructionType.mul;
        } else if (operation == Operation.DIV) {
            return InstructionType.sdiv;
        } else if (operation == Operation.MOD) {
            return InstructionType.srem;
        }
        return null;
    }

    private Operation simplyfyUnaryOp(ArrayList<Operation> operationArrayList) {
        boolean hasNot = false;
        boolean isSub = false;
        for (Operation operation : operationArrayList) {
            if (operation == Operation.SUB) {
                isSub = !isSub;
            } else if (operation == Operation.NOT) {
                hasNot = !hasNot;
            }
        }
        if (isSub) {
            return Operation.SUB;
        } else {
            if (hasNot) {
                return Operation.NOT;
            }
            return Operation.ADD;
        }
    }

    private boolean isNumber(LLRegister register) {
        return register.getRegisterType() == RegisterType.CHAR
                || register.getRegisterType() == RegisterType.NUM;
    }

    private boolean isAllNumber(LLRegister mulRegisterLeft, LLRegister mulRegisterRight) {
            return (mulRegisterLeft.getRegisterType() == RegisterType.CHAR
                    || mulRegisterLeft.getRegisterType() == RegisterType.NUM)
                    && (mulRegisterRight.getRegisterType() == RegisterType.CHAR
                    || mulRegisterRight.getRegisterType() == RegisterType.NUM);
    }

    private LLRegister combineNumber(LLRegister mulRegisterLeft, LLRegister mulRegisterRight,
                                     InstructionType instructionType) {

        LLRegister retRegister = new LLRegister(mulRegisterLeft.getId());
        int leftValue = mulRegisterLeft.getRealValue();
        int rightValue = mulRegisterRight.getRealValue();
        int finalValue = 0;
        if (instructionType == InstructionType.add) {
            finalValue = leftValue + rightValue;
        } else if (instructionType == InstructionType.sub) {
            finalValue = leftValue - rightValue;
        } else if (instructionType == InstructionType.mul) {
            finalValue = leftValue * rightValue;
        } else if (instructionType == InstructionType.sdiv) {
            finalValue = leftValue / rightValue;
        } else if (instructionType == InstructionType.srem) {
            finalValue = leftValue % rightValue;
        }
        retRegister.setRegister(finalValue, '0', RetType.i32, RegisterType.NUM);
        return retRegister;
    }

    private int getRegId() {
        regIdCount++;
        return regIdCount;
    }

    private int getStringConstId() {
        stringConstIdCount++;
        return stringConstIdCount;
    }

    private void backRegId() {
        regIdCount--;
    }

    private void assignToRegister(LLRegister primaryRegister, LLRegister expRegister) {
        primaryRegister.setByReg(expRegister);
    }

    private void dealError(char type, int lineNumber) {
        if (output4Correct) {
            output4Correct = false;
        }
        compError.updateError(lineNumber, type);
    }

    private void output2File4Symbol() {
        if (output4Correct) {
            try (FileWriter writer = new FileWriter("symbol.txt", false)) {
                writer.write("");// 清空文件内容
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileWriter writer = new FileWriter("symbol.txt", true)) { // 追加模式
                for (SymbolTable symbolTable : outputSymbols) {
                    for (Symbol symbol : symbolTable.getSymbolArrayList()) {
                        writer.write((symbolTable.getId()) + " " + symbol.getName() + " " + symbol.getIdentType() + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (output4Error) {
                compError.output();
            }
        }
    }

    private void output2File4LLVM() {
        try (FileWriter writer = new FileWriter("llvm_ir.txt", false)) {
            writer.write("");// 清空文件内容
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter writer = new FileWriter("llvm_ir.txt", true)) { // 追加模式
            writer.write(module.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
