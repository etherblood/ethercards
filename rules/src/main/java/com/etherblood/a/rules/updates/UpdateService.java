package com.etherblood.a.rules.updates;

import java.util.List;

public class UpdateService {

    private final EffectiveStatsService stats;
    private final List<ActionSystem> actionSystems;
    private final List<ActionSystem> phaseSystems;
    private final ActionSystem survivalSystem;

    public UpdateService(List<ActionSystem> phaseSystems, List<ActionSystem> actionSystems, ActionSystem survivalSystem, EffectiveStatsService stats) {
        this.stats = stats;
        this.actionSystems = actionSystems;
        this.phaseSystems = phaseSystems;
        this.survivalSystem = survivalSystem;
    }

    public void run() {
        do {
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.before();
            }
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.run();
            }
            for (ActionSystem phaseSystem : phaseSystems) {
                phaseSystem.after();
            }
            while (actionSystems.stream().anyMatch(ActionSystem::isActive)) {
                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.before();
                }
                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.run();
                }
                for (ActionSystem actionSystem : actionSystems) {
                    actionSystem.after();
                }

                stats.killHealthless();
                stats.unbindFreedMinions();
                if (actionSystems.stream().noneMatch(ActionSystem::isActive)) {
                    survivalSystem.before();
                    survivalSystem.run();
                    survivalSystem.after();
                }
            }
        } while (phaseSystems.stream().anyMatch(ActionSystem::isActive));
    }
}
