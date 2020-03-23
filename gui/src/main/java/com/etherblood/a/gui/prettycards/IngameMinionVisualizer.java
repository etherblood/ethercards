package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualisation.CardBoxVisualizer;
import com.destrostudios.cardgui.samples.visualisation.PaintableImage;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.HashMap;

public class IngameMinionVisualizer extends CardBoxVisualizer<MinionModel> {

    private static final String NAME_GLOW_BOX = "glowBox";

    private final CardPainterJME cardPainter;
    private final HashMap<Node, Geometry> cachedGlowBoxes = new HashMap<>();

    public IngameMinionVisualizer(CardPainterJME cardPainter) {
        this.cardPainter = cardPainter;
    }

    @Override
    public void createVisualisation(Node node, AssetManager assetManager) {
        super.createVisualisation(node, assetManager);
        Geometry glowBox = createGlowBox(assetManager);
        cachedGlowBoxes.put(node, glowBox);
    }

    private Geometry createGlowBox(AssetManager assetManager) {
        // TODO: Have these as setting/constants somewhere in the cardgui
        float cardWidth = 0.8f;
        float cardHeight = 1.2f;
        float glowExtension = 0.08f;
        Geometry geometry = new Geometry(NAME_GLOW_BOX, new Quad((cardWidth + (2 * glowExtension)), (cardHeight + (2 * glowExtension))));
        Material material = new Material(assetManager, "materials/glow_box/glow_box.j3md");
        material.setTexture("GlowMap", assetManager.loadTexture("textures/effects/card_glow.png"));
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geometry.setMaterial(material);
        geometry.setQueueBucket(RenderQueue.Bucket.Translucent);
        geometry.setLocalTranslation(-1 * ((cardWidth / 2) + glowExtension), 0, (cardHeight / 2) + glowExtension);
        geometry.rotate(new Quaternion().fromAngleAxis(-1 * FastMath.HALF_PI, Vector3f.UNIT_X));
        geometry.addControl(new PulsatingMaterialParamControl("Alpha", 0.4f, 1, 2.5f));
        return geometry;
    }

    @Override
    public void updateVisualisation(Node node, Card<MinionModel> card, AssetManager assetManager) {
        super.updateVisualisation(node, card, assetManager);
        Geometry glowBox = cachedGlowBoxes.get(node);
        if (card.getModel().getGlow() != null) {
            // Here we can have different colors for different effects
            glowBox.getMaterial().setColor("Color", card.getModel().getGlow());
            node.attachChild(glowBox);
        } else {
            node.detachChild(glowBox);
        }
    }

    @Override
    public PaintableImage paintCard(MinionModel cardModel) {
        return cardPainter.drawMinion_Minified(cardModel);
    }
}
