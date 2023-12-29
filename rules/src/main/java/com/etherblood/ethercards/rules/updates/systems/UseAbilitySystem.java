package com.etherblood.ethercards.rules.updates.systems;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.ActivatedAbility;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Effect;

import java.util.function.IntUnaryOperator;

public class UseAbilitySystem {

    private final EntityData data;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final CoreComponents core;

    public UseAbilitySystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.core = data.getSchema().getModule(CoreComponents.class);
    }

    public void run() {
        EntityList abilityUsers = data.list(core.USE_ABILITY_TARGET);
        for (int abilityUser : abilityUsers) {
            int cardTemplateId = data.get(abilityUser, core.CARD_TEMPLATE);
            int target = data.get(abilityUser, core.USE_ABILITY_TARGET);
            CardTemplate template = templates.getCard(cardTemplateId);
            ActivatedAbility ability = template.getActiveZone(abilityUser, data).getActivated();
            if (ability == null) {
                throw new AssertionError();
            }
            if (target == ~0) {
                assert ability.getTarget().isValidTarget(data, templates, abilityUser, null);
            } else {
                assert ability.getTarget().isValidTarget(data, templates, abilityUser, target);
            }
            int owner = data.get(abilityUser, core.OWNER);
            Integer manaCost = ability.getManaCost();
            if (manaCost != null && manaCost != 0) {
                int mana = data.get(owner, core.MANA);
                mana -= manaCost;
                if (mana < 0) {
                    throw new IllegalStateException();
                }
                data.set(owner, core.MANA, mana);
            }
            if (ability.isSelfTap()) {
                assert !data.has(abilityUser, core.TIRED);
                data.set(abilityUser, core.TIRED, 1);
            }
            for (Effect effect : ability.getEffects()) {
                effect.apply(data, templates, random, events, abilityUser, target);
            }

        }
        data.clear(core.USE_ABILITY_TARGET);
    }
}
