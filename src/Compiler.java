import entity.CompUnit;
import frontend.CompError;
import frontend.Lexer;
import frontend.Parser;
import frontend.Token;
import middleend.Visitor;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws Exception {
        CompError errors = new CompError();
        Lexer lexer = new Lexer(errors);
        ArrayList<Token> tokens = lexer.lexicalAnalysis();
        //
        Parser parser = new Parser(tokens, errors);
        CompUnit compUnit = parser.parseCompUnit();
        //
        Visitor visitor = new Visitor(compUnit, errors);
        visitor.visit();
    }
}
