package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.StatModifier;

public class EffectiveStatsService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;

    public EffectiveStatsService(EntityData data, GameTemplates templates) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
    }

    public int manaPool(int player) {
        int manaPool = data.getOptional(player, core.MANA_POOL).orElse(0);
        for (int minion : data.list(core.MANA_POOL_AURA)) {
            if (data.hasValue(minion, core.OWNED_BY, player)) {
                manaPool += data.get(minion, core.MANA_POOL_AURA);
            }
        }
        return manaPool;
    }

    public void killHealthless() {
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (health(minion) <= 0 && !data.has(minion, core.INDESTRUCTIBLE)) {
                data.set(minion, core.DEATH_REQUEST, 1);
            }
        }
    }

    public void unbindFreedMinions() {
        for (int minion : data.listInValueOrder(core.BOUND_TO)) {
            int bindTarget = data.get(minion, core.BOUND_TO);
            if(!data.has(bindTarget, core.IN_BATTLE_ZONE)) {
                data.set(minion, core.OWNED_BY, data.get(minion, core.ORIGINALLY_OWNED_BY));
                data.remove(minion, core.ORIGINALLY_OWNED_BY);
                data.remove(minion, core.BOUND_TO);
            }
        }
    }

    public int attack(int minion) {
        int attack = data.getOptional(minion, core.ATTACK).orElse(0);
        attack += data.getOptional(minion, core.TEMPORARY_ATTACK).orElse(0);
        CardTemplate template = templates.getCard(data.get(minion, core.CARD_TEMPLATE));
        for (StatModifier attackModifier : template.getComponentModifiers(core.ATTACK)) {
            attack = attackModifier.modify(data, templates, minion, attack);
        }
        return attack;
    }

    public boolean hasVigilance(int minion) {
        int vigilance = data.getOptional(minion, core.VIGILANCE).orElse(0);
        CardTemplate template = templates.getCard(data.get(minion, core.CARD_TEMPLATE));
        for (StatModifier modifier : template.getComponentModifiers(core.VIGILANCE)) {
            vigilance = modifier.modify(data, templates, minion, vigilance);
        }
        return vigilance >= 1;
    }

    public int health(int minion) {
        int health = data.getOptional(minion, core.HEALTH).orElse(0);
        health += data.getOptional(minion, core.TEMPORARY_HEALTH).orElse(0);
        CardTemplate template = templates.getCard(data.get(minion, core.CARD_TEMPLATE));
        for (StatModifier healthModifier : template.getComponentModifiers(core.HEALTH)) {
            health = healthModifier.modify(data, templates, minion, health);
        }
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
