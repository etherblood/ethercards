package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.ZoneState;
import java.util.List;
import java.util.function.IntUnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(TriggerService.class);

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;

    public TriggerService(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
    }

    public void onSummoned(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_SUMMON, minion);
        triggerOthers(minion, core.TRIGGER_OTHER_SUMMON);
    }

    public void onCast(int source, int target) {
        triggerSelf(source, core.TRIGGER_SELF_CAST, target);
        triggerOthers(source, core.TRIGGER_OTHER_CAST);
    }

    public void onDeath(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_DEATH, minion);
    }

    public void onEnterBattle(int card) {
        triggerSelf(card, core.TRIGGER_SELF_ENTER_BATTLE, card);
    }

    public void onEnterGraveyard(int card) {
        triggerSelf(card, core.TRIGGER_SELF_ENTER_GRAVEYARD, card);
    }

    public void onSurvive(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_SURVIVE, minion);
    }

    public void onFight(int minion, int target) {
        triggerSelf(minion, core.TRIGGER_SELF_FIGHT, target);
    }

    public void onUpkeep(int owner) {
        triggerOwner(owner, core.TRIGGER_OWNER_UPKEEP);
    }

    public void onDraw(int owner) {
        triggerOwner(owner, core.TRIGGER_OWNER_DRAW);
    }

    private void triggerOwner(int owner, int ownerComponent) {
        for (int other : data.listInValueOrder(ownerComponent)) {
            if (data.hasValue(other, core.OWNER, owner)) {
                trigger(ownerComponent, other, owner);
            }
        }
    }

    private void triggerSelf(int self, int triggerComponent, int target) {
        if (data.has(self, triggerComponent)) {
            trigger(triggerComponent, self, target);
        }
    }

    private void triggerOthers(int triggerTarget, int triggerComponent) {
        IntList listInValueOrder = data.listInValueOrder(triggerComponent);
        for (int other : listInValueOrder) {
            if (other == triggerTarget) {
                continue;
            }

            if (data.has(other, triggerComponent)) {
                trigger(triggerComponent, other, triggerTarget);
            }
        }
    }

    public void trigger(int triggerComponent, int self, int triggerTarget) {
        List<Effect> triggers = zoneState(self).getPassive().get(triggerComponent);
        if (triggers == null) {
            throw new NullPointerException("Missing " + data.getComponents().getMeta(triggerComponent).name);
        }
        for (Effect effect : triggers) {
            effect.apply(data, templates, random, events, self, triggerTarget);
        }
    }

    public void cleanupEffects(int entity) {
        for (int trigger : zoneState(entity).getPassive().keySet()) {
            assert !zoneState(entity).getPassive().get(trigger).isEmpty();
            data.remove(entity, trigger);
        }
        data.remove(entity, core.ACTIVATED_ABILITY);
    }

    public void initEffects(int entity) {
        ZoneState zoneState = zoneState(entity);
        for (int trigger : zoneState.getPassive().keySet()) {
            assert !zoneState.getPassive().get(trigger).isEmpty();
            data.set(entity, trigger, data.createEntity());
        }
        if (zoneState.getActivated() != null) {
            data.set(entity, core.ACTIVATED_ABILITY, 1);
        }
    }

    private ZoneState zoneState(int entity) {
        int templateId = data.get(entity, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        return template.getActiveZone(entity, data);
    }
}
