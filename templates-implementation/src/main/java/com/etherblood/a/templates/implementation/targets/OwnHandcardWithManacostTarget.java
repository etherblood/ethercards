package com.etherblood.a.templates.implementation.targets;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.targeting.TargetingType;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.TargetSelection;
import com.etherblood.a.templates.implementation.RelationType;
import java.util.PrimitiveIterator;
import java.util.function.IntUnaryOperator;

public class OwnHandcardWithManacostTarget implements TargetSelection {

    private final TargetingType select;
    private final int value;
    private final RelationType relation;
    private final boolean requiresTarget;

    public OwnHandcardWithManacostTarget(TargetingType select, int value, RelationType relation, boolean requiresTarget) {
        this.select = select;
        this.value = value;
        this.relation = relation;
        this.requiresTarget = requiresTarget;
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public IntList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, IntList validTargets) {
        switch (select) {
            case ALL:
                return validTargets;
            case ANY:
                return new IntList(new int[]{validTargets.getRandomItem(random)});
            case USER:
                // user selection should be done INSTEAD of calling this method
                throw new AssertionError(select);
            default:
                throw new AssertionError(select);
        }
    }

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList targets = data.list(core.IN_HAND_ZONE);
        int owner = data.get(source, core.OWNER);
        PrimitiveIterator.OfInt iterator = targets.iterator();
        while (iterator.hasNext()) {
            int handCard = iterator.nextInt();
            if (handCard != source && data.hasValue(handCard, core.OWNER, owner)) {
                int templateId = data.get(handCard, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);
                if (template.getManaCost() != null) {
                    if (relation.applyTo(template.getManaCost(), value)) {
                        continue;
                    }
                }
            }
            iterator.remove();
        }
        return targets;
    }

}
