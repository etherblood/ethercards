package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.jme3.asset.AssetManager;

public class CardVisualizer_Card extends CardVisualizer<CardModel> {

    private final CardPainterJME cardPainter;

    public CardVisualizer_Card(CardPainterJME cardPainter) {
        this.cardPainter = cardPainter;
    }

    @Override
    protected void updateVisualizationObject(CardVisualization visualization, Card<CardModel> card, AssetManager assetManager) {
        PaintableImage frontImage = cardPainter.drawCard(card.getModel());
        visualization.setCardFront(frontImage);

        if (card.getModel().getGlow() != null) {
            visualization.setGlow(card.getModel().getGlow());
        } else {
            visualization.removeGlow();
        }
    }
}
