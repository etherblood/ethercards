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
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (health(minion) <= 0) {
                data.set(minion, core.DEATH_REQUEST, 1);
            }
        }
    }

    public int health(int minion) {
        int health = data.getOptional(minion, core.HEALTH).orElse(0);
        if (data.has(minion, core.IN_BATTLE_ZONE) && !data.has(minion, core.HERO)) {
            health += sumOwnerOtherMinionComponents(minion, core.OWN_MINIONS_HEALTH_AURA);
        }
        return health;
    }

    public int venom(int minion) {
        int venom = data.getOptional(minion, core.VENOM).orElse(0);
        if (data.has(minion, core.IN_BATTLE_ZONE) && !data.has(minion, core.HERO)) {
            venom += sumOwnerOtherMinionComponents(minion, core.OWN_MINIONS_VENOM_AURA);
        }
        return venom;
    }

    public boolean isFastToAttack(int minion) {
        if (data.has(minion, core.FAST_TO_ATTACK)) {
            return true;
        }
        if (data.has(minion, core.IN_BATTLE_ZONE) && !data.has(minion, core.HERO)) {
            if (hasOwnerOtherMinionWithComponent(minion, core.OWN_MINIONS_HASTE_AURA)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFastToDefend(int minion) {
        if (data.has(minion, core.FAST_TO_DEFEND)) {
            return true;
        }
        if (data.has(minion, core.IN_BATTLE_ZONE) && !data.has(minion, core.HERO)) {
            if (hasOwnerOtherMinionWithComponent(minion, core.OWN_MINIONS_HASTE_AURA)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOwnerOtherMinionWithComponent(int minion, int component) {
        int owner = data.get(minion, core.OWNED_BY);
        for (int other : data.list(component)) {
            if (other == minion) {
                continue;
            }
            if (!data.has(other, core.IN_BATTLE_ZONE)) {
                continue;
            }
            if (data.hasValue(other, core.OWNED_BY, owner)) {
                return true;
            }
        }
        return false;
    }

    private int sumOwnerOtherMinionComponents(int minion, int component) {
        int sum = 0;
        int owner = data.get(minion, core.OWNED_BY);
        for (int other : data.list(component)) {
            if (other == minion) {
                continue;
            }
            if (!data.has(other, core.IN_BATTLE_ZONE)) {
                continue;
            }
            if (data.hasValue(other, core.OWNED_BY, owner)) {
                sum += data.get(other, component);
            }
        }
        return sum;
    }
}
