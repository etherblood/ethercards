package com.etherblood.a.templates.implementation;

import com.etherblood.a.templates.implementation.effects.TargetedEffects;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.implementation.effects.BuffEffect;
import com.etherblood.a.templates.implementation.effects.CardDestructionEffect;
import com.etherblood.a.templates.implementation.effects.CreateCardEffect;
import com.etherblood.a.templates.implementation.effects.DebuffEffect;
import com.etherblood.a.templates.implementation.effects.DrawCardTemplateEffect;
import com.etherblood.a.templates.implementation.effects.FractionalDamageEffect;
import com.etherblood.a.templates.implementation.effects.HandCardCountActivatedEffects;
import com.etherblood.a.templates.implementation.effects.KolaghanDamageEffect;
import com.etherblood.a.templates.implementation.effects.LathlissTokenEffect;
import com.etherblood.a.templates.implementation.effects.ParticleEventEffect;
import com.etherblood.a.templates.implementation.effects.ResurrectRandomEffect;
import com.etherblood.a.templates.implementation.effects.SelfDiscardEffect;
import com.etherblood.a.templates.implementation.effects.SelfSummonEffect;
import com.etherblood.a.templates.implementation.effects.SoulshiftEffect;
import com.etherblood.a.templates.implementation.effects.SpiritCountSoulshiftEffect;
import com.etherblood.a.templates.implementation.effects.SummonEffect;
import com.etherblood.a.templates.implementation.effects.TakeControlEffect;
import com.etherblood.a.templates.implementation.effects.TargetComponentActivatedEffects;
import com.etherblood.a.templates.implementation.effects.TargetOwnedActivatedEffects;
import com.etherblood.a.templates.implementation.effects.TribeActivatedEffects;
import com.etherblood.a.templates.implementation.statmodifiers.AddFractionalModifier;
import com.etherblood.a.templates.implementation.statmodifiers.AddOnOpponentThresholdModifier;
import com.etherblood.a.templates.implementation.statmodifiers.AddOnOwnTurnModifier;
import com.etherblood.a.templates.implementation.statmodifiers.AddOwnHandCardCountModifier;
import com.etherblood.a.templates.implementation.statmodifiers.AddOwnManaPoolModifier;
import com.etherblood.a.templates.implementation.statmodifiers.AddOwnSpiritCountModifier;
import com.etherblood.a.templates.implementation.statmodifiers.OwnHandCardCountActivatedModifier;
import java.util.HashMap;
import java.util.Map;

public class TemplateAliasMaps {
    
    public Map<String, Class<? extends Effect>> getEffects() {
        Map<String, Class<? extends Effect>> effectClasses = new HashMap<>();
        effectClasses.put("summon", SummonEffect.class);
        effectClasses.put("selfSummon", SelfSummonEffect.class);
        effectClasses.put("selfDiscard", SelfDiscardEffect.class);
        effectClasses.put("lathlissToken", LathlissTokenEffect.class);
        effectClasses.put("fractionalDamage", FractionalDamageEffect.class);
        effectClasses.put("buff", BuffEffect.class);
        effectClasses.put("debuff", DebuffEffect.class);
        effectClasses.put("create", CreateCardEffect.class);
        effectClasses.put("targeted", TargetedEffects.class);
        effectClasses.put("particle", ParticleEventEffect.class);
        effectClasses.put("drawTemplate", DrawCardTemplateEffect.class);
        effectClasses.put("takeControl", TakeControlEffect.class);
        effectClasses.put("soulshift", SoulshiftEffect.class);
        effectClasses.put("spiritCountSoulshift", SpiritCountSoulshiftEffect.class);
        effectClasses.put("cardDestruction", CardDestructionEffect.class);
        effectClasses.put("resurrectRandom", ResurrectRandomEffect.class);
        effectClasses.put("kolaghanDamage", KolaghanDamageEffect.class);
        effectClasses.put("tribeActivated", TribeActivatedEffects.class);
        effectClasses.put("targetOwnedActivated", TargetOwnedActivatedEffects.class);
        effectClasses.put("targetComponentActivated", TargetComponentActivatedEffects.class);
        effectClasses.put("handCardCountActivated", HandCardCountActivatedEffects.class);
        return effectClasses;
    }

    public Map<String, Class<? extends StatModifier>> getStatModifiers() {
        Map<String, Class<? extends StatModifier>> modifierClasses = new HashMap<>();
        modifierClasses.put("addOwnSpiritCount", AddOwnSpiritCountModifier.class);
        modifierClasses.put("addOwnManaPool", AddOwnManaPoolModifier.class);
        modifierClasses.put("addOwnHandCardCount", AddOwnHandCardCountModifier.class);
        modifierClasses.put("addOnOpponentThreshold", AddOnOpponentThresholdModifier.class);
        modifierClasses.put("addOnOwnTurn", AddOnOwnTurnModifier.class);
        modifierClasses.put("addFractional", AddFractionalModifier.class);
        modifierClasses.put("ownHandCardCountActivated", OwnHandCardCountActivatedModifier.class);
        return modifierClasses;
    }
}
