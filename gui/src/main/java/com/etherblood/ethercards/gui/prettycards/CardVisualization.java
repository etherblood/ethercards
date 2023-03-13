package com.etherblood.ethercards.gui.prettycards;

import com.destrostudios.cardgui.samples.visualization.CustomAttachmentVisualization;
import com.destrostudios.cardgui.samples.visualization.background.GlowQuad;
import com.destrostudios.cardgui.samples.visualization.cards.modelled.FoilModelledCard;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class CardVisualization extends CustomAttachmentVisualization<Node> {

    public static final String CARDBACK_IMAGE_PATH = "images/cardbacks/magic.png";
    private final Node node;
    private final FoilModelledCard modelledCard;
    private final GlowQuad glowBox;

    public CardVisualization(AssetManager assetManager) {
        node = new Node();
        modelledCard = new FoilModelledCard(assetManager,  CARDBACK_IMAGE_PATH, ColorRGBA.Black);
        node.attachChild(modelledCard.getNode());
        glowBox = new GlowQuad(assetManager, 0.96f, 1.28f);
    }

    public void setGlow(ColorRGBA colorRGBA) {
        glowBox.setColor(colorRGBA);
        node.attachChild(glowBox.getGeometry());
    }

    public void removeGlow() {
        node.detachChild(glowBox.getGeometry());
    }

    @Override
    public Node getSpatial() {
        return node;
    }

    public Material getMaterial_Front() {
        return modelledCard.getMaterial_Front();
    }
}
