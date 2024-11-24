package llvm;

import java.util.ArrayList;

public class Value {
    private String name;
    private ArrayList<Use> beenUsedList;
    private ArrayList<Use> useList;
    private ValueType valueType;
}
