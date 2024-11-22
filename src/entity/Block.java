package entity;

import java.util.ArrayList;

public class Block extends Stmt{
    ArrayList<BlockItem> blockItemArrayList;
    int rBraceLine;

    public Block(ArrayList<BlockItem> blockItemArrayList, int rBraceLine) {
        this.blockItemArrayList = blockItemArrayList;
        this.rBraceLine = rBraceLine;
    }

    public ArrayList<BlockItem> getBlockItemArrayList() {
        return blockItemArrayList;
    }

    public int getrBraceLine() {
        return rBraceLine;
    }
}
