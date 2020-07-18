package com.etherblood.a.rules.updates;

import com.etherblood.a.rules.updates.systems.StateDrivenUpdatesService;
import java.util.List;

public class UpdateService {

    private final List<ActionSystem> actionSystems;
    private final List<ActionSystem> phaseSystems;
    private final ActionSystem survivalSystem;
    private final StateDrivenUpdatesService stateDrivenService;

    public UpdateService(List<ActionSystem> phaseSystems, List<ActionSystem> actionSystems, ActionSystem survivalSystem, StateDrivenUpdatesService stateDrivenService) {
        this.actionSystems = actionSystems;
        this.phaseSystems = phaseSystems;
        this.survivalSystem = survivalSystem;
        this.stateDrivenService = stateDrivenService;
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

                fixInconsistencies();
                if (actionSystems.stream().noneMatch(ActionSystem::isActive)) {
                    survivalSystem.before();
                    survivalSystem.run();
                    survivalSystem.after();
                    fixInconsistencies();
                }
            }
        } while (phaseSystems.stream().anyMatch(ActionSystem::isActive));
    }

    private void fixInconsistencies() {
        stateDrivenService.killHealthless();
        stateDrivenService.unbindFreedMinions();
        stateDrivenService.removeInvalidAttacks();
        stateDrivenService.removeInvalidBlocks();
        stateDrivenService.attackWithRagers();
    }
}
