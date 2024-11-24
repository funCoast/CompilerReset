package llvm.instruction;


import llvm.RetType;

public class LLRegister {
    private int id;
    private int valueInt;
    private char valueChar;
    private RetType valueType;
    private RegisterType registerType;

    public LLRegister(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getValueInt() {
        return valueInt;
    }

    public char getValueChar() {
        return valueChar;
    }

    public RegisterType getRegisterType() {
        return registerType;
    }

    public void setValueType(RetType valueType) {
        this.valueType = valueType;
    }

    public void setRegister(int valueInt, char valueChar, RetType valueType, RegisterType registerType) {
        this.valueInt = valueInt;
        this.valueChar = valueChar;
        this.registerType = registerType;
        this.valueType = valueType;
    }

    public int getRealValue() {
        if (this.getRegisterType() == RegisterType.CHAR) {
            return this.getValueChar();
        } else {
            return this.getValueInt();
        }
    }

    public RetType getValueType() {
        return valueType;
    }

    public void setByReg(LLRegister llRegister) {
        this.id = llRegister.id;
        this.valueInt = llRegister.valueInt;
        this.valueChar = llRegister.valueChar;
        this.registerType = llRegister.registerType;
        this.valueType = llRegister.valueType;
    }

    public void assignByReg(LLRegister llRegister) {
        this.valueInt = llRegister.valueInt;
        this.valueChar = llRegister.valueChar;
        this.valueType = llRegister.valueType;
    }

    public void setRegisterType(RegisterType registerType) {
        this.registerType = registerType;
    }

    public void doNot() {
        if (registerType == RegisterType.NUM) {
            this.valueInt = -this.valueInt;
        } else if (registerType == RegisterType.CHAR) {
            this.valueChar =  (char) (-(int) this.valueChar);
        }
    }

    @Override
    public String toString() {
        if (registerType == RegisterType.NUM) {
            return  " " + valueInt;
        } else if (registerType == RegisterType.CHAR) {
            return " " + (int) valueChar;
        } else if (registerType == RegisterType.TEMP) {
            return " %" + id;
        } else if (registerType == RegisterType.GLOBAL) {
            return "* @" + (char) (id + 'a' - 1);
        } else if (registerType == RegisterType.POINT) {
            return "* %" + id; // * is put in Instr
        } else {
            System.out.println("Error: Register type wrong");
            return "Error";
        }
    }
}
