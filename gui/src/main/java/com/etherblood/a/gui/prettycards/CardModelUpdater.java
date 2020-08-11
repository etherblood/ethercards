package com.etherblood.a.gui.prettycards;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.EffectiveStatsService;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.templates.api.DisplayCardTemplate;
import com.etherblood.a.templates.implementation.effects.SoulshiftEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CardModelUpdater {

    public void updateFromTemplate(CardModel model, DisplayCardTemplate template, CoreComponents core) {
        IntMap components = template.getBattle().getComponents();
        model.setTemplate(template);
        if (components.hasKey(core.ATTACK)) {
            model.setAttack(components.get(core.ATTACK));
        } else {
            model.setAttack(null);
        }
        if (components.hasKey(core.HEALTH)) {
            model.setHealth(components.get(core.HEALTH));
        } else {
            model.setHealth(null);
        }
        model.setDamaged(false);
        model.setInBattleZone(false);

        List<String> keywords = new ArrayList<>();
        if (components.hasKey(core.TRAMPLE)) {
            keywords.add("Trample");
        }
        if (components.hasKey(core.LIFELINK)) {
            keywords.add("Lifelink");
        }
        if (components.hasKey(core.VIGILANCE)) {
            keywords.add("Vigilance");
        }
        if (components.hasKey(core.FLYING)) {
            keywords.add("Flying");
        }
        if (components.hasKey(core.REACH)) {
            keywords.add("Reach");
        }
        if (components.hasKey(core.VENOM)) {
            keywords.add("Venom_" + components.get(core.VENOM));
        }
        if (components.hasKey(core.POISONED)) {
            keywords.add("Poisoned_" + components.get(core.POISONED));
        }
        if (components.hasKey(core.BUSHIDO)) {
            keywords.add("Bushido_" + components.get(core.BUSHIDO));
        }
        if (components.hasKey(core.INDESTRUCTIBLE)) {
            keywords.add("Indestructible");
        }
        if (components.hasKey(core.MANA_POOL_AURA)) {
            keywords.add("Mana_Pool_" + components.get(core.MANA_POOL_AURA));
        }
        if (components.hasKey(core.MANA_POOL_AURA_GROWTH)) {
            keywords.add("Mana_Growth_" + components.get(core.MANA_POOL_AURA_GROWTH));
        }
        if (components.hasKey(core.DRAWS_PER_TURN)) {
            keywords.add("Draws_per_Turn_" + components.get(core.DRAWS_PER_TURN));
        }
        if (components.hasKey(core.DRAWS_ON_ATTACK)) {
            keywords.add("Draws_on_Attack_" + components.get(core.DRAWS_ON_ATTACK));
        }
        if (components.hasKey(core.GIVE_DRAWS_ON_ATTACK)) {
            keywords.add("Opponent_Draws_on_Attack_" + components.get(core.GIVE_DRAWS_ON_ATTACK));
        }
        if (components.hasKey(core.DRAWS_ON_BLOCK)) {
            keywords.add("Draws_on_Block_" + components.get(core.DRAWS_ON_BLOCK));
        }
        if (components.hasKey(core.CANNOT_ATTACK)) {
            keywords.add("Cannot_attack");
        }
        if (components.hasKey(core.CANNOT_BLOCK)) {
            keywords.add("Cannot_block");
        }
        if (components.hasKey(core.CANNOT_BE_ATTACKED)) {
            keywords.add("Cannot_be_attacked");
        }
        if (components.hasKey(core.CANNOT_BE_BLOCKED)) {
            keywords.add("Cannot_be_blocked");
        }
        if (components.hasKey(core.HEXPROOF)) {
            keywords.add("Hexproof");
        }
        if (components.hasKey(core.RAGE)) {
            keywords.add("Rage");
        }
        if (components.hasKey(core.FAST_TO_ATTACK) && components.hasKey(core.FAST_TO_DEFEND)) {
            keywords.add("Haste");
        } else if (components.hasKey(core.FAST_TO_ATTACK)) {
            keywords.add("Fast_Attacker");
        } else if (components.hasKey(core.FAST_TO_DEFEND)) {
            keywords.add("Fast_Blocker");
        }
        if (components.hasKey(core.FATIGUE)) {
            keywords.add("Fatigue_" + components.get(core.FATIGUE));
        }
        if (components.hasKey(core.REGENERATION)) {
            keywords.add("Regeneration_" + components.get(core.REGENERATION));
        }

        if (template.getHand().getComponents().hasKey(core.NINJUTSU)) {
            keywords.add("Ninjutsu_" + template.getHand().getComponents().get(core.NINJUTSU));
        }

        List<Effect> deathEffects = template.getBattle().getPassive().getOrDefault(core.TRIGGER_SELF_DEATH, Collections.emptyList());
        for (Effect effect : deathEffects) {
            if (effect instanceof SoulshiftEffect) {
                SoulshiftEffect soulshift = (SoulshiftEffect) effect;
                keywords.add("Soulshift_" + soulshift.power);
            }
        }
        keywords.sort(Comparator.naturalOrder());
        model.setKeywords(keywords);
    }

    public void updateFromData(CardModel model, GameTemplates templates, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int entityId = model.getEntityId();
        DisplayCardTemplate template = (DisplayCardTemplate) templates.getCard(data.get(entityId, core.CARD_TEMPLATE));
        model.setTemplate(template);
        if (!data.has(entityId, core.IN_BATTLE_ZONE)) {
            updateFromTemplate(model, template, core);
            return;
        }
        model.setInBattleZone(true);
        List<String> keywords = new ArrayList<>();
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        model.setFoil(data.has(entityId, core.HERO));
        if (data.has(entityId, core.ATTACK)) {
            model.setAttack(stats.attack(entityId));
        } else {
            model.setAttack(null);
        }
        if (data.has(entityId, core.HEALTH)) {
            model.setHealth(stats.health(entityId));
            model.setDamaged(data.get(entityId, core.HEALTH) < template.getBattle().getComponents().get(core.HEALTH));
        } else {
            model.setHealth(null);
            model.setDamaged(false);
        }
        if (data.has(entityId, core.TRAMPLE)) {
            keywords.add("Trample");
        }
        if (data.has(entityId, core.LIFELINK)) {
            keywords.add("Lifelink");
        }
        if (stats.hasVigilance(entityId)) {
            keywords.add("Vigilance");
        }
        if (data.has(entityId, core.FLYING)) {
            keywords.add("Flying");
        }
        if (data.has(entityId, core.REACH)) {
            keywords.add("Reach");
        }
        if (stats.venom(entityId) != 0) {
            keywords.add("Venom_" + stats.venom(entityId));
        }
        if (data.has(entityId, core.POISONED)) {
            keywords.add("Poisoned_" + data.get(entityId, core.POISONED));
        }
        if (data.has(entityId, core.BUSHIDO)) {
            keywords.add("Bushido_" + data.get(entityId, core.BUSHIDO));
        }
        if (data.has(entityId, core.INDESTRUCTIBLE)) {
            keywords.add("Indestructible");
        }
        if (data.has(entityId, core.MANA_POOL_AURA)) {
            keywords.add("Mana_Pool_" + data.get(entityId, core.MANA_POOL_AURA));
        }
        if (data.has(entityId, core.MANA_POOL_AURA_GROWTH)) {
            keywords.add("Mana_Growth_" + data.get(entityId, core.MANA_POOL_AURA_GROWTH));
        }
        if (data.has(entityId, core.DRAWS_PER_TURN)) {
            keywords.add("Draws_per_Turn_" + data.get(entityId, core.DRAWS_PER_TURN));
        }
        if (data.has(entityId, core.DRAWS_ON_ATTACK)) {
            keywords.add("Draws_on_Attack_" + data.get(entityId, core.DRAWS_ON_ATTACK));
        }
        if (data.has(entityId, core.GIVE_DRAWS_ON_ATTACK)) {
            keywords.add("Opponent_Draws_on_Attack_" + data.get(entityId, core.GIVE_DRAWS_ON_ATTACK));
        }
        if (data.has(entityId, core.DRAWS_ON_BLOCK)) {
            keywords.add("Draws_on_Block_" + data.get(entityId, core.DRAWS_ON_BLOCK));
        }
        if (data.has(entityId, core.CANNOT_ATTACK)) {
            keywords.add("Cannot_attack");
        }
        if (data.has(entityId, core.CANNOT_BLOCK)) {
            keywords.add("Cannot_block");
        }
        if (data.has(entityId, core.CANNOT_BE_ATTACKED)) {
            keywords.add("Cannot_be_attacked");
        }
        if (data.has(entityId, core.CANNOT_BE_BLOCKED)) {
            keywords.add("Cannot_be_blocked");
        }
        if (data.has(entityId, core.HEXPROOF)) {
            keywords.add("Hexproof");
        }
        if (data.has(entityId, core.RAGE)) {
            keywords.add("Rage");
        }
        if (stats.isFastToAttack(entityId) && stats.isFastToDefend(entityId)) {
            keywords.add("Haste");
        } else if (stats.isFastToAttack(entityId)) {
            keywords.add("Fast_Attacker");
        } else if (stats.isFastToDefend(entityId)) {
            keywords.add("Fast_Blocker");
        }
        if (data.has(entityId, core.FATIGUE)) {
            keywords.add("Fatigue_" + data.get(entityId, core.FATIGUE));
        }
        if (data.has(entityId, core.REGENERATION)) {
            keywords.add("Regeneration_" + data.get(entityId, core.REGENERATION));
        }

        if (data.has(entityId, core.NINJUTSU)) {
            keywords.add("Ninjutsu_" + data.get(entityId, core.NINJUTSU));
        } else if (!data.has(entityId, core.IN_HAND_ZONE) && template.getHand().getComponents().hasKey(core.NINJUTSU)) {
            keywords.add("Ninjutsu_" + template.getHand().getComponents().get(core.NINJUTSU));
        }

        Map<Integer, List<Effect>> triggers = template.getBattle().getPassive();
        List<Effect> effects = triggers.getOrDefault(core.TRIGGER_SELF_DEATH, Collections.emptyList());
        for (Effect effect : effects) {
            if (effect instanceof SoulshiftEffect) {
                SoulshiftEffect soulshift = (SoulshiftEffect) effect;
                keywords.add("Soulshift_" + soulshift.power);
            }
        }
        keywords.sort(Comparator.naturalOrder());
        model.setKeywords(keywords);
    }
}
