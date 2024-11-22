package middleend;

import entity.*;
import entity.decl.Decl;
import entity.decl.con.ConstDecl;
import entity.decl.con.ConstDef;
import entity.decl.con.ConstInitVal;
import entity.decl.var.InitVal;
import entity.decl.var.VarDecl;
import entity.decl.var.VarDef;
import entity.expression.Exp;
import entity.expression.LOrExp;
import entity.funcdef.FuncDef;
import entity.funcdef.FuncFParam;
import entity.stmtEntity.*;
import frontend.CompError;

import java.util.ArrayList;

public class Visitor {
    private CompUnit compUnit;
    private SymbolTable curSymbolTable;
    private ArrayList<SymbolTable> symbolTableArrayList;
    private CompError compError;
    private boolean output4Correct = true;
    private boolean output4Error = true;
    private int tableIdTop;

    public Visitor(CompUnit compUnit, CompError compError) {
        this.compUnit = compUnit;
        this.compError = compError;
        this.symbolTableArrayList = new ArrayList<>();
        this.tableIdTop = 0;
    }


    public void visit() {
        if (!compError.isEmpty()) {
            output4Correct = false;
        }
        visitCompUnit();
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
        } else if (blockItem instanceof Stmt_IF) {
            visitStmt_IF((Stmt_IF) blockItem, identType);
        } else if (blockItem instanceof Stmt_FOR) {
            visitStmt_FOR((Stmt_FOR) blockItem, identType);
        } else if (blockItem instanceof Stmt_BREAK) {
            visitStmt_BREAK();
        } else if (blockItem instanceof Stmt_CONTINUE) {
            visitStmt_CONTINUE();
        } else if (blockItem instanceof Stmt_RETURN) {
            visitStmt_RETURN((Stmt_RETURN) blockItem, identType);
        } else if (blockItem instanceof Stmt_GetInt) {
            visitStmt_GetInt((Stmt_GetInt) blockItem);
        } else if (blockItem instanceof Stmt_GetChar) {
            visitStmt_GetChar((Stmt_GetChar) blockItem);
        }
    }

    private void visitStmt_Assign(Stmt_Assign blockItem) {
        visitLVal(blockItem.getlVal());
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
        visitStmt(blockItem.getStmtDo(), identType);
    }

    private void visitStmt_BREAK() {
    }

    private void visitStmt_CONTINUE() {
    }

    private void visitStmt_RETURN(Stmt_RETURN blockItem, IdentType identType) {
        if (blockItem.hasReturnValue()) {
            if (identType == IdentType.VoidFunc) {
                dealError('f', blockItem.getReturnLine());
            }
            visitExp(blockItem.getExp());
        }
    }

    private void visitStmt_GetInt(Stmt_GetInt blockItem, IdentType identType) {
    }

    private void visitStmt_GetChar(Stmt_GetChar blockItem) {
    }

    private void visitLVal(LVal lVal) {
        String ident = lVal.getIdent();
        int identLine = lVal.getIdentLine();
        searchHasDefine(ident, identLine);
        visitExp(lVal.getExp());
    }

    private void visitExp(Exp exp) {

    }

    private void visitLOrExp(LOrExp cond) {
    }

    private void pushNewTable() {
        tableIdTop++;
        SymbolTable symbolTable = new SymbolTable(tableIdTop);
        this.symbolTableArrayList.add(symbolTable);
        this.curSymbolTable = symbolTable;
    }

    private void popCurTable() {
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

    private void searchHasDefine(String ident, int identLine) {
        boolean hasFind = false;
        for (SymbolTable symbolTable : symbolTableArrayList) {
            if (!hasFind) {
                ArrayList<Symbol> symbolArrayList = symbolTable.getSymbolArrayList();
                for (Symbol symbol : symbolArrayList) {
                    if (symbol.getName().equals(ident)) {
                        hasFind = true;
                        break;
                    }
                }
            }
        }
        if (!hasFind) {
            dealError('c', identLine);
        }
    }

    private void insertSymbol(Symbol symbol) {
        curSymbolTable.getSymbolArrayList().add(symbol);
    }

    private void dealError(char type, int lineNumber) {
        if (output4Correct) {
            output4Correct = false;
        }
        compError.updateError(lineNumber, type);
    }
}
