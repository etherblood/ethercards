package com.etherblood.ethercards.templates.implementation;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.api.TemplateClassAliasMap;
import com.etherblood.ethercards.templates.api.Untargeted;
import com.etherblood.ethercards.templates.implementation.effects.BindControlEffect;
import com.etherblood.ethercards.templates.implementation.effects.BuffEffect;
import com.etherblood.ethercards.templates.implementation.effects.CardDestructionEffect;
import com.etherblood.ethercards.templates.implementation.effects.CreateCardEffect;
import com.etherblood.ethercards.templates.implementation.effects.DamageEffect;
import com.etherblood.ethercards.templates.implementation.effects.DebuffEffect;
import com.etherblood.ethercards.templates.implementation.effects.DiscardEffect;
import com.etherblood.ethercards.templates.implementation.effects.DittoEffect;
import com.etherblood.ethercards.templates.implementation.effects.DrawCardTemplateEffect;
import com.etherblood.ethercards.templates.implementation.effects.DrawEffect;
import com.etherblood.ethercards.templates.implementation.effects.ExileEffect;
import com.etherblood.ethercards.templates.implementation.effects.FractionalDamageEffect;
import com.etherblood.ethercards.templates.implementation.effects.FusionEffect;
import com.etherblood.ethercards.templates.implementation.effects.KolaghanDamageEffect;
import com.etherblood.ethercards.templates.implementation.effects.LathlissTokenEffect;
import com.etherblood.ethercards.templates.implementation.effects.LossEffect;
import com.etherblood.ethercards.templates.implementation.effects.MoveToZoneEffect;
import com.etherblood.ethercards.templates.implementation.effects.MultiTriggerEffects;
import com.etherblood.ethercards.templates.implementation.effects.NinjutsuEffect;
import com.etherblood.ethercards.templates.implementation.effects.ParticleEventEffect;
import com.etherblood.ethercards.templates.implementation.effects.PlayerDiscardEffect;
import com.etherblood.ethercards.templates.implementation.effects.PredicateActivatedEffects;
import com.etherblood.ethercards.templates.implementation.effects.RecallEffect;
import com.etherblood.ethercards.templates.implementation.effects.ResurrectRandomEffect;
import com.etherblood.ethercards.templates.implementation.effects.SelfDiscardEffect;
import com.etherblood.ethercards.templates.implementation.effects.SelfResurrectEffect;
import com.etherblood.ethercards.templates.implementation.effects.SelfSummonEffect;
import com.etherblood.ethercards.templates.implementation.effects.SelfSummonFromLibraryEffect;
import com.etherblood.ethercards.templates.implementation.effects.SoulshiftEffect;
import com.etherblood.ethercards.templates.implementation.effects.SourceOwnerPhaseEffects;
import com.etherblood.ethercards.templates.implementation.effects.SpiritCountSoulshiftEffect;
import com.etherblood.ethercards.templates.implementation.effects.SummonEffect;
import com.etherblood.ethercards.templates.implementation.effects.TakeControlEffect;
import com.etherblood.ethercards.templates.implementation.effects.TargetActivatedEffects;
import com.etherblood.ethercards.templates.implementation.effects.TargetedEffects;
import com.etherblood.ethercards.templates.implementation.effects.TransformTemplateEffect;
import com.etherblood.ethercards.templates.implementation.effects.TribeActivatedEffects;
import com.etherblood.ethercards.templates.implementation.effects.WinEffect;
import com.etherblood.ethercards.templates.implementation.predicates.AllOfPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.AnyOfPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.AttackIsPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.BoolPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.CanNinjutsuPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.FlyingPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HandCardCountIsPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HasSourceOwnerPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HasSourceTeamPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HasTribePredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HealthIsPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.HeroPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.IsAttackingPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.IsBlockedPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.IsSourcePredicate;
import com.etherblood.ethercards.templates.implementation.predicates.ManaCostIsPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.MinionCountIsPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.MinionPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.NoneOfPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.SourceOwnerPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.TestSourcePredicate;
import com.etherblood.ethercards.templates.implementation.predicates.TiredPredicate;
import com.etherblood.ethercards.templates.implementation.predicates.ZonePredicate;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddComponentPredicateCountModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddFractionalModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOnOpponentThresholdModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOnOwnTurnModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOwnHandCardCountModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOwnManaPoolModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOwnSpiritCountModifier;
import com.etherblood.ethercards.templates.implementation.statmodifiers.PredicateActivatedModifier;
import com.etherblood.ethercards.templates.implementation.targets.ComponentTarget;
import com.etherblood.ethercards.templates.implementation.targets.SimpleTarget;
import com.etherblood.ethercards.templates.implementation.targets.SourceTarget;
import java.util.HashMap;
import java.util.Map;

public class TemplateAliasMapsImpl implements TemplateClassAliasMap {

