package com.etherblood.ethercards.gui.prettycards;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.CustomAttachmentVisualizer;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

public abstract class CardVisualizer<CardModelType extends BoardObjectModel> extends CustomAttachmentVisualizer<Card<CardModelType>, Node, CardVisualization> {

    @Override
    protected CardVisualization createVisualizationObject(AssetManager assetManager) {
        return new CardVisualization(assetManager);
    }
}
