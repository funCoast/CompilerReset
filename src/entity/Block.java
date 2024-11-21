package entity;

import java.util.ArrayList;

public class Block extends Stmt{
    ArrayList<BlockItem> blockItemArrayList;

    public Block(ArrayList<BlockItem> blockItemArrayList) {
        this.blockItemArrayList = blockItemArrayList;
    }

    public ArrayList<BlockItem> getBlockItemArrayList() {
        return blockItemArrayList;
    }
}
