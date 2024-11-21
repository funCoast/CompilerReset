package frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class Lexer {
    private boolean needOutputerror = false;

    private boolean  output4Correct = false;

    private boolean matchComments = false;

    private LexType curLexType = LexType.START;

    private int curpos = 0;

    private ArrayList<Token> tokens = new ArrayList<>();

    private CompError errors;

    public Lexer (CompError errors) {
        this.errors = errors;
    }

    public ArrayList<Token> lexicalAnalysis() throws Exception {
        String filePath = "testfile.txt";  // 文件路径
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int lineCount = 0;
        // 逐行读取文件
        while ((line = reader.readLine()) != null) {
            lineCount++;
            if (matchComments) {
                String leftPart = dealComments(line);
                if (leftPart != null) {
                    parseLine(leftPart, lineCount);
                }
            } else {
                parseLine(line, lineCount);
            }
        }

        if(!needOutputerror && output4Correct) {
            outputTokens();
        }

        if (needOutputerror) {
            outputError();
        }

        reader.close();  // 关闭文件读取器

        return tokens;
    }

    private String dealComments(String line) {
        curpos = 0;
        while (true) {
            while(curpos < line.length() && line.charAt(curpos) != '*') {
                curpos++;
            }
            if (curpos >= line.length()) {
                return null;
            }
            curpos++;
            if (curpos < line.length() && line.charAt(curpos) == '/') {
                matchComments = false;
                curpos++;
                return line.substring(curpos);
            }
        }
    }

    private void parseLine(String line, int lineCount) {
        curpos = 0;
        String name = null;
        while (true) {
            while(curpos < line.length() && (line.charAt(curpos) == ' ' || line.charAt(curpos) == '\t' || line.charAt(curpos) == '\r')) {
                curpos++;
            }
            if (curpos >= line.length()) {
                return;
            }
            if (line.charAt(curpos) == '\"') {  // deal String
                StringBuilder tem = new StringBuilder(1024);
                tem.append(line.charAt(curpos));
                curpos++;
                while (curpos < line.length() && line.charAt(curpos) != '\"') {
                    if (line.charAt(curpos) == '\\') {
                        tem.append(line.charAt(curpos));
                        curpos++;
                        if (curpos >= line.length()) {
                            //TODO
                            //dont match more
                        }
                    }
                    tem.append(line.charAt(curpos));
                    curpos++;
                }
                tem.append(line.charAt(curpos));
                curpos++;
                name = String.valueOf(tem);
                curLexType = LexType.STRCON;
            } else if (line.charAt(curpos) == '\'') {   // deal char
                StringBuilder tem = new StringBuilder(1024);
                tem.append(line.charAt(curpos));
                curpos++;
                while (curpos < line.length() && line.charAt(curpos) != '\'') {
                    if (line.charAt(curpos) == '\\') {
                        tem.append(line.charAt(curpos));
                        curpos++;
                        if (curpos >= line.length()) {
                            //TODO
                            //dont match more
                        }
                    }
                    tem.append(line.charAt(curpos));
                    curpos++;
                }
                tem.append(line.charAt(curpos));
                curpos++;
                name = String.valueOf(tem);
                curLexType = LexType.CHRCON;
            } else if (Character.isDigit(line.charAt(curpos))) {    // deal digit
                StringBuilder tem = new StringBuilder(1024);
                tem.append(line.charAt(curpos));
                curpos++;
                while (curpos < line.length() && Character.isDigit(line.charAt(curpos))) {
                    tem.append(line.charAt(curpos));
                    curpos++;
                }
                name = String.valueOf(tem);
                curLexType = LexType.INTCON;
            } else if (Character.isLetter(line.charAt(curpos)) || line.charAt(curpos) == '_') { //deal name
                StringBuilder tem = new StringBuilder(1024);
                tem.append(line.charAt(curpos));
                curpos++;
                while (curpos < line.length() && (Character.isLetter(line.charAt(curpos))
                        || Character.isDigit(line.charAt(curpos)) || line.charAt(curpos) == '_')) {
                    tem.append(line.charAt(curpos));
                    curpos++;
                }
                name = String.valueOf(tem);
                if (name.equals("main")) { // deal key word
                    curLexType = LexType.MAINTK;
                } else if (name.equals("const")) {
                    curLexType = LexType.CONSTTK;
                } else if (name.equals("int")) {
                    curLexType = LexType.INTTK;
                } else if (name.equals("char")) {
                    curLexType = LexType.CHARTK;
                } else if (name.equals("break")) {
                    curLexType = LexType.BREAKTK;
                } else if (name.equals("continue")) {
                    curLexType = LexType.CONTINUETK;
                } else if (name.equals("if")) {
                    curLexType = LexType.IFTK;
                } else if (name.equals("else")) {
                    curLexType = LexType.ELSETK;
                } else if (name.equals("for")) {
                    curLexType = LexType.FORTK;
                } else if (name.equals("getint")) {
                    curLexType = LexType.GETINTTK;
                } else if (name.equals("getchar")) {
                    curLexType = LexType.GETCHARTK;
                } else if (name.equals("printf")) {
                    curLexType = LexType.PRINTFTK;
                } else if (name.equals("return")) {
                    curLexType = LexType.RETURNTK;
                } else if (name.equals("void")) {
                    curLexType = LexType.VOIDTK;
                } else {
                    curLexType = LexType.IDENFR;
                }
            } else if(line.charAt(curpos) == '!') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '=') {
                        curpos++;
                        curLexType = LexType.NEQ;
                        name = "!=";
                    } else {
                        curLexType = LexType.NOT;
                        name = "!";
                    }
                }
            } else if (line.charAt(curpos) == '&') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '&') {
                        curpos++;
                        curLexType = LexType.AND;
                        name = "&&";
                    } else {
                        curLexType = LexType.AND;
                        name = "&";
                        needOutputerror = true;
                        errors.updateError(lineCount, 'a');
                    }
                }
            } else if (line.charAt(curpos) == '|') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '|') {
                        curpos++;
                        curLexType = LexType.OR;
                        name = "||";
                    } else {
                        curLexType = LexType.OR;
                        name = "|";
                        needOutputerror = true;
                        errors.updateError(lineCount, 'a');
                    }
                }
            } else if (line.charAt(curpos) == '+') {
                curLexType = LexType.PLUS;
                name = "+";
                curpos++;
            } else if (line.charAt(curpos) == '-') {
                curLexType = LexType.MINU;
                name = "-";
                curpos++;
            } else if (line.charAt(curpos) == '*') {
                curLexType = LexType.MULT;
                name = "*";
                curpos++;
            } else if (line.charAt(curpos) == '/') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '/') {
                        return;
                    } else if (line.charAt(curpos) == '*') {
                        matchComments = true;
                        curpos++;
                        String leftPart = dealComments(line.substring(curpos));
                        if (leftPart != null) {
                            parseLine(leftPart, lineCount);
                        }
                        return;
                    } else {
                        name = "/";
                        curLexType = LexType.DIV;
                    }
                }
            } else if (line.charAt(curpos) == '%') {
                curLexType = LexType.MOD;
                name = "%";
                curpos++;
            } else if (line.charAt(curpos) == ';') {
                curLexType = LexType.SEMICN;
                name = ";";
                curpos++;
            } else if (line.charAt(curpos) == ',') {
                curLexType = LexType.COMMA;
                name = ",";
                curpos++;
            } else if (line.charAt(curpos) == '(') {
                curLexType = LexType.LPARENT;
                name = "(";
                curpos++;
            } else if (line.charAt(curpos) == ')') {
                curLexType = LexType.RPARENT;
                name = ")";
                curpos++;
            } else if (line.charAt(curpos) == '[') {
                curLexType = LexType.LBRACK;
                name = "[";
                curpos++;
            } else if (line.charAt(curpos) == ']') {
                curLexType = LexType.RBRACK;
                name = "]";
                curpos++;
            } else if (line.charAt(curpos) == '{') {
                curLexType = LexType.LBRACE;
                name = "{";
                curpos++;
            } else if (line.charAt(curpos) == '}') {
                curLexType = LexType.RBRACE;
                name = "}";
                curpos++;
            } else if (line.charAt(curpos) == '<') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '=') {
                        name = "<=";
                        curLexType = LexType.LEQ;
                        curpos++;
                    } else {
                        name = "<";
                        curLexType = LexType.LSS;
                    }
                }
            } else if (line.charAt(curpos) == '>') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '=') {
                        name = ">=";
                        curLexType = LexType.GEQ;
                        curpos++;
                    } else {
                        name = ">";
                        curLexType = LexType.GRE;
                    }
                }
            } else if (line.charAt(curpos) == '=') {
                curpos++;
                if (curpos < line.length()) {
                    if (line.charAt(curpos) == '=') {
                        name = "==";
                        curLexType = LexType.EQL;
                        curpos++;
                    } else {
                        name = "=";
                        curLexType = LexType.ASSIGN;
                    }
                }
            }
            if (name == null) {
                int a;
            }
            tokens.add(new Token(name, curLexType, lineCount));
        }
    }

    private void outputTokens() {
        try (FileWriter writer = new FileWriter("lexer.txt", false)) {  // 使用 false 覆盖文件
            for (Token token : tokens) {
                writer.write(token.getLexType() + " " + token.getName() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputError(){
        errors.output();
    }
}
