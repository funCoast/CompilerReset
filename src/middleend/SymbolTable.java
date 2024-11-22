package middleend;

import java.util.ArrayList;

public class SymbolTable {
    private ArrayList<Symbol> symbolArrayList;
    private int id;

    public SymbolTable(int id) {
        this.symbolArrayList = new ArrayList<>();
        this.id = id;
    }

    public ArrayList<Symbol> getSymbolArrayList() {
        return symbolArrayList;
    }
}
