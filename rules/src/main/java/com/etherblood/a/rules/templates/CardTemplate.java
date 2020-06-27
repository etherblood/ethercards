package com.etherblood.a.rules.templates;

import java.util.Arrays;

public class CardTemplate {

    private final int id;
    private final CardCast[] casts;

    protected CardTemplate(int templateId, CardCast[] casts) {
        this.id = templateId;
        this.casts = casts;
    }

    public int getId() {
        return id;
    }

    public CardCast getAttackPhaseCast() {
        return Arrays.stream(casts).filter(c -> c.isAttackCast()).findFirst().orElse(null);
    }

    public CardCast getBlockPhaseCast() {
        return Arrays.stream(casts).filter(c -> c.isBlockCast()).findFirst().orElse(null);
    }

    public CardCast[] getCasts() {
        return casts;
    }

    public String getTemplateName() {
        return "#" + id;
    }
}
