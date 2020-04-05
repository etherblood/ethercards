package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.samples.visualization.CustomAttachmentVisualization;
import com.destrostudios.cardgui.samples.visualization.GlowBox;
import com.destrostudios.cardgui.samples.visualization.ModelledCard;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class CardVisualization extends CustomAttachmentVisualization<Node> {

    public CardVisualization(AssetManager assetManager) {
        node = new Node();
        modelledCard = new ModelledCard(assetManager, "models/card/card.j3o", "images/cardbacks/magic.png", ColorRGBA.Black);
        node.attachChild(modelledCard.getNode());
        glowBox = new GlowBox(assetManager, 0.96f, 1.28f);
    }
    private Node node;
    private ModelledCard modelledCard;
    private GlowBox glowBox;

    public void setCardFront(PaintableImage paintableImage) {
        modelledCard.setFront(paintableImage);
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
}
