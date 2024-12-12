package llvm.instruction;


import llvm.RetType;

import java.util.ArrayList;

public class LLRegister {
    private Label id;
    private String name;
    private int valueInt;
    private char valueChar;
    private RetType valueType;
    private RegisterType registerType;
    private ArrayList<Integer> valueArrayList;
    private boolean isConst;

    public void setId(Label id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public void setValueChar(char valueChar) {
        this.valueChar = valueChar;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public LLRegister(int id) {
        this.id = new Label(id);
    }

    public int getId() {
        return id.getId();
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

    public void setRegister(int valueInt, char valueChar, RetType valueType, RegisterType registerType) {
        this.valueInt = valueInt;
        this.valueChar = valueChar;
        this.registerType = registerType;
        this.valueType = valueType;
    }


    public void setRegister(int valueInt, char valueChar, RetType valueType, RegisterType registerType, String name) {
        this.valueInt = valueInt;
        this.valueChar = valueChar;
        this.registerType = registerType;
        this.valueType = valueType;
        this.name = name;
    }

    public int getRealValue() {
        if (this.valueType == RetType.i8) {
            return this.getValueChar();
        } else {
            return this.getValueInt();
        }
    }

    public RetType getValueType() {
        return valueType;
    }

    public ArrayList<Integer> getValueArrayList() {
        return valueArrayList;
    }

    public int getValueByIndex(int index) {
        return valueArrayList.get(index);
    }

    public void setValueArrayList(ArrayList<Integer> valueArrayList) {
        this.valueArrayList = valueArrayList;
    }

    public void setByReg(LLRegister llRegister) {
        this.id = llRegister.id;
        this.valueInt = llRegister.valueInt;
        this.valueChar = llRegister.valueChar;
        this.registerType = llRegister.registerType;
        this.valueType = llRegister.valueType;
        this.name = llRegister.name;
    }

    public void assignByReg(LLRegister llRegister) {
        this.valueInt = llRegister.valueInt;
        this.valueChar = llRegister.valueChar;
        this.valueType = llRegister.valueType;
    }

    public void setValueType(RetType valueType) {
        this.valueType = valueType;
    }

    public void setRegisterType(RegisterType registerType) {
        this.registerType = registerType;
    }

    public void doSubSelf() {
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
            return "* @" + name;
        } else if (registerType == RegisterType.POINT || registerType == RegisterType.P_POINT) {
            return "* %" + id; // * is put in Instr
        } else {
            System.out.println("Error: Register type wrong");
            return "Error";
        }
    }

    public void setId(int id) {
        this.id.setId(id);
    }
}
