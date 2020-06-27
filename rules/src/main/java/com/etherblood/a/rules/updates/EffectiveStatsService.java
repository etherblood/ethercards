package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;

public class EffectiveStatsService {

    private final EntityData data;
    private final CoreComponents core;

    public EffectiveStatsService(EntityData data) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
    }

    public void killHealthless() {
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (health(minion) <= 0) {
                data.set(minion, core.DEATH_REQUEST, 1);
            }
        }
    }

    public int health(int minion) {
        assert data.has(minion, core.IN_BATTLE_ZONE);
        return data.getOptional(minion, core.HEALTH).orElse(0);
    }

    public boolean isFastToAttack(int minion) {
        assert data.has(minion, core.IN_BATTLE_ZONE);
        if (data.has(minion, core.FAST_TO_ATTACK)) {
            return true;
        }
        if (!data.has(minion, core.HERO)) {
            int owner = data.get(minion, core.OWNED_BY);
            for (int other : data.list(core.OWN_MINIONS_HASTE_AURA)) {
                if (!data.has(other, core.IN_BATTLE_ZONE)) {
                    continue;
                }
                if (data.hasValue(other, core.OWNED_BY, owner)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFastToDefend(int minion) {
        assert data.has(minion, core.IN_BATTLE_ZONE);
        if (data.has(minion, core.FAST_TO_DEFEND)) {
            return true;
        }
        if (!data.has(minion, core.HERO)) {
            int owner = data.get(minion, core.OWNED_BY);
            for (int other : data.list(core.OWN_MINIONS_HASTE_AURA)) {
                if (!data.has(other, core.IN_BATTLE_ZONE)) {
                    continue;
                }
                if (data.hasValue(other, core.OWNED_BY, owner)) {
                    return true;
                }
            }
        }
        return false;
    }
}
