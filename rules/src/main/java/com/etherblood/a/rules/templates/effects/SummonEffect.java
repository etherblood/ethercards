package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.rules.templates.effects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class SummonEffect extends Effect {

    @MinionId
    public final int minionId;
    public final boolean fast;

    public SummonEffect(int minionId, boolean fast) {
        this.minionId = minionId;
        this.fast = fast;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        // haste-aura check before summoning to exclude summon buffing itself
        boolean addSickness = !fast && data.list(core.OWN_MINIONS_HASTE_AURA).stream().noneMatch(minion -> data.hasValue(minion, core.OWNED_BY, owner));
        int summon = SystemsUtil.createMinion(settings, data, random, minionId, owner);
        if (addSickness) {
            data.set(summon, core.SUMMONING_SICKNESS, 1);
        }
    }
}
