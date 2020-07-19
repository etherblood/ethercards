package com.etherblood.a.gui.matchmaking;

import com.etherblood.a.gui.HudTextAppstate;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;

public class SelectOpponentAppstate extends AbstractAppState {

    private final int[] teamHumanCounts = {1, 0};
    private int teamSize = 1;

    private final Node guiNode;
    private final Container container;
    private final Button toggleTeamSizeButton;
    private final Button confirmButton;

    private boolean finalized = false;

    public SelectOpponentAppstate(Node guiNode, AssetManager assetManager) {
        this.guiNode = guiNode;

        container = new Container();
        toggleTeamSizeButton = new Button("");
        toggleTeamSizeButton.addClickCommands(x -> toggleTeamSize());
        toggleTeamSizeButton.setLocalTranslation(100, 200, 0);
        container.addChild(toggleTeamSizeButton);
        confirmButton = new Button("confirm");
        confirmButton.addClickCommands(x -> finalized = true);
        confirmButton.setLocalTranslation(100, 300, 0);
        container.addChild(confirmButton);
        container.setLocalTranslation(0, 900, 0);
    }

    private void toggleTeamSize() {
        teamSize ^= 3;
        for (int i = 0; i < teamHumanCounts.length; i++) {
            if (teamHumanCounts[i] > teamSize) {
                teamHumanCounts[i] = teamSize;
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        int teamCount = teamHumanCounts.length;
        StringBuilder builder = new StringBuilder();
        builder.append(teamSize);
        for (int i = 1; i < teamCount; i++) {
            builder.append(" vs ");
            builder.append(teamSize);
        }
        toggleTeamSizeButton.setText(builder.toString());
    }

    public MatchOpponents getMatchOpponents() {
        if (finalized) {
            return new MatchOpponents(teamHumanCounts, teamSize, 10_000);
        }
        return null;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        finalized = false;
        stateManager.getState(HudTextAppstate.class).setText("Select your opponent.");
        guiNode.attachChild(container);
        updateButtons();
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        guiNode.detachChild(container);
        stateManager.getState(HudTextAppstate.class).setText("");
    }

}
