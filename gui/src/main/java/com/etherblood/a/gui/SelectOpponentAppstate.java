package com.etherblood.a.gui;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class SelectOpponentAppstate extends AbstractAppState {

    private final Node rootNode;
    private final Geometry humanButton;
    private final Geometry lv1Button;
    private final Geometry lv3Button;
    private final Geometry lv6Button;
    private final Geometry lv10Button;
    private Integer selectedStrength;

    public SelectOpponentAppstate(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        humanButton = createButton(assetManager, "human", 0);
        lv1Button = createButton(assetManager, "level_1", 1);
        lv3Button = createButton(assetManager, "level_3", 2);
        lv6Button = createButton(assetManager, "level_6", 3);
        lv10Button = createButton(assetManager, "level_10", 4);
    }

    public Integer getSelectedStrength() {
        return selectedStrength;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        selectedStrength = null;
        ButtonAppstate buttonAppstate = stateManager.getState(ButtonAppstate.class);
        ColorRGBA[] colors = {ColorRGBA.Gray, ColorRGBA.LightGray, ColorRGBA.White};
        buttonAppstate.registerButton(humanButton, createHandler(0), colors);
        buttonAppstate.registerButton(lv1Button, createHandler(1), colors);
        buttonAppstate.registerButton(lv3Button, createHandler(3), colors);
        buttonAppstate.registerButton(lv6Button, createHandler(6), colors);
        buttonAppstate.registerButton(lv10Button, createHandler(10), colors);
        stateManager.getState(HudTextAppstate.class).setText("Select your opponent.");
        rootNode.attachChild(humanButton);
        rootNode.attachChild(lv1Button);
        rootNode.attachChild(lv3Button);
        rootNode.attachChild(lv6Button);
        rootNode.attachChild(lv10Button);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        ButtonAppstate buttonAppstate = stateManager.getState(ButtonAppstate.class);
        buttonAppstate.unregisterButton(humanButton);
        buttonAppstate.unregisterButton(lv1Button);
        buttonAppstate.unregisterButton(lv3Button);
        buttonAppstate.unregisterButton(lv6Button);
        buttonAppstate.unregisterButton(lv10Button);
        stateManager.getState(HudTextAppstate.class).setText("");
        rootNode.detachChild(humanButton);
        rootNode.detachChild(lv1Button);
        rootNode.detachChild(lv3Button);
        rootNode.detachChild(lv6Button);
        rootNode.detachChild(lv10Button);
    }

    private Runnable createHandler(int strength) {
        return () -> this.selectedStrength = strength;
    }

    private Geometry createButton(AssetManager assetManager, String texture, int index) {
        Quad quad = new Quad(1, 1);
        Geometry button = new Geometry(texture, quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("textures/buttons/" + texture + ".png"));
        button.setMaterial(mat);

        button.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
        button.setLocalTranslation(index * 2.5f - 7, 2, 2);
        button.setLocalScale(2, 1, 1);
        return button;
    }

}
