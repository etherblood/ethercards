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
        PaintableImage back = cardPainter.createPaintable();
        PaintableImage art = cardPainter.createPaintable();
        PaintableImage front = cardPainter.createPaintable();
        if (card.getModel().isInspected()) {
            cardPainter.drawMinion_Full(card.getModel(), back, art, front);
        } else {
            cardPainter.drawMinion_Minified(card.getModel(), back, art, front);
        }
        if (card.getModel().isHero()) {
            visualization.setFront(back, art, front);
        } else {
            back.paintImage(art, 0, 0, back.getWidth(), back.getHeight());
            back.paintImage(front, 0, 0, back.getWidth(), back.getHeight());
            visualization.setCardFront(back);
        }

        if (card.getModel().getGlow() != null) {
            visualization.setGlow(card.getModel().getGlow());
        } else {
            visualization.removeGlow();
        }
    }
}
