package com.etherblood.a.rules.targeting;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import java.util.Arrays;
import java.util.List;

public class TargetUtil {

    public static IntList findValidTargets(EntityData data, int source, TargetFilters[] targets) {
        List<TargetFilters> targetTypes = Arrays.asList(targets);
        assert !targetTypes.contains(null);
        IntList availableTargets = new IntList();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int sourceOwner = data.get(source, core.OWNER);
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (data.has(minion, core.DEATH_REQUEST)) {
                continue;
            }
            int owner = data.get(minion, core.OWNER);
            if (sourceOwner == owner) {
                if (data.has(minion, core.HERO)) {
                    if (targetTypes.contains(TargetFilters.OWN_HERO)) {
                        availableTargets.add(minion);
                    }
                } else {
                    if (targetTypes.contains(TargetFilters.OWN_MINION)) {
                        availableTargets.add(minion);
                    }
                }
            } else {
                if (data.has(minion, core.HERO)) {
                    if (targetTypes.contains(TargetFilters.OPPONENT_HERO)) {
                        availableTargets.add(minion);
                    }
                } else {
                    if (targetTypes.contains(TargetFilters.OPPONENT_MINION)) {
                        availableTargets.add(minion);
                    }
                }
            }
        }
        if (availableTargets.contains(source)) {
            availableTargets.swapRemove(source);
        }
        if (targetTypes.contains(TargetFilters.SOURCE)) {
            availableTargets.add(source);
        }
        if (targetTypes.contains(TargetFilters.OWNER)) {
            availableTargets.add(sourceOwner);
        }
        if (targetTypes.contains(TargetFilters.OPPONENT)) {
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (player != sourceOwner) {
                    availableTargets.add(player);
                }
            }
        }
        return availableTargets;
    }
}
