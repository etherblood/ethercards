package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.Tribe;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.CardId;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class LathlissTokenEffect implements Effect {

    @CardId
    public final int tokenId;

    public LathlissTokenEffect(int tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        if (self == triggerTarget) {
            return;
        }
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNER);
        if (!data.hasValue(triggerTarget, core.OWNER, owner)) {
            return;
        }
        int targetTemplateId = data.get(triggerTarget, core.CARD_TEMPLATE);
        CardTemplate targetTemplate = templates.getCard(targetTemplateId);
        Set<Tribe> tribes = targetTemplate.getTribes();
        if (tribes.contains(Tribe.DRAGON) && !tribes.contains(Tribe.TOKEN)) {
            SystemsUtil.summonMinion(data, templates, random, events, tokenId, owner);
        }
    }
}
