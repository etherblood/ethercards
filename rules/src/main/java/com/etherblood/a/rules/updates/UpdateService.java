package com.etherblood.a.rules.updates;

import java.util.List;

public class UpdateService {

    private final EffectiveStatsService stats;
    private final List<ActionSystem> actionSystems;
    private final List<ActionSystem> phaseSystems;

    public UpdateService(List<ActionSystem> phaseSystems, List<ActionSystem> actionSystems, EffectiveStatsService stats) {
        this.stats = stats;
        this.actionSystems = actionSystems;
        this.phaseSystems = phaseSystems;
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
            }
        } while (phaseSystems.stream().anyMatch(ActionSystem::isActive));
    }
}
