package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.jme3.asset.AssetManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyCardVisualizer extends CardVisualizer<CardModel> {

    // this cache assumes card images are static which works atm...
    // TODO: replace this with better optimizations which don't assume staticness
    private static final Map<String, PaintableImage> FULL_CARD_CACHE = new ConcurrentHashMap<>();
    private final CardPainterJME cardPainter;

    public MyCardVisualizer(CardPainterJME cardPainter) {
        this.cardPainter = cardPainter;
    }

    @Override
    protected void updateVisualizationObject(CardVisualization visualization, Card<CardModel> card, AssetManager assetManager) {
        if (!card.getModel().isFoil() && card.getModel().getZone() != BoardZone.BATTLE && FULL_CARD_CACHE.containsKey(card.getModel().getTemplate().getAlias())) {
            PaintableImage image = FULL_CARD_CACHE.get(card.getModel().getTemplate().getAlias());
            visualization.setCardFront(image);
            image.flipY();
        } else {
            PaintableImage back = cardPainter.createPaintable();
            PaintableImage art = cardPainter.createPaintable();
            PaintableImage front = cardPainter.createPaintable();
            if (card.getModel().getZone() == BoardZone.BATTLE) {
                if (card.getModel().isInspected()) {
                    cardPainter.drawMinion_Full(card.getModel(), back, art, front);
                } else {
                    cardPainter.drawMinion_Minified(card.getModel(), back, art, front);
                }
            } else {
                cardPainter.drawCard(card.getModel(), back, art, front);
            }
            if (card.getModel().isFoil()) {
                visualization.setFront(back, art, front);
            } else {
                back.paintSameSizeImage(art);
                back.paintSameSizeImage(front);
                visualization.setCardFront(back);
                if (card.getModel().getZone() != BoardZone.BATTLE) {
                    FULL_CARD_CACHE.put(card.getModel().getTemplate().getAlias(), back);
                    back.flipY();
                }
            }
        }

        if (card.getModel().getGlow() != null) {
            visualization.setGlow(card.getModel().getGlow());
        } else {
            visualization.removeGlow();
        }
    }
}
