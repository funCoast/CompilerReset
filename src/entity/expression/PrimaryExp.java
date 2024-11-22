package entity.expression;

import entity.LVal;

public class PrimaryExp {
    Exp exp;
    LVal lVal;
    Integer number;
    Character character;
    PriExpType priExpType;


    public PrimaryExp(Exp exp, LVal lVal, Integer number,
                      Character character, PriExpType priExpType) {
        this.exp = exp;
        this.lVal = lVal;
        this.number = number;
        this.character = character;
        this.priExpType = priExpType;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Integer getNumber() {
        return number;
    }

    public Character getCharacter() {
        return character;
    }

    public PriExpType getPriExpType() {
        return priExpType;
    }
}
