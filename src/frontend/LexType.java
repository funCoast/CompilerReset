package frontend;

public enum LexType {
    //初始阶段
    START,

    // 标识符
    IDENFR,

    // 常量
    INTCON,         // 整数常量
    STRCON,         // 字符串常量
    CHRCON,         // 字符常量

    // 关键字
    MAINTK,         // main
    CONSTTK,        // const
    INTTK,          // int
    CHARTK,         // char
    BREAKTK,        // break
    CONTINUETK,     // continue
    IFTK,           // if
    ELSETK,         // else
    FORTK,          // for
    GETINTTK,       // getint
    GETCHARTK,      // getchar
    PRINTFTK,       // printf
    RETURNTK,       // return
    VOIDTK,         // void

    // 操作符和分隔符
    NOT,            // !
    AND,            // &&
    OR,             // ||
    PLUS,           // +
    MINU,           // -
    MULT,           // *
    DIV,            // /
    MOD,            // %
    LSS,            // <
    LEQ,            // <=
    GRE,            // >
    GEQ,            // >=
    EQL,            // ==
    NEQ,            // !=
    ASSIGN,         // =
    SEMICN,         // ;
    COMMA,          // ,
    LBRACE,         // {
    RBRACE,         // }
    LBRACK,         // [
    RBRACK,         // ]
    LPARENT,         // (
    RPARENT,          // )
}
