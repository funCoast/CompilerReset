package frontend;

import entity.*;
import entity.Block;
import entity.BlockItem;
import entity.CompUnit;
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
import entity.funcdef.FuncType;
import entity.stmtEntity.*;
import middleend.IdentType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private int curpos;

    private ArrayList<Token> tokens;

    private Token curToken;

    private boolean  output4Correct = true;

    private boolean output4Error = false;

    private CompError errors;

    private ArrayList<String> correctOutput;

    private CompUnit compUnit;

    public Parser(ArrayList<Token> tokens, CompError errors) {
        this.curpos = -1;
        this.tokens = tokens;
        this.errors = errors;
        this.correctOutput = new ArrayList<>();
    }

    public CompUnit parseCompUnit() {
        curpos = 0;
        curToken = tokens.get(0);    // now in 1
        ArrayList<Decl> declArrayList = new ArrayList<>();
        ArrayList<FuncDef> funcDefArrayList = new ArrayList<>();
        while (readAfterToken(1).getLexType() != LexType.MAINTK) {
            Token thirdtoken = readAfterToken(2); // get 3
            if (thirdtoken.getLexType() == LexType.LPARENT) {
                funcDefArrayList.add(parseFuncDef());
            } else {
                declArrayList.add(parseDecl());
            }
        }
        curToken = nextToken(); // skip int
        curToken = nextToken(); // skip main
        curToken = nextToken(); // skip (
        if (curToken.getLexType() != LexType.RPARENT) {
            dealError('j');
        } else {
            curToken = nextToken(); // skip )
        }
        Block mainBlock = parseBlock();
        MainFuncDef mainFuncDef = new MainFuncDef(mainBlock);
        this.compUnit = new CompUnit(declArrayList, funcDefArrayList, mainFuncDef);
        outputType("<MainFuncDef>");
        outputType("<CompUnit>");
        output2File();
        return this.compUnit;
    }

    private FuncDef parseFuncDef() {
        FuncType funcType = parseFuncType();
        int identLine = curToken.getLineNumber();
        String ident = curToken.getName();
        curToken = nextToken(); // skip ident
        curToken = nextToken(); // skip (
        ArrayList<FuncFParam> funcFParamArrayList = new ArrayList<>();
        if (curToken.getLexType() == LexType.INTTK || curToken.getLexType() == LexType.CHARTK) {
            funcFParamArrayList =  parseFuncFParams();
        }
        if (curToken.getLexType() != LexType.RPARENT) {
            dealError('j');
        } else {
            curToken = nextToken(); // skip )
        }
        Block block = parseBlock();
        outputType("<FuncDef>");
        return new FuncDef(funcType, ident, funcFParamArrayList, block, identLine);
    }

    private Block parseBlock() {
        int rBrace;
        curToken = nextToken(); // skip {
        ArrayList<BlockItem> blockItemArrayList = new ArrayList<>();
        if (curToken.getLexType() == LexType.RBRACE) {
            rBrace = curToken.getLineNumber();
            curToken = nextToken(); // skip }
        } else {
            while (curToken.getLexType() != LexType.RBRACE) {
                blockItemArrayList.add(parseBlockItem());
            }
            rBrace = curToken.getLineNumber();
            curToken = nextToken(); // skip }
        }
        outputType("<Block>");
        return new Block(blockItemArrayList, rBrace);
    }

    private BlockItem parseBlockItem() {
        if (curToken.getLexType() == LexType.CONSTTK
                || curToken.getLexType() == LexType.INTTK
                || curToken.getLexType() == LexType.CHARTK) {
            return parseDecl();
        } else {
            return parseStmt();
        }
        // no need output
    }

    private Stmt parseStmt() {
        Stmt stmt;
        if (curToken.getLexType() == LexType.SEMICN) { // only has ;
            stmt = new Stmt_Exp(null, false);
            curToken = nextToken(); // skip ;
        } else if (curToken.getLexType() == LexType.LBRACE) { // it's block
            stmt = parseBlock();
        } else if (curToken.getLexType() == LexType.IFTK) { // it's if
            curToken = nextToken(); // skip if
            curToken = nextToken(); // skip (
            LOrExp cond = parseCond();
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
            Stmt stmtIf = parseStmt();
            Stmt stmtElse = null;
            if (curToken.getLexType() == LexType.ELSETK) {
                curToken = nextToken();
                stmtElse = parseStmt();
            }
            stmt = new Stmt_IF(cond, stmtIf, stmtElse);
        } else if (curToken.getLexType() == LexType.FORTK) { // it's for
            curToken = nextToken(); // skip for
            curToken = nextToken(); // skip (
            Stmt_Assign stmtForInit = null;
            LOrExp cond = null;
            Stmt_Assign stmtForAdd = null;
            Stmt stmtDo = null;
            if (curToken.getLexType() != LexType.SEMICN) {
                stmtForInit = parseForStmt();
            }
            curToken = nextToken(); // skip ;
            if (curToken.getLexType() != LexType.SEMICN) {
                cond = parseCond();
            }
            curToken = nextToken(); // skip ;
            if (curToken.getLexType() != LexType.RPARENT) {
                stmtForAdd = parseForStmt();
            }
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
            stmtDo = parseStmt();
            stmt = new Stmt_FOR(stmtForInit, cond, stmtForAdd, stmtDo);
        } else if (curToken.getLexType() == LexType.BREAKTK) { // it's break
            int breakLine = curToken.getLineNumber();
            curToken = nextToken(); // skip breaks
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
            stmt = new Stmt_BREAK(breakLine);
        } else if (curToken.getLexType() == LexType.CONTINUETK) { // it's continue
            int continueLine = curToken.getLineNumber();
            curToken = nextToken(); // skip continue
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
            stmt = new Stmt_CONTINUE(continueLine);
        } else if (curToken.getLexType() == LexType.RETURNTK) {  // it's return
            int returnLine = curToken.getLineNumber();
            curToken = nextToken(); // skip return
            Exp exp = null;
            if (curToken.getLexType() == LexType.LPARENT || curToken.getLexType() == LexType.IDENFR
                    || curToken.getLexType() == LexType.INTCON || curToken.getLexType() == LexType.CHRCON
                    || curToken.getLexType() == LexType.PLUS || curToken.getLexType() == LexType.MINU
                    || curToken.getLexType() == LexType.NOT) {
                exp = parseExp();
            }
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
            stmt = new Stmt_RETURN(exp, returnLine);
        } else if (curToken.getLexType() == LexType.PRINTFTK) { // it's printf
            int printfLine = curToken.getLineNumber();
            curToken = nextToken(); // skip printf
            curToken = nextToken(); // skip (
            ArrayList<Exp> expArrayList = new ArrayList<>();
            String stringConst = curToken.getName();
            curToken = nextToken(); // skip string
            while (curToken.getLexType() == LexType.COMMA) {
                curToken = nextToken(); // skip ,
                expArrayList.add(parseExp());
            }
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
            stmt = new Stmt_Printf(stringConst, expArrayList, printfLine);
        } else { // it's LVal
            int saveOutSum = correctOutput.size();
            int savePos = curpos;
            Token saveToken = curToken;
            boolean isAssign = false;
            boolean saveOutput4Correct = output4Correct;
            curToken = nextToken(); // skip ident
            if (curToken.getLexType() == LexType.LBRACK) {
                curToken = nextToken(); // skip [
                parseExp();
                if (curToken.getLexType() == LexType.RBRACK) {
                    curToken = nextToken(); // skip ]
                }
                if (curToken.getLexType() == LexType.ASSIGN) {
                    isAssign = true;
                }
            } else {
                if (curToken.getLexType() == LexType.ASSIGN) {
                    isAssign = true;
                }
            }
            curToken = saveToken;
            curpos = savePos;
            output4Correct = saveOutput4Correct;
            correctOutput.subList(saveOutSum, correctOutput.size()).clear();
            if (isAssign) {
                LVal lVal = parseLVal();
                curToken = nextToken(); // skip =
                if (curToken.getLexType() == LexType.GETINTTK ||
                        curToken.getLexType() == LexType.GETCHARTK) {
                    if (curToken.getLexType() == LexType.GETINTTK) {
                        stmt = new Stmt_GetInt(lVal);
                    } else {
                        stmt = new Stmt_GetChar(lVal);
                    }
                    curToken = nextToken(); // skip get~
                    curToken = nextToken(); // skip (
                    if (curToken.getLexType() != LexType.RPARENT) {
                        dealError('j');
                    } else {
                        curToken = nextToken(); // skip )
                    }
                    if (curToken.getLexType() != LexType.SEMICN) {
                        dealError('i');
                    } else {
                        curToken = nextToken(); // skip ;
                    }
                } else {
                    Exp exp = parseExp();
                    if (curToken.getLexType() != LexType.SEMICN) {
                        dealError('i');
                    } else {
                        curToken = nextToken(); // skip ;
                    }
                    stmt = new Stmt_Assign(lVal, exp);
                }
            } else {
                Exp exp = parseExp();
                if (curToken.getLexType() != LexType.SEMICN) {
                    dealError('i');
                } else {
                    curToken = nextToken(); // skip ;
                }
                stmt = new Stmt_Exp(exp, true);
            }
        }
        outputType("<Stmt>");
        return stmt;
    }

    private Stmt_Assign parseForStmt() {
        LVal lVal = parseLVal();
        curToken = nextToken(); // skip =
        Exp exp = parseExp();
        outputType("<ForStmt>");
        return new Stmt_Assign(lVal, exp);
    }

    private LOrExp parseCond() {
        LOrExp lOrExp = parseLOrExp();
        outputType("<Cond>");
        return lOrExp;
    }

    private LOrExp parseLOrExp() {
        ArrayList<LAndExp> lAndExpArrayList = new ArrayList<>();
        lAndExpArrayList.add(parseLAndExp());
        outputType("<LOrExp>");
        while (curToken.getLexType() == LexType.OR) {
            curToken = nextToken();
            lAndExpArrayList.add(parseLAndExp());
            outputType("<LOrExp>");
        }
        return new LOrExp(lAndExpArrayList);
    }

    private LAndExp parseLAndExp() {
        ArrayList<EqExp> expArrayList = new ArrayList<>();
        expArrayList.add(parseEqExp());
        outputType("<LAndExp>");
        while (curToken.getLexType() == LexType.AND) {
            curToken = nextToken();
            expArrayList.add(parseEqExp());
            outputType("<LAndExp>");
        }
        return new LAndExp(expArrayList);
    }

    private EqExp parseEqExp() {
        ArrayList<RelExp> relExpArrayList = new ArrayList<>();
        ArrayList<Compare> compareArrayList = new ArrayList<>();
        relExpArrayList.add(parseRelExp());
        outputType("<EqExp>");
        while (curToken.getLexType() == LexType.EQL ||
                curToken.getLexType() == LexType.NEQ) {
            compareArrayList.add(getCompare(curToken));
            curToken = nextToken();
            relExpArrayList.add(parseRelExp());
            outputType("<EqExp>");
        }
        return new EqExp(relExpArrayList, compareArrayList);
    }

    private RelExp parseRelExp() {
        ArrayList<Exp> expArrayList = new ArrayList<>();
        ArrayList<Compare> compareArrayList = new ArrayList<>();
        expArrayList.add(parseAddExp());
        outputType("<RelExp>");
        while (curToken.getLexType() == LexType.LSS ||
                curToken.getLexType() == LexType.LEQ ||
                curToken.getLexType() == LexType.GRE ||
                curToken.getLexType() == LexType.GEQ) {
            compareArrayList.add(getCompare(curToken));
            curToken = nextToken(); // skip <,<=...
            expArrayList.add(parseAddExp());
            outputType("<RelExp>");
        }
        return new RelExp(expArrayList, compareArrayList);
    }

    private ArrayList<FuncFParam> parseFuncFParams() {
        ArrayList<FuncFParam> funcFParamArrayList = new ArrayList<>();
        funcFParamArrayList.add(parseFuncFParam());
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            funcFParamArrayList.add(parseFuncFParam());
        }
        outputType("<FuncFParams>");
        return funcFParamArrayList;
    }

    private FuncFParam parseFuncFParam() {
        BType bType = new BType(getBType(curToken, false));
        curToken = nextToken(); // skip type
        int identLine = curToken.getLineNumber();
        String ident = curToken.getName();
        curToken = nextToken(); // skip ident
        boolean isArray = false;
        if (curToken.getLexType() == LexType.LBRACK) {
            bType.setIdentType(bType.setArray());
            isArray = true;
            curToken = nextToken(); // skip [
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        }
        outputType("<FuncFParam>");
        return new FuncFParam(bType, ident, identLine, isArray);
    }

    private FuncType parseFuncType() {
        IdentType identType = getFuncType(curToken);
        curToken = nextToken(); // skip type
        outputType("<FuncType>");
        return new FuncType(identType);
    }

    private Decl parseDecl() {
        if (curToken.getLexType() == LexType.CONSTTK) {
            return parseConstDecl();
        } else {
            return parseVarDecl();
        }
        // no need to output
    }

    private VarDecl parseVarDecl() {
        IdentType identType = getBType(curToken ,false);
        BType bType = new BType(identType);
        ArrayList<VarDef> varDefArrayList = new ArrayList<>();
        curToken = nextToken(); // skip BType
        varDefArrayList.add(parseVarDef());
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            varDefArrayList.add(parseVarDef());
        }
        if (curToken.getLexType() != LexType.SEMICN) {
            dealError('i');
        } else {
            curToken = nextToken(); // skip ;
        }
        outputType("<VarDecl>");
        return new VarDecl(bType, varDefArrayList);
    }

    private VarDef parseVarDef() {
        int identLine = curToken.getLineNumber();
        String ident = curToken.getName();
        boolean isArray = false;
        Exp exp = null;
        InitVal initVal = null;
        curToken = nextToken(); // skip ident
        if (curToken.getLexType() == LexType.LBRACK) { // IF now is [
            isArray = true;
            curToken = nextToken(); // skip [
            exp = parseConstExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        }
        if (curToken.getLexType() == LexType.ASSIGN) {
            curToken = nextToken(); // skip =
            initVal = parseInitVal();
        }
        outputType("<VarDef>");
        return new VarDef(ident, isArray, exp, initVal, identLine);
    }

    private InitVal parseInitVal() {
        String string = null;
        ArrayList<Exp> expArrayList = null;
        if (curToken.getLexType() == LexType.STRCON) {
            string = curToken.getName();
            curToken = nextToken();
        } else if (curToken.getLexType() == LexType.LBRACE) {
            expArrayList = new ArrayList<>();
            curToken = nextToken(); // skip {
            expArrayList.add(parseExp());
            while (curToken.getLexType() == LexType.COMMA) {
                curToken = nextToken(); // skip ,
                expArrayList.add(parseExp());
            }
            curToken = nextToken(); // skip }
        } else {
            expArrayList = new ArrayList<>();
            expArrayList.add(parseExp());
        }
        outputType("<InitVal>");
        return new InitVal(string, expArrayList);
    }

    private ConstDecl parseConstDecl() {
        curToken = nextToken(); // skip const
        IdentType identType = getBType(curToken, true);
        BType bType = new BType(identType);
        curToken = nextToken(); // skip BType, now is ConstDef
        ArrayList<ConstDef> constDefArrayList = new ArrayList<>();
        constDefArrayList.add(parseConstDef());
        while (tokens.get(curpos).getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            constDefArrayList.add(parseConstDef());
        }
        if (curToken.getLexType() != LexType.SEMICN) {
            dealError('i');
        } else {
            curToken = nextToken(); // skip ;
        }
        outputType("<ConstDecl>");
        return new ConstDecl(bType, constDefArrayList);
    }

    private ConstDef parseConstDef() {
        int identLine = curToken.getLineNumber();
        String ident = curToken.getName();
        curToken = nextToken(); // skip Ident
        boolean isArray = false;
        Exp exp = null;
        if (curToken.getLexType() == LexType.LBRACK) { // if now is [
            curToken = nextToken(); // skip [
            isArray = true;
            exp = parseConstExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip [
            }
        }
        curToken = nextToken(); //skip =,jump in InitiVal
        ConstInitVal constInitVal = parseConstInitVal();
        outputType("<ConstDef>");
        return new ConstDef(ident, isArray, exp, constInitVal, identLine);
    }

    private ConstInitVal parseConstInitVal() {
        String string = null;
        ArrayList<Exp> expArrayList = null;
        if (curToken.getLexType() == LexType.STRCON) {
            string = curToken.getName();
            curToken = nextToken(); // skip
        } else if (curToken.getLexType() == LexType.LBRACE) {
            expArrayList = new ArrayList<>();
            curToken = nextToken(); // skip {
            if (curToken.getLexType() != LexType.RBRACE) {
                expArrayList.add(parseConstExp());
                while (curToken.getLexType() == LexType.COMMA) {
                    curToken = nextToken(); // skip ,
                    expArrayList.add(parseConstExp());
                }
            }
            curToken = nextToken(); // skip }
        } else {
            expArrayList = new ArrayList<>();
            expArrayList.add(parseConstExp());
        }
        outputType("<ConstInitVal>");
        return new ConstInitVal(string, expArrayList);
    }

    private Exp parseConstExp() {
        Exp exp = parseAddExp();
        outputType("<ConstExp>");
        return exp;
    }

    private Exp parseAddExp() {
        ArrayList<MulExp> mulExpArrayList = new ArrayList<>();
        ArrayList<Operation> operationArrayList = new ArrayList<>();
        mulExpArrayList.add(parseMulExp());
        outputType("<AddExp>");
        while (curToken.getLexType() == LexType.PLUS // if now is +
                || curToken.getLexType() == LexType.MINU) {// or -
            operationArrayList.add(getOperation(curToken));
            curToken = nextToken(); // skip -, +
            mulExpArrayList.add(parseMulExp());
            outputType("<AddExp>");
        }
        return new Exp(mulExpArrayList, operationArrayList);
    }

    private MulExp parseMulExp() {
        ArrayList<UnaryExp> unaryExpArrayList = new ArrayList<>();
        ArrayList<Operation> operationArrayList = new ArrayList<>();
        unaryExpArrayList.add(parseUnaryExp());
        outputType("<MulExp>");
        while (curToken.getLexType() == LexType.MULT // if now is *
                || curToken.getLexType() == LexType.DIV // or /
                || curToken.getLexType() == LexType.MOD) { //or %
            operationArrayList.add(getOperation(curToken));
            curToken = nextToken(); // skip *...
            unaryExpArrayList.add(parseUnaryExp());
            outputType("<MulExp>");
        }
        return new MulExp(unaryExpArrayList, operationArrayList);
    }

    private UnaryExp parseUnaryExp() {
        ArrayList<Operation> operationArrayList = new ArrayList<>();
        boolean isFuncCall = false;
        PrimaryExp primaryExp = null;
        String ident = null;
        int identLine = 0;
        ArrayList<Exp> funcRParamArrayList = new ArrayList<>();
        if (curToken.getLexType() == LexType.PLUS // if now is +
                || curToken.getLexType() == LexType.MINU // or -
                || curToken.getLexType() == LexType.NOT) {// or !
            operationArrayList.add(parseUnaryOp());
            UnaryExp unaryExp = parseUnaryExp();
            isFuncCall = unaryExp.isFuncCall();
            primaryExp = unaryExp.getPrimaryExp();
            ident = unaryExp.getIdent();
            identLine = unaryExp.getIdentLine();
            funcRParamArrayList = unaryExp.getFuncRParams();
            operationArrayList.addAll(unaryExp.getOperationArrayList());
        } else if (curToken.getLexType() == LexType.IDENFR && readAfterToken(1).getLexType() == LexType.LPARENT) {
            isFuncCall = true;
            identLine = curToken.getLineNumber();
            ident = curToken.getName();
            curToken = nextToken(); // now is (
            if (readAfterToken(1).getLexType() != LexType.RPARENT) { // if next isn't )
                if (readAfterToken(1).getLexType() == LexType.SEMICN) { // if next is early ;
                    curToken = nextToken(); // skip (
                    dealError('j');
                } else {
                    curToken = nextToken(); // skip (
                    funcRParamArrayList = parseFuncRParams();
                    if (curToken.getLexType() != LexType.RPARENT) {
                        dealError('j');
                    } else {
                        curToken = nextToken(); // skip )
                    }
                }

            } else {
                curToken = nextToken(); // now is )
                if (curToken.getLexType() != LexType.RPARENT) {
                    dealError('j');
                } else {
                    curToken = nextToken(); // skip )
                }
            }
        } else {
            primaryExp = parsePrimaryExp();
        }
        outputType("<UnaryExp>");
        return new UnaryExp(operationArrayList, isFuncCall, primaryExp, ident, funcRParamArrayList, identLine);
    }

    private ArrayList<Exp> parseFuncRParams() {
        ArrayList<Exp> expArrayList = new ArrayList<>();
        expArrayList.add(parseExp());
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            expArrayList.add(parseExp());
        }
        outputType("<FuncRParams>");
        return expArrayList;
    }

    private PrimaryExp parsePrimaryExp() {
        Exp exp = null;
        LVal lVal = null;
        Integer number = null;
        Character character = null;
        PriExpType priExpType = null;
        if (curToken.getLexType() == LexType.LPARENT) {
            curToken = nextToken(); // skip (
            exp = parseExp();
            priExpType = PriExpType.EXP;
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
        } else if (curToken.getLexType() == LexType.IDENFR) {
            lVal = parseLVal();
            priExpType = PriExpType.LVAL;
        } else { // it's num or char
            if (curToken.getLexType() == LexType.INTCON) {
                number = parseNumber();
                priExpType = PriExpType.NUM;
            } else {
                character = parseCharacter();
                priExpType = PriExpType.CHAR;
            }
        }
        outputType("<PrimaryExp>");
        return new PrimaryExp(exp, lVal, number, character, priExpType);
    }

    private Integer parseNumber() {
        Integer integer = Integer.parseInt(curToken.getName());
        curToken = nextToken(); // just skip
        outputType("<Number>");
        return integer;
    }

    private Character parseCharacter() {
        Character character = curToken.getName().charAt(1);
        curToken = nextToken(); // just skip
        outputType("<Character>");
        return character;
    }

    private LVal parseLVal() {
        String ident = null;
        Exp exp = null;
        int identLine;
        boolean isArray = false;
        if (readAfterToken(1).getLexType() == LexType.LBRACK) { // if next is [
            isArray = true;
            identLine = curToken.getLineNumber();
            ident = curToken.getName();
            curToken = nextToken(); // skip ident
            curToken = nextToken(); // skip [
            exp = parseExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        } else { // only Ident
            identLine = curToken.getLineNumber();
            ident = curToken.getName();
            curToken = nextToken(); // just skip
        }
        outputType("<LVal>");
        return new LVal(ident, exp, isArray, identLine);
    }

    private Exp parseExp() {
        Exp exp = parseAddExp();
        outputType("<Exp>");
        return exp;
    }

    private Operation parseUnaryOp() {
        Operation operation = getOperation(curToken);
        curToken = nextToken();
        outputType("<UnaryOp>");
        return operation;
    }

    private Token readAfterToken(int index) {
        if (curpos + index < tokens.size()) {
            return tokens.get(curpos + index);
        } else {
            return null;
        }
    }

    private Token nextToken() {
        outputToken(tokens.get(curpos));
        curpos++;
        if (curpos < tokens.size()) {
            return tokens.get(curpos);
        } else {
            return null;
        }
    }

    private void outputToken(Token token) {
        correctOutput.add(token.getLexType() + " " + token.getName() + "\n");
    }

    private void outputType(String string) {
        correctOutput.add(string + "\n");
    }

    private void dealError(char type) {
        if (output4Correct) {
            output4Correct = false;
        }
        errors.updateError(tokens.get(curpos - 1).getLineNumber(), type);
    }

    private void output2File() {
        if (output4Correct) {
            // 清空或创建 parser.txt 文件
            try (FileWriter writer = new FileWriter("parser.txt", false)) {
                // false 表示覆盖文件内容，达到清空文件的效果
                writer.write(""); // 清空文件内容
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileWriter writer = new FileWriter("parser.txt", true)) {  // true 表示追加模式
                for (String string : correctOutput) {
                    writer.write(string);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (output4Error) {
                errors.output();
            }
        }
    }

    private IdentType getBType(Token token, boolean isConst) {
        int bType = token.getBType();
        if (bType == 1) {
            if (isConst) {
                return IdentType.ConstInt;
            } else {
                return IdentType.Int;
            }
        } else {
            if (isConst) {
                return IdentType.ConstChar;
            } else {
                return IdentType.Char;
            }
        }
    }

    private IdentType getFuncType(Token token) {
        int bType = token.getBType();
        if (bType == 1) {
            return IdentType.IntFunc;
        } else if (bType == 2) {
            return IdentType.CharFunc;
        } else {
            return IdentType.VoidFunc;
        }
    }

    private void setArray(IdentType identType) {
        if (identType == IdentType.Int) {
            identType = IdentType.IntArray;
        } else if (identType == IdentType.Char) {
            identType = IdentType.CharArray;
        } else if (identType == IdentType.ConstInt) {
            identType = IdentType.ConstIntArray;
        } else if (identType == IdentType.ConstChar) {
            identType = IdentType.ConstCharArray;
        }
    }

    private Operation getOperation(Token token) {
        if (token.getLexType() == LexType.PLUS) {
            return Operation.ADD;
        } else if (token.getLexType() == LexType.MINU) {
            return Operation.SUB;
        } else if (token.getLexType() == LexType.MULT) {
            return Operation.MUL;
        } else if (token.getLexType() == LexType.DIV) {
            return Operation.DIV;
        } else if (token.getLexType() == LexType.MOD) {
            return Operation.MOD;
        } else if (token.getLexType() == LexType.NOT) {
            return Operation.NOT;
        }
        System.out.println("this is not operation, from:Parse.getOperation");;
        return null;
    }

    private Compare getCompare(Token token) {
        if (token.getLexType() == LexType.GRE) {
            return Compare.BIG_THAN;
        } else if (token.getLexType() == LexType.GEQ) {
            return Compare.BIG_OR_EQUAL;
        } else if (token.getLexType() == LexType.LSS) {
            return Compare.SMALL_THAN;
        } else if (token.getLexType() == LexType.LEQ) {
            return Compare.SMALL_OR_EQUAL;
        } else if (token.getLexType() == LexType.EQL) {
            return Compare.EQUAL;
        } else if (token.getLexType() == LexType.NEQ) {
            return Compare.NOT_EQUAL;
        }
        System.out.println("this is not compare, from:Parse.getCompare");;
        return null;
    }
}
