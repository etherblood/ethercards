package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.ZoneState;
import java.util.OptionalInt;

public class EffectiveStatsService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;

    public EffectiveStatsService(EntityData data, GameTemplates templates) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
    }

    public int attack(int minion) {
        int attack = data.getOptional(minion, core.ATTACK).orElse(0);
        attack += data.getOptional(minion, core.TEMPORARY_ATTACK).orElse(0);
        OptionalInt templateId = data.getOptional(minion, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            CardTemplate template = templates.getCard(templateId.getAsInt());
            StatModifier modifier = template.getBattle().getStatModifiers().get(core.ATTACK);
            if (modifier != null) {
                attack = modifier.modify(data, templates, minion, minion, attack);
            }
        }
        attack = applyAuras(minion, core.ATTACK_AURA, attack);
        return attack;
    }

    public int health(int minion) {
        int health = data.getOptional(minion, core.HEALTH).orElse(0);
        health += data.getOptional(minion, core.TEMPORARY_HEALTH).orElse(0);
        OptionalInt templateId = data.getOptional(minion, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            CardTemplate template = templates.getCard(templateId.getAsInt());
            StatModifier modifier = template.getBattle().getStatModifiers().get(core.HEALTH);
            if (modifier != null) {
                health = modifier.modify(data, templates, minion, minion, health);
            }
        }
        health = applyAuras(minion, core.HEALTH_AURA, health);
        return health;
    }

    public int manaPool(int player) {
        int manaPool = data.getOptional(player, core.MANA_POOL).orElse(0);
        for (int minion : data.list(core.MANA_POOL_AURA)) {
            if (data.hasValue(minion, core.OWNER, player)) {
                manaPool += data.get(minion, core.MANA_POOL_AURA);
            }
        }
        return manaPool;
    }

    public boolean hasVigilance(int minion) {
        int vigilance = data.getOptional(minion, core.VIGILANCE).orElse(0);
        OptionalInt templateId = data.getOptional(minion, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            CardTemplate template = templates.getCard(templateId.getAsInt());
            StatModifier modifier = template.getBattle().getStatModifiers().get(core.VIGILANCE);
            if (modifier != null) {
                vigilance = modifier.modify(data, templates, minion, minion, vigilance);
            }
        }
        return vigilance > 0;
    }

    public boolean hasFlying(int minion) {
        int flying = data.getOptional(minion, core.FLYING).orElse(0);
        OptionalInt templateId = data.getOptional(minion, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            CardTemplate template = templates.getCard(templateId.getAsInt());
            StatModifier modifier = template.getBattle().getStatModifiers().get(core.FLYING);
            if (modifier != null) {
                flying = modifier.modify(data, templates, minion, minion, flying);
            }
        }
        return flying > 0;
    }

    public int venom(int minion) {
        int venom = data.getOptional(minion, core.VENOM).orElse(0);
        venom = applyAuras(minion, core.VENOM_AURA, venom);
        return venom;
    }

    public boolean isFastToAttack(int minion) {
        if (data.has(minion, core.FAST_TO_ATTACK)) {
            return true;
        }
        return applyAuras(minion, core.HASTE_AURA, 0) > 0;
    }

    public boolean isFastToDefend(int minion) {
        if (data.has(minion, core.FAST_TO_DEFEND)) {
            return true;
        }
        return applyAuras(minion, core.HASTE_AURA, 0) > 0;
    }
    
    public boolean isHexProof(int entity) {
        int hexProof = data.getOptional(entity, core.HEXPROOF).orElse(0);
        OptionalInt templateId = data.getOptional(entity, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            CardTemplate template = templates.getCard(templateId.getAsInt());
            StatModifier modifier = template.getBattle().getStatModifiers().get(core.HEXPROOF);
            if (modifier != null) {
                hexProof = modifier.modify(data, templates, entity, entity, hexProof);
            }
        }
        return hexProof > 0;
    }

    public boolean preventCombatDamage(int entity) {
        return applyAuras(entity, core.PREVENT_COMBAT_DAMAGE_AURA, 0) > 0;
    }

    private int applyAuras(int self, int auraComponent, int stat) {
        for (int other : data.listInValueOrder(auraComponent)) {
            CardTemplate otherTemplate = templates.getCard(data.get(other, core.CARD_TEMPLATE));
            ZoneState otherZone = otherTemplate.getActiveZone(other, data);
            StatModifier modifier = otherZone.getStatModifiers().get(auraComponent);
            if (modifier != null) {
                stat = modifier.modify(data, templates, other, self, stat);
            }
        }
        return stat;
    }
}
