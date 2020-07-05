package com.etherblood.a.templates.instances;

import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.effects.SelfSummonEffect;
import com.etherblood.a.rules.templates.effects.SoulshiftEffect;
import com.etherblood.a.templates.CardColor;
import com.etherblood.a.templates.DisplayCardTemplateBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class KodamaOfTheCenterTree {

    public static DisplayCardTemplateBuilder builder(Components components, Map<String, Integer> cardAliases) {
        CoreComponents core = components.getModule(CoreComponents.class);
        DisplayCardTemplateBuilder builder = new DisplayCardTemplateBuilder();
        builder.setName("Kodama of the Center Tree");
        builder.setAlias("kodama_of_the_center_tree");
        builder.setImagePath("kodama_of_the_center_tree.jpg");
        builder.setManaCost(5);
        builder.setColors(Arrays.asList(CardColor.GREEN));
        builder.addTribe(Tribe.SPIRIT);
        builder.set(core.ATTACK, 0);
        builder.set(core.HEALTH, 0);
        builder.modifyAttack(KodamaOfTheCenterTree::modifyStat);
        builder.modifyHealth(KodamaOfTheCenterTree::modifyStat);
        builder.onDeath(KodamaOfTheCenterTree::onDeath);
        builder.setDescription("Kodama of the Center Tree's power and toughness are each equal to the number of Spirits you control.\n"
                + "Kodama of the Center Tree has soulshift X, where X is the number of Spirits you control.");
        builder.newCast().addEffect(new SelfSummonEffect());
        return builder;
    }

    private static void onDeath(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        int power = modifyStat(data, templates, self, 0);
        SoulshiftEffect soulshiftEffect = new SoulshiftEffect(power);
        soulshiftEffect.apply(data, templates, random, events, self, triggerTarget);
    }

    private static int modifyStat(EntityData data, GameTemplates templates, int self, int stat) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNED_BY);
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.hasValue(minion, core.OWNED_BY, owner)) {
                CardTemplate template = templates.getCard(data.get(minion, core.CARD_TEMPLATE));
                if (template.getTribes().contains(Tribe.SPIRIT)) {
                    stat++;
                }
            }
        }
        return stat;
    }
}
