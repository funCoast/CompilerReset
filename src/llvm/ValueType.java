package llvm;

public enum ValueType {
    // Value
    ArgumentTy, //参数
    BasicBlockTy, //基本块

    // Value -> Constant
    ConstantTy, //常量标识符
    ConstantDataTy, //字面量

    // Value -> Constant -> GlobalValue
    FunctionTy,
    GlobalVariableTy,

    // Value -> User -> Instruction
    BinaryOperatorTy,
    CompareInstTy,
    BranchInstTy,
    ReturnInstTy,
    StoreInstTy,
    CallInstTy,
    InputInstTy,
    OutputInstTy,
    AllocaInstTy,
    LoadInstTy,
    UnaryOperatorTy,
}
