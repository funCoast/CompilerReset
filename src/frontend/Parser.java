package frontend;

import entity.CompUnit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private int curpos;

    private ArrayList<Token> tokens;

    private Token curToken;

    private boolean  output4Correct = true;

    private CompError errors;

    private ArrayList<String> correctOutput;

    private CompUnit compUnit;

    public Parser(ArrayList<Token> tokens, CompError errors) {
        this.curpos = -1;
        this.tokens = tokens;
        this.errors = errors;
        this.correctOutput = new ArrayList<>();
        this.compUnit = new CompUnit();
    }

    public void parseCompUnit() {
        curpos = 0;
        curToken = tokens.get(0);    // now in 1
        while (readAfterToken(1).getLexType() != LexType.MAINTK) {
            Token thirdtoken = readAfterToken(2); // get 3
            if (thirdtoken.getLexType() == LexType.LPARENT) {
                parseFuncDef();
            } else {
                parseDecl();
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
        parseBlock();
        outputType("<MainFuncDef>");
        outputType("<CompUnit>");
        output2File();
    }

    private void parseFuncDef() {
        parseFuncType();
        curToken = nextToken(); // skip ident
        curToken = nextToken(); // skip (
        if (curToken.getLexType() == LexType.INTTK || curToken.getLexType() == LexType.CHARTK) {
            parseFuncFParams();
        }
        if (curToken.getLexType() != LexType.RPARENT) {
            dealError('j');
        } else {
            curToken = nextToken(); // skip )
        }
        parseBlock();
        outputType("<FuncDef>");
    }

    private void parseBlock() {
        curToken = nextToken(); // skip {
        if (curToken.getLexType() == LexType.RBRACE) {
            curToken = nextToken(); // skip }
        } else {
            while (curToken.getLexType() != LexType.RBRACE) {
                parseBlockItem();
            }
            curToken = nextToken(); // skip }
        }
        outputType("<Block>");
    }

    private void parseBlockItem() {
        if (curToken.getLexType() == LexType.CONSTTK
                || curToken.getLexType() == LexType.INTTK
                || curToken.getLexType() == LexType.CHARTK) {
            parseDecl();
        } else {
            parseStmt();
        }
        // no need output
    }

    private void parseStmt() {
        if (curToken.getLexType() == LexType.SEMICN) { // only has ;
            curToken = nextToken(); // skip ;
        } else if (curToken.getLexType() == LexType.LBRACE) { // it's block
            parseBlock();
        } else if (curToken.getLexType() == LexType.IFTK) { // it's if
            curToken = nextToken(); // skip if
            curToken = nextToken(); // skip (
            parseCond();
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
            parseStmt();
            if (curToken.getLexType() == LexType.ELSETK) {
                curToken = nextToken();
                parseStmt();
            }
        } else if (curToken.getLexType() == LexType.FORTK) { // it's for
            curToken = nextToken(); // skip for
            curToken = nextToken(); // skip (
            if (curToken.getLexType() != LexType.SEMICN) {
                parseForStmt();
            }
            curToken = nextToken(); // skip ;
            if (curToken.getLexType() != LexType.SEMICN) {
                parseCond();
            }
            curToken = nextToken(); // skip ;
            if (curToken.getLexType() != LexType.RPARENT) {
                parseForStmt();
            }
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
            parseStmt();
        } else if (curToken.getLexType() == LexType.BREAKTK) { // it's break
            curToken = nextToken(); // skip breaks
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
        } else if (curToken.getLexType() == LexType.CONTINUETK) { // it's continue
            curToken = nextToken(); // skip continue
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
        } else if (curToken.getLexType() == LexType.RETURNTK) {  // it's return
            curToken = nextToken(); // skip return
            if (curToken.getLexType() == LexType.LPARENT || curToken.getLexType() == LexType.IDENFR
                    || curToken.getLexType() == LexType.INTCON || curToken.getLexType() == LexType.CHRCON
                    || curToken.getLexType() == LexType.PLUS || curToken.getLexType() == LexType.MINU
                    || curToken.getLexType() == LexType.NOT) {
                parseExp();
            }
            if (curToken.getLexType() != LexType.SEMICN) {
                dealError('i');
            } else {
                curToken = nextToken(); // skip ;
            }
        } else if (curToken.getLexType() == LexType.PRINTFTK) { // it's printf
            curToken = nextToken(); // skip printf
            curToken = nextToken(); // skip (
            curToken = nextToken(); // skip string、
            while (curToken.getLexType() == LexType.COMMA) {
                curToken = nextToken(); // skip ,
                parseExp();
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
                parseLVal();
                curToken = nextToken(); // skip =
                if (curToken.getLexType() == LexType.GETINTTK ||
                        curToken.getLexType() == LexType.GETCHARTK) {
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
                    parseExp();
                    if (curToken.getLexType() != LexType.SEMICN) {
                        dealError('i');
                    } else {
                        curToken = nextToken(); // skip ;
                    }
                }
            } else {
                parseExp();
                if (curToken.getLexType() != LexType.SEMICN) {
                    dealError('i');
                } else {
                    curToken = nextToken(); // skip ;
                }
            }
        }
        outputType("<Stmt>");
    }

    private void parseForStmt() {
        parseLVal();
        curToken = nextToken(); // skip =
        parseExp();
        outputType("<ForStmt>");
    }

    private void parseCond() {
        parseLOrExp();
        outputType("<Cond>");
    }

    private void parseLOrExp() {
        parseLAndExp();
        outputType("<LOrExp>");
        while (curToken.getLexType() == LexType.OR) {
            curToken = nextToken();
            parseLAndExp();
            outputType("<LOrExp>");
        }
    }

    private void parseLAndExp() {
        parseEqExp();
        outputType("<LAndExp>");
        while (curToken.getLexType() == LexType.AND) {
            curToken = nextToken();
            parseEqExp();
            outputType("<LAndExp>");
        }
    }

    private void parseEqExp() {
        parseRelExp();
        outputType("<EqExp>");
        while (curToken.getLexType() == LexType.EQL ||
                curToken.getLexType() == LexType.NEQ) {
            curToken = nextToken();
            parseRelExp();
            outputType("<EqExp>");
        }
    }

    private void parseRelExp() {
        parseAddExp();
        outputType("<RelExp>");
        while (curToken.getLexType() == LexType.LSS ||
                curToken.getLexType() == LexType.LEQ ||
                curToken.getLexType() == LexType.GRE ||
                curToken.getLexType() == LexType.GEQ) {
            curToken = nextToken(); // skip <,<=...
            parseAddExp();
            outputType("<RelExp>");
        }
    }

    private void parseFuncFParams() {
        parseFuncFParam();
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            parseFuncFParam();
        }
        outputType("<FuncFParams>");
    }

    private void parseFuncFParam() {
        curToken = nextToken(); // skip type
        curToken = nextToken(); // skip ident
        if (curToken.getLexType() == LexType.LBRACK) {
            curToken = nextToken(); // skip [
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        }
        outputType("<FuncFParam>");
    }

    private void parseFuncType() {
        curToken = nextToken(); // skip type
        outputType("<FuncType>");
    }

    private void parseDecl() {
        if (curToken.getLexType() == LexType.CONSTTK) {
            parseConstDecl();
        } else {
            parseVarDecl();
        }
        // no need to output
    }

    private void parseVarDecl() {
        curToken = nextToken(); // skip BType
        parseVarDef();
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            parseVarDef();
        }
        if (curToken.getLexType() != LexType.SEMICN) {
            dealError('i');
        } else {
            curToken = nextToken(); // skip ;
        }
        outputType("<VarDecl>");
    }

    private void parseVarDef() {
        curToken = nextToken(); // skip ident
        if (curToken.getLexType() == LexType.LBRACK) { // IF now is [
            curToken = nextToken(); // skip [
            parseConstExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        }
        if (curToken.getLexType() == LexType.ASSIGN) {
            curToken = nextToken(); // skip =
            parseInitVal();
        }
        outputType("<VarDef>");
    }

    private void parseInitVal() {
        if (curToken.getLexType() == LexType.STRCON) {
            curToken = nextToken();
        } else if (curToken.getLexType() == LexType.LBRACE) {
            curToken = nextToken(); // skip {
            parseExp();
            while (curToken.getLexType() == LexType.COMMA) {
                curToken = nextToken(); // skip ,
                parseExp();
            }
            curToken = nextToken(); // skip }
        } else {
            parseExp();
        }
        outputType("<InitVal>");
    }

    private void parseConstDecl() {
        curToken = nextToken(); // skip const
        curToken = nextToken(); // skip BType, now is ConstDef
        parseConstDef();
        while (tokens.get(curpos).getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            parseConstDef();
        }
        if (curToken.getLexType() != LexType.SEMICN) {
            dealError('i');
        } else {
            curToken = nextToken(); // skip ;
        }
        outputType("<ConstDecl>");
    }

    private void parseConstDef() {
        curToken = nextToken(); // skip Ident
        if (curToken.getLexType() == LexType.LBRACK) { // if now is [
            curToken = nextToken(); // skip [
            parseConstExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip [
            }
        }
        curToken = nextToken(); //skip =,jump in InitiVal
        parseConstInitVal();
        outputType("<ConstDef>");
    }

    private void parseConstInitVal() {
        if (curToken.getLexType() == LexType.STRCON) {
            curToken = nextToken(); // skip
        } else if (curToken.getLexType() == LexType.LBRACE) {
            curToken = nextToken(); // skip {
            if (curToken.getLexType() != LexType.RBRACE) {
                parseConstExp();
                while (curToken.getLexType() == LexType.COMMA) {
                    curToken = nextToken(); // skip ,
                    parseConstExp();
                }
            }
            curToken = nextToken(); // skip }
        } else {
            parseConstExp();
        }
        outputType("<ConstInitVal>");
    }

    private void parseConstExp() {
        parseAddExp();
        outputType("<ConstExp>");
    }

    private void parseAddExp() {
        parseMulExp();
        outputType("<AddExp>");
        while (curToken.getLexType() == LexType.PLUS // if now is +
                || curToken.getLexType() == LexType.MINU) {// or -
            curToken = nextToken(); // skip -, +
            parseMulExp();
            outputType("<AddExp>");
        }
    }

    private void parseMulExp() {
        parseUnaryExp();
        outputType("<MulExp>");
        while (curToken.getLexType() == LexType.MULT // if now is *
                || curToken.getLexType() == LexType.DIV // or /
                || curToken.getLexType() == LexType.MOD) { //or %
            curToken = nextToken(); // skip *...
            parseUnaryExp();
            outputType("<MulExp>");
        }
    }

    private void parseUnaryExp() {
        if (curToken.getLexType() == LexType.PLUS // if now is +
                || curToken.getLexType() == LexType.MINU // or -
                || curToken.getLexType() == LexType.NOT) {// or !
            parseUnaryOp();
            parseUnaryExp();
        } else if (curToken.getLexType() == LexType.IDENFR && readAfterToken(1).getLexType() == LexType.LPARENT) {
            curToken = nextToken(); // now is (
            if (readAfterToken(1).getLexType() != LexType.RPARENT) { // if next isn't )
                if (readAfterToken(1).getLexType() == LexType.SEMICN) { // if next is early ;
                    curToken = nextToken(); // skip (
                    dealError('j');
                } else {
                    curToken = nextToken(); // skip (
                    parseFuncRParams();
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
            parsePrimaryExp();
        }
        outputType("<UnaryExp>");
    }

    private void parseFuncRParams() {
        parseExp();
        while (curToken.getLexType() == LexType.COMMA) {
            curToken = nextToken(); // skip ,
            parseExp();
        }
        outputType("<FuncRParams>");
    }

    private void parsePrimaryExp() {
        if (curToken.getLexType() == LexType.LPARENT) {
            curToken = nextToken(); // skip (
            parseExp();
            if (curToken.getLexType() != LexType.RPARENT) {
                dealError('j');
            } else {
                curToken = nextToken(); // skip )
            }
        } else if (curToken.getLexType() == LexType.IDENFR) {
            parseLVal();
        } else { // it's num or char
            if (curToken.getLexType() == LexType.INTCON) {
                parseNumber();
            } else {
                parseCharacter();
            }
        }
        outputType("<PrimaryExp>");
    }

    private void parseNumber() {
        curToken = nextToken(); // just skip
        outputType("<Number>");
    }

    private void parseCharacter() {
        curToken = nextToken(); // just skip
        outputType("<Character>");
    }

    private void parseLVal() {
        if (readAfterToken(1).getLexType() == LexType.LBRACK) { // if next is [
            curToken = nextToken(); // skip ident
            curToken = nextToken(); // skip [
            parseExp();
            if (curToken.getLexType() != LexType.RBRACK) {
                dealError('k');
            } else {
                curToken = nextToken(); // skip ]
            }
        } else { // only Ident
            curToken = nextToken(); // just skip
        }
        outputType("<LVal>");
    }

    private void parseExp() {
        parseAddExp();
        outputType("<Exp>");
    }

    private void parseUnaryOp() {
        curToken = nextToken();
        outputType("<UnaryOp>");
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
            errors.output();
        }
    }
}
