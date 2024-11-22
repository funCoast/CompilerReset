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

    public Visitor(CompUnit compUnit, CompError compError) {
        this.compUnit = compUnit;
        this.compError = compError;
        this.symbolTableArrayList = new ArrayList<>();
        this.tableIdTop = 0;
        this.countForCircle = 0;
        this.outputSymbols = new ArrayList<>();
    }


    public void visit() {
        if (!compError.isEmpty()) {
            output4Correct = false;
        }
        visitCompUnit();
        output2File4Symbol();
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
        visitMainFuncDef(compUnit.getMainFuncDef());
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
            visitExp(varDef.getConstExp());
        }
        if (varDef.getInitVal() != null) {
            visitInitVal(varDef.getInitVal());
        }
    }

    private void visitInitVal(InitVal initVal) {
        if (initVal.isString()) {

        } else {
            ArrayList<Exp> expArrayList = initVal.getExpArrayList();
            for (Exp exp : expArrayList) {
                visitExp(exp);
            }
        }
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
            visitExp(constDef.getConstExp());
        }
        visitConstInitVal(constDef.getConstInitVal());
    }

    private void visitConstInitVal(ConstInitVal constInitVal) {
        if (constInitVal.isString()) {

        } else {
            ArrayList<Exp> expArrayList = constInitVal.getExpArrayList();
            for (Exp exp : expArrayList) {
                visitExp(exp);
            }
        }
    }

    private void visitFuncDef(FuncDef funcDef) {
        String ident = funcDef.getIdent();
        int identLine = funcDef.getIdentLine();
        searchInCurTable(ident, identLine); // can deal define repeat error
        IdentType identType = funcDef.getFuncType().getIdentType();
        int paramNum = funcDef.getFuncFParamArrayList().size();
        ArrayList<IdentType> paramTypes = funcDef.getParamIdentTypes();
        FuncInfo funcInfo = new FuncInfo(paramNum, paramTypes);
        Symbol symbol = new Symbol(symbolTableArrayList.indexOf(curSymbolTable) + 1, ident, identType, funcInfo);
        insertSymbol(symbol);
        // ^^^ insert symbol
        ArrayList<FuncFParam> funcFParamArrayList = funcDef.getFuncFParamArrayList();
        for (FuncFParam funcFParam : funcFParamArrayList) {
            visitFuncFParam(funcFParam);
        }
        pushNewTable();
        visitBlock(funcDef.getBlock(), identType, true);
        popCurTable();
    }

    private void visitFuncFParam(FuncFParam funcFParam) {
        String ident = funcFParam.getIdent();
        int identLine = funcFParam.getIdentLine();
        searchInCurTable(ident, identLine);
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {

    }

    private void visitBlock(Block block, IdentType identType, boolean needReturn) {
        ArrayList<BlockItem> blockItemArrayList = block.getBlockItemArrayList();
        for (BlockItem blockItem : blockItemArrayList) {
            visitBlockItem(blockItem, identType);
        }
        if (needReturn && (identType == IdentType.IntFunc || identType == IdentType.CharFunc)) {
            BlockItem finalItem = blockItemArrayList.get(blockItemArrayList.size() - 1);
            if (!(finalItem instanceof Stmt_RETURN)) {
                dealError('g', block.getrBraceLine());
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
        visitLVal(blockItem.getlVal(), true);
        visitExp(blockItem.getExp());
    }

    private void visitStmt_Exp(Stmt_Exp blockItem) {
        if (blockItem.isHasExp()) {
            visitExp(blockItem.getExp());
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
        if (blockItem.hasReturnValue()) {
            if (identType == IdentType.VoidFunc) {
                dealError('f', blockItem.getReturnLine());
            }
            visitExp(blockItem.getExp());
        }
    }

    private void visitStmt_GetInt(Stmt_GetInt blockItem) {
        visitLVal(blockItem.getlVal(), true);
    }

    private void visitStmt_GetChar(Stmt_GetChar blockItem) {
        visitLVal(blockItem.getlVal(), true);
    }

    private void visitStmt_Printf(Stmt_Printf blockItem) {
        int expressionNum = countPrintfChar(blockItem.getStringConst());
        int getNum = 0;
        if (blockItem.hasDescribe()) {
            ArrayList<Exp> expArrayList = blockItem.getExpArrayList();
            for (Exp exp : expArrayList) {
                getNum++;
                visitExp(exp);
            }
        }
        if (expressionNum != getNum) {
            dealError('l', blockItem.getPrintfLine());
        }
    }

    private IdentType visitLVal(LVal lVal, boolean needCheckConst) {
        String ident = lVal.getIdent();
        int identLine = lVal.getIdentLine();
        Symbol symbol = searchHasDefine(ident, identLine);
        IdentType identType = symbol.getIdentType();
        if (needCheckConst) {
            if (symbol.isConst()) {
                dealError('h', identLine);
            }
        }
        if (lVal.isArray()) {
            if (lVal.getExp() != null) {
                visitExp(lVal.getExp());
            } else {
                identType = goBasic(identType);
            }
        }
        return identType;
    }

    private IdentType visitExp(Exp exp) {
        IdentType identType = IdentType.Char;
        ArrayList<MulExp> mulExpArrayList = exp.getMulExpArrayList();
        ArrayList<Operation> operationArrayList = exp.getOperationArrayList();
        for (MulExp mulExp : mulExpArrayList) {
            IdentType identTypeTemp = visitMulExp(mulExp, identType);
            identType = adaptIdentType(identType, identTypeTemp);
        }
        return identType;
    }

    private IdentType visitMulExp(MulExp mulExp, IdentType identType) {
        IdentType identTypeReturn = identType;
        ArrayList<UnaryExp> unaryExpArrayList = mulExp.getUnaryExpArrayList();
        for (UnaryExp unaryExp : unaryExpArrayList) {
            IdentType identTypeTemp = visitUnaryExp(unaryExp, identTypeReturn);
            identTypeReturn = adaptIdentType(identTypeReturn, identTypeTemp);
        }
        return identTypeReturn;
    }

    private IdentType visitUnaryExp(UnaryExp unaryExp, IdentType identTypeReturn) {
        if (unaryExp.isFuncCall()) {
            IdentType identType = null;
            String ident = unaryExp.getIdent();
            int identLine = unaryExp.getIdentLine();
            Symbol funcSymbol = searchHasDefine(ident, identLine);
            FuncInfo expectedFuncInfo = funcSymbol.getFuncInfo(); // expected
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
            for (Exp exp : funcRParams) {
                paramTypesGet.add(visitExp(exp));
            } // visit and get identType ArrayList
            if (expectedFuncInfo.getParamNum() != paramNumGet) {
                dealError('d', identLine);
            } else if (expectedFuncInfo.checkCons(new FuncInfo(paramNumGet, paramTypesGet))) {
                dealError('e', identLine);
            } // deal error
            return adaptIdentType(identType, identTypeReturn);
        } else {
            IdentType identType = visitPrimaryExp(unaryExp.getPrimaryExp());
            return adaptIdentType(identType, identTypeReturn);
        }
    }

    private IdentType visitPrimaryExp(PrimaryExp primaryExp) {
        PriExpType priExpType = primaryExp.getPriExpType();
        if (priExpType == PriExpType.EXP) {
            return visitExp(primaryExp.getExp());
        } else if (priExpType == PriExpType.LVAL) {
            return visitLVal(primaryExp.getlVal(), false);
        } else if (priExpType == PriExpType.NUM) {
            return IdentType.Int;
        } else if (priExpType == PriExpType.CHAR) {
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
            visitExp(exp);
        }
    }

    private void pushNewTable() {
        tableIdTop++;
        SymbolTable symbolTable = new SymbolTable(tableIdTop);
        this.symbolTableArrayList.add(symbolTable);
        this.curSymbolTable = symbolTable;
    }

    private void popCurTable() {
        outputSymbols.add(this.symbolTableArrayList.get(symbolTableArrayList.size() - 1));
        this.symbolTableArrayList.remove(symbolTableArrayList.size() - 1);
        this.curSymbolTable = this.symbolTableArrayList.get(symbolTableArrayList.size() - 1);
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
        for (SymbolTable symbolTable : symbolTableArrayList) {
            if (!hasFind) {
                ArrayList<Symbol> symbolArrayList = symbolTable.getSymbolArrayList();
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

    private void insertSymbol(Symbol symbol) {
        curSymbolTable.getSymbolArrayList().add(symbol);
    }

    private IdentType adaptIdentType(IdentType identTypeA, IdentType identTypeB) {
        if (identTypeA == identTypeB) {
            return identTypeA;
        }
        // char 遇到 int 返回 int
        if ((identTypeA == IdentType.Char && identTypeB == IdentType.Int) ||
                (identTypeA == IdentType.Int && identTypeB == IdentType.Char)) {
            return IdentType.Int;
        }
        // 基础类型遇到数组，返回数组类型
        if (isBasicType(identTypeA) && isArrayType(identTypeB)) {
            return identTypeB;
        }
        if (isArrayType(identTypeA) && isBasicType(identTypeB)) {
            return identTypeA;
        }
        // 默认返回数组类型（处理 Const 和非 Const 的数组）
        if (isArrayType(identTypeA) && isArrayType(identTypeB)) {
            return identTypeA; // 或 identTypeB，依据业务需求调整
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

    private int countPrintfChar(String name) {
        int count = 0;
        boolean isKey = false;
        for (int i = 0; i < name.length(); i++) {
            char currentChar = name.charAt(i);
            if (currentChar == '%') {
                isKey = true;
            } else if (isKey && (currentChar == 'd' || currentChar == 'c')) {
                count++;
                isKey = false;
            } else {
                isKey = false;
            }
        }
        return count;
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
                ArrayList<Integer> ids = new ArrayList<>();
                ArrayList<ArrayList<Symbol>> tables = new ArrayList<>();
                for (SymbolTable symbolTable : outputSymbols) {
                    ids.add(symbolTable.getId());
                    tables.add(symbolTable.getSymbolArrayList());
                }
                for (int i = 0; i < ids.size() - 1; i++) {
                    for (int j = 0; j < ids.size() - 1 - i; j++) {
                        if (ids.get(j) > ids.get(j + 1)) {
                            int tempId = ids.get(j);
                            ids.set(j, ids.get(j + 1));
                            ids.set(j + 1, tempId);
                            ArrayList<Symbol> tempTable = tables.get(j);
                            tables.set(j, tables.get(j + 1));
                            tables.set(j + 1, tempTable);
                        }
                    }
                }
                for (int i = 0; i < tables.size(); i++) {
                    int tableId = ids.get(i);
                    ArrayList<Symbol> symbols = tables.get(i);
                    for (Symbol symbol : symbols) {
                        writer.write(tableId + " " + symbol.getName() + " " + symbol.getIdentType() + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            compError.output();
        }
    }

}
