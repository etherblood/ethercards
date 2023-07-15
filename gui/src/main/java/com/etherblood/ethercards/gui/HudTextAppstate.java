package com.etherblood.ethercards.gui;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

public class HudTextAppstate extends AbstractAppState {

    private final Node guiNode;
    private final BitmapText hudText;
    private final AppSettings appSettings;

    public HudTextAppstate(Node guiNode, BitmapFont guiFont, AppSettings appSettings) {
        this.guiNode = guiNode;
        this.appSettings = appSettings;
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
    }

    public void setText(String text) {
        hudText.setText(text);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        guiNode.detachChild(hudText);
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        guiNode.attachChild(hudText);
    }

    @Override
    public void update(float tpf) {
        hudText.setLocalTranslation(0, appSettings.getHeight(), 0);
        super.update(tpf);
    }
}
