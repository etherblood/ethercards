package com.etherblood.a.gui.matchmaking;

import com.etherblood.a.gui.HudTextAppstate;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Slider;

public class SelectOpponentAppstate extends AbstractAppState {

    private int teamSize = 1;
    private final int strength = 10_000;

    private final Node guiNode;
    private final Container container;
    private final Button toggleTeamSizeButton;
    private final Button confirmButton;
    private final Slider[] humanCountSliders;

    private boolean finalized = false;

    public SelectOpponentAppstate(Node guiNode, AssetManager assetManager) {
        this.guiNode = guiNode;

        container = new Container();
        toggleTeamSizeButton = new Button("");
        toggleTeamSizeButton.addClickCommands(x -> toggleTeamSize());
        container.addChild(toggleTeamSizeButton);
        container.setLocalTranslation(500, 600, 0);
        container.addChild(new Label("Humans per team:"));
        int teamCount = 2;
        humanCountSliders = new Slider[teamCount];
        for (int i = 0; i < humanCountSliders.length; i++) {
            int humans = i == 0 ? 1 : 0;
            Slider slider = new Slider(new DefaultRangedValueModel(0, teamSize, humans) {

                @Override
                public void setValue(double value) {
                    super.setValue(Math.round(value));
                    updateGuiElements();
                }

            });
            container.addChild(slider);
            humanCountSliders[i] = slider;
        }
        confirmButton = new Button("confirm");
        confirmButton.addClickCommands(x -> finalized = true);
        container.addChild(confirmButton);
    }

    private void toggleTeamSize() {
        teamSize ^= 3;
        updateGuiElements();
    }

    private void updateGuiElements() {
        int teamCount = humanCountSliders.length;
        StringBuilder builder = new StringBuilder();
        builder.append(teamSize);
        for (int i = 1; i < teamCount; i++) {
            builder.append(" vs ");
            builder.append(teamSize);
        }
        toggleTeamSizeButton.setText(builder.toString());
        for (Slider slider : humanCountSliders) {
            slider.getModel().setMaximum(teamSize);
            slider.getThumbButton().setText(Long.toString(Math.round(slider.getModel().getValue())));
        }
    }

    public MatchOpponents getMatchOpponents() {
        if (finalized) {
            int[] teamHumanCounts = new int[humanCountSliders.length];
            for (int i = 0; i < teamHumanCounts.length; i++) {
                teamHumanCounts[i] = Math.toIntExact(Math.round(humanCountSliders[i].getModel().getValue()));
            }
            return new MatchOpponents(teamHumanCounts, teamSize, strength);
        }
        return null;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        finalized = false;
        stateManager.getState(HudTextAppstate.class).setText("Select your opponent.");
        guiNode.attachChild(container);
        updateGuiElements();
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        guiNode.detachChild(container);
        stateManager.getState(HudTextAppstate.class).setText("");
    }

}
