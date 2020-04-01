package com.etherblood.a.rules.templates;

public class CardTemplateBuilder {

    protected final int templateId;
    protected final CardCastBuilder attackPhaseCast = new CardCastBuilder(), blockPhaseCast = new CardCastBuilder();

    public CardTemplateBuilder(int templateId) {
        this.templateId = templateId;
    }

    public CardCastBuilder getAttackPhaseCast() {
        return attackPhaseCast;
    }

    public CardCastBuilder getBlockPhaseCast() {
        return blockPhaseCast;
    }

    public CardTemplate build() {
        return new CardTemplate(templateId, attackPhaseCast.build(), blockPhaseCast.build());
    }

}
