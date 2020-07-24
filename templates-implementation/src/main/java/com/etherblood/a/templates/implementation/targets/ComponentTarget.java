package com.etherblood.a.templates.implementation.targets;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.targeting.TargetingType;
import com.etherblood.a.rules.templates.TargetSelection;
import com.etherblood.a.templates.api.deserializers.filedtypes.ComponentId;
import java.util.PrimitiveIterator;
import java.util.function.IntUnaryOperator;
import com.etherblood.a.templates.api.TargetPredicate;

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
    public IntList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, IntList validTargets) {
        if (validTargets.isEmpty()) {
            assert !requiresTarget;
            return validTargets;
        }
        switch (select) {
            case ALL:
                return validTargets;
            case ANY:
                return new IntList(new int[]{validTargets.getRandomItem(random)});
            default:
                throw new AssertionError(select);
        }
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        IntList result = data.listInValueOrder(component);
        PrimitiveIterator.OfInt iterator = result.iterator();
        while (iterator.hasNext()) {
            int target = iterator.nextInt();
            if (!predicate.test(data, templates, source, target)) {
                iterator.remove();
            }
        }
        return result;
    }

}
