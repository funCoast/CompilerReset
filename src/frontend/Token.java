package frontend;

public class Token {
    private String name;

    private LexType lexType;

    private int lineNumber;


    public Token(String name, LexType lexType, int lineNumber) {
        this.name = name;
        this.lexType = lexType;
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LexType getLexType() {
        return lexType;
    }

    public void setLexType(LexType lexType) {
        this.lexType = lexType;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getBType() {
        if (lexType == LexType.INTTK) {
            return 1;
        } else if (lexType == LexType.CHARTK) {
            return 2;
        } else if (lexType == LexType.VOIDTK) {
            return 3;
        } else {
            System.out.println("this is not BType,called by Token.getBType");
            return 0;
        }
    }
}
