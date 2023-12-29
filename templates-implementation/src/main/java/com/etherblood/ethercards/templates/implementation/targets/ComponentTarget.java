package com.etherblood.ethercards.templates.implementation.targets;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.targeting.TargetingType;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.ComponentId;

import java.util.function.IntUnaryOperator;

public class ComponentTarget implements TargetSelection {

    @ComponentId
    private final int component;
    private final boolean requiresTarget;
    private final TargetPredicate predicate;
    private final TargetingType select;

    public ComponentTarget(int component, boolean requiresTarget, TargetPredicate predicate, TargetingType select) {
        this.component = component;
        this.requiresTarget = requiresTarget;
        this.predicate = predicate;
        this.select = select;
    }

    @Override
    public EntityList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, EntityList validTargets) {
        if (validTargets.isEmpty()) {
            assert !requiresTarget;
            return validTargets;
        }
        return switch (select) {
            case ALL -> validTargets;
            case ANY -> {
                int index = random.applyAsInt(validTargets.size());
                yield new EntityList(validTargets.get(index));
            }
            default -> throw new AssertionError(select);
        };
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public EntityList getValidTargets(EntityData data, GameTemplates templates, int source) {
        EntityList candidates = data.listInValueOrder(component);
        IntList validTargets = new IntList();
        for (int target : candidates) {
            if (predicate.test(data, templates, source, target)) {
                validTargets.add(target);
            }
        }
        return new EntityList(validTargets);
    }

}
