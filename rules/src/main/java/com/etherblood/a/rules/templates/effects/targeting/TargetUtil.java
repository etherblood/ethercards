package com.etherblood.a.rules.templates.effects.targeting;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import java.util.Arrays;
import java.util.List;

public class TargetUtil {

    public static boolean isValidTarget(EntityData data, int source, int target, TargetFilters[] targets) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        List<TargetFilters> targetTypes = Arrays.asList(targets);
        if (target == source) {
            return targetTypes.contains(TargetFilters.SOURCE);
        }
        int sourceOwner = data.get(source, core.OWNED_BY);
        if (sourceOwner == target) {
            return targetTypes.contains(TargetFilters.OWNER);
        }
        if (data.has(target, core.IN_BATTLE_ZONE) && !data.has(target, core.DEATH_REQUEST)) {
            int targetOwner = data.get(target, core.OWNED_BY);
            if (data.has(target, core.HERO)) {
                if (sourceOwner == targetOwner) {
                    return targetTypes.contains(TargetFilters.OWN_HERO);
                }
                return targetTypes.contains(TargetFilters.OPPONENT_HERO);
            }
            if (sourceOwner == targetOwner) {
                return targetTypes.contains(TargetFilters.OWN_MINION);
            }
            return targetTypes.contains(TargetFilters.OPPONENT_MINION);
        }
        if (data.has(target, core.PLAYER_INDEX)) {
            return targetTypes.contains(TargetFilters.OPPONENT);
        }
        return false;
    }

    public static IntList findValidTargets(EntityData data, int source, TargetFilters[] targets) {
        List<TargetFilters> targetTypes = Arrays.asList(targets);
        assert !targetTypes.contains(null);
        IntList availableTargets = new IntList();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int sourceOwner = data.get(source, core.OWNED_BY);
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.has(minion, core.DEATH_REQUEST)) {
                continue;
            }
            int owner = data.get(minion, core.OWNED_BY);
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
        for (int target : availableTargets) {
            assert isValidTarget(data, source, target, targets);
        }
        return availableTargets;
    }
}
