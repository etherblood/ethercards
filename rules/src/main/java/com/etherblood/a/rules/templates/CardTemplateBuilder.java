package com.etherblood.a.rules.templates;

import java.util.ArrayList;
import java.util.List;

public class CardTemplateBuilder {

    protected final List<CardCastBuilder> casts = new ArrayList<>();

    public CardCastBuilder newCast() {
        CardCastBuilder cast = new CardCastBuilder();
        casts.add(cast);
        return cast;
    }

    public CardTemplate build(int templateId) {
        return new CardTemplate(templateId, casts.stream().map(CardCastBuilder::build).toArray(CardCast[]::new));
    }

}
