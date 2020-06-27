package com.etherblood.a.rules.updates;

import java.util.List;

public class UpdateService {

    private final StatsService stats;
    private final List<ActionSystem> actionSystems;
    private final List<ActionSystem> phaseSystems;

    public UpdateService(List<ActionSystem> phaseSystems, List<ActionSystem> actionSystems, StatsService stats) {
        this.stats = stats;
        this.actionSystems = actionSystems;
        this.phaseSystems = phaseSystems;
    }

    public void run() {
        do {
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.modify();
            }
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.apply();
            }
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.triggerAndClean();
            }
            while (actionSystems.stream().anyMatch(ActionSystem::isActive)) {
                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.modify();
                }
                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.apply();
                }

                stats.killHealthless();

                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.triggerAndClean();
                }
            }
        } while (phaseSystems.stream().anyMatch(ActionSystem::isActive));
    }
}
