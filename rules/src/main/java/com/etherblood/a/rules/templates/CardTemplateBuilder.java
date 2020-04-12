package com.etherblood.a.rules.templates;

public class CardTemplateBuilder {

    protected final CardCastBuilder attackPhaseCast = new CardCastBuilder(), blockPhaseCast = new CardCastBuilder();

    public CardCastBuilder getAttackPhaseCast() {
        return attackPhaseCast;
    }

    public CardCastBuilder getBlockPhaseCast() {
        return blockPhaseCast;
    }

    public CardTemplate build(int templateId) {
        return new CardTemplate(templateId, attackPhaseCast.build(), blockPhaseCast.build());
    }

}
