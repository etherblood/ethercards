package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.jme3.asset.AssetManager;

public class CardVisualizer_Minion extends CardVisualizer<MinionModel> {

    private final CardPainterJME cardPainter;

    public CardVisualizer_Minion(CardPainterJME cardPainter) {
        this.cardPainter = cardPainter;
    }

    @Override
    protected void updateVisualizationObject(CardVisualization visualization, Card<MinionModel> card, AssetManager assetManager) {
        PaintableImage frontImage;
        if (card.getModel().isInspected()) {
            frontImage = cardPainter.drawMinion_Full(card.getModel());
        } else {
            frontImage = cardPainter.drawMinion_Minified(card.getModel());
        }
        visualization.setCardFront(frontImage);

        if (card.getModel().getGlow() != null) {
            visualization.setGlow(card.getModel().getGlow());
        } else {
            visualization.removeGlow();
        }
    }
}
