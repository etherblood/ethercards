package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.rules.templates.effects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class LathlissTokenEffect extends Effect {

    @MinionId
    public final int tokenId;

    public LathlissTokenEffect(int tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (self == triggerTarget) {
            return;
        }
        int targetTemplateId = data.get(triggerTarget, core.MINION_TEMPLATE);
        MinionTemplate targetTemplate = templates.getMinion(targetTemplateId);
        Set<Tribe> tribes = targetTemplate.getTribes();
        if (tribes.contains(Tribe.DRAGON) && !tribes.contains(Tribe.TOKEN)) {
            int owner = data.get(self, core.OWNED_BY);
            SystemsUtil.summonMinion(data, templates, random, events, tokenId, owner);
        }
    }
}
