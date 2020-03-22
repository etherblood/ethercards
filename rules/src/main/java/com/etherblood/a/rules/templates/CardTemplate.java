package com.etherblood.a.rules.templates;

public class CardTemplate {

    private final int id;
    private final CardCast attackPhaseCast, blockPhaseCast;

    protected CardTemplate(int templateId, CardCast attackPhaseCast, CardCast blockPhaseCast) {
        this.id = templateId;
        this.attackPhaseCast = attackPhaseCast;
        this.blockPhaseCast = blockPhaseCast;
    }

    public int getId() {
        return id;
    }

    public CardCast getAttackPhaseCast() {
        return attackPhaseCast;
    }

    public CardCast getBlockPhaseCast() {
        return blockPhaseCast;
    }
}