    @Override
    public Map<String, Class<? extends Effect>> getEffects() {
        Map<String, Class<? extends Effect>> effectClasses = new HashMap<>();
        effectClasses.put("summon", SummonEffect.class);
        effectClasses.put("selfSummon", SelfSummonEffect.class);
        effectClasses.put("selfSummonFromLibrary", SelfSummonFromLibraryEffect.class);
        effectClasses.put("selfResurrect", SelfResurrectEffect.class);
        effectClasses.put("selfDiscard", SelfDiscardEffect.class);
        effectClasses.put("lathlissToken", LathlissTokenEffect.class);
        effectClasses.put("fractionalDamage", FractionalDamageEffect.class);
        effectClasses.put("buff", BuffEffect.class);
        effectClasses.put("debuff", DebuffEffect.class);
        effectClasses.put("transformTemplate", TransformTemplateEffect.class);
        effectClasses.put("sourceOwnerPhase", SourceOwnerPhaseEffects.class);
        effectClasses.put("create", CreateCardEffect.class);
        effectClasses.put("targeted", TargetedEffects.class);
        effectClasses.put("particle", ParticleEventEffect.class);
        effectClasses.put("drawTemplate", DrawCardTemplateEffect.class);
        effectClasses.put("takeControl", TakeControlEffect.class);
        effectClasses.put("bindControl", BindControlEffect.class);
        effectClasses.put("soulshift", SoulshiftEffect.class);
        effectClasses.put("spiritCountSoulshift", SpiritCountSoulshiftEffect.class);
        effectClasses.put("cardDestruction", CardDestructionEffect.class);
        effectClasses.put("resurrectRandom", ResurrectRandomEffect.class);
        effectClasses.put("kolaghanDamage", KolaghanDamageEffect.class);
        effectClasses.put("tribeActivated", TribeActivatedEffects.class);
        effectClasses.put("targetActivated", TargetActivatedEffects.class);
        effectClasses.put("predicateActivated", PredicateActivatedEffects.class);
        effectClasses.put("multiTrigger", MultiTriggerEffects.class);
        effectClasses.put("fusion", FusionEffect.class);
        effectClasses.put("ditto", DittoEffect.class);
        effectClasses.put("exile", ExileEffect.class);
        effectClasses.put("moveToZone", MoveToZoneEffect.class);
        effectClasses.put("ninjutsu", NinjutsuEffect.class);
        effectClasses.put("recall", RecallEffect.class);
        effectClasses.put("discard", DiscardEffect.class);
        effectClasses.put("playerDiscard", PlayerDiscardEffect.class);
        effectClasses.put("draw", DrawEffect.class);
        effectClasses.put("win", WinEffect.class);
        effectClasses.put("loss", LossEffect.class);
        effectClasses.put("damage", DamageEffect.class);
        return effectClasses;
    }

    @Override
    public Map<String, Class<? extends StatModifier>> getStatModifiers() {
        Map<String, Class<? extends StatModifier>> modifierClasses = new HashMap<>();
        modifierClasses.put("addOwnSpiritCount", AddOwnSpiritCountModifier.class);
        modifierClasses.put("addOwnManaPool", AddOwnManaPoolModifier.class);
        modifierClasses.put("addOwnHandCardCount", AddOwnHandCardCountModifier.class);
        modifierClasses.put("addOnOpponentThreshold", AddOnOpponentThresholdModifier.class);
        modifierClasses.put("addOnOwnTurn", AddOnOwnTurnModifier.class);
        modifierClasses.put("addFractional", AddFractionalModifier.class);
        modifierClasses.put("predicateActivated", PredicateActivatedModifier.class);
        modifierClasses.put("addComponentPredicateCount", AddComponentPredicateCountModifier.class);
        return modifierClasses;
    }

    @Override
    public Map<String, Class<? extends TargetSelection>> getTargetSelections() {
        Map<String, Class<? extends TargetSelection>> targetClasses = new HashMap<>();
        targetClasses.put("untargeted", Untargeted.class);
        targetClasses.put("simple", SimpleTarget.class);
        targetClasses.put("component", ComponentTarget.class);
        targetClasses.put("source", SourceTarget.class);
        return targetClasses;
    }

    @Override
    public Map<String, Class<? extends TargetPredicate>> getTargetPredicates() {
        Map<String, Class<? extends TargetPredicate>> targetClasses = new HashMap<>();
        targetClasses.put("bool", BoolPredicate.class);
        targetClasses.put("noneOf", NoneOfPredicate.class);
        targetClasses.put("anyOf", AnyOfPredicate.class);
        targetClasses.put("allOf", AllOfPredicate.class);
        targetClasses.put("flying", FlyingPredicate.class);
        targetClasses.put("attackIs", AttackIsPredicate.class);
        targetClasses.put("healthIs", HealthIsPredicate.class);
        targetClasses.put("manaCostIs", ManaCostIsPredicate.class);
        targetClasses.put("handCardCountIs", HandCardCountIsPredicate.class);
        targetClasses.put("minionCountIs", MinionCountIsPredicate.class);
        targetClasses.put("hero", HeroPredicate.class);
        targetClasses.put("minion", MinionPredicate.class);
        targetClasses.put("tired", TiredPredicate.class);
        targetClasses.put("zone", ZonePredicate.class);
        targetClasses.put("isSource", IsSourcePredicate.class);
        targetClasses.put("hasSourceOwner", HasSourceOwnerPredicate.class);
        targetClasses.put("hasSourceTeam", HasSourceTeamPredicate.class);
        targetClasses.put("sourceOwner", SourceOwnerPredicate.class);
        targetClasses.put("hasTribe", HasTribePredicate.class);
        targetClasses.put("canNinjutsu", CanNinjutsuPredicate.class);
        targetClasses.put("testSource", TestSourcePredicate.class);
        targetClasses.put("isAttacking", IsAttackingPredicate.class);
        targetClasses.put("isBlocked", IsBlockedPredicate.class);
        return targetClasses;
    }
}
