package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.jme3.asset.AssetManager;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MyCardVisualizer extends CardVisualizer<CardModel> {

    // this cache assumes card images are static which works atm...
    // TODO: replace this with better optimizations which don't assume staticness
    private static final Map<String, PaintableImage> FULL_CARD_CACHE = new ConcurrentHashMap<>();
    private static final String FACE_DOWN_CARD_ID = UUID.randomUUID().toString();
    private final CardPainterJME cardPainter;
    private final boolean battleFullArt;

    public MyCardVisualizer(CardPainterJME cardPainter, boolean battleFullArt) {
        this.cardPainter = cardPainter;
        this.battleFullArt = battleFullArt;
    }

    @Override
    protected void updateVisualizationObject(CardVisualization visualization, Card<CardModel> card, AssetManager assetManager) {
        if (!card.getModel().isFoil() && card.getModel().getZone() != BoardZone.BATTLE && FULL_CARD_CACHE.containsKey(card.getModel().getTemplate().getAlias())) {
            PaintableImage image = FULL_CARD_CACHE.get(card.getModel().getTemplate().getAlias());
            visualization.setCardFront(image);
            image.flipY();
        } else {
            if (!card.getModel().isFaceUp()) {
                //don't bother rendering the card when it is face down
                PaintableImage image = FULL_CARD_CACHE.computeIfAbsent(FACE_DOWN_CARD_ID, x -> cardPainter.createPaintable(Color.BLACK));
                visualization.setCardFront(image);
                return;
            }

            PaintableImage back = cardPainter.createPaintable();
            PaintableImage art = cardPainter.createPaintable();
            PaintableImage front = cardPainter.createPaintable();
            if (card.getModel().getZone() == BoardZone.BATTLE) {
                if (card.getModel().isInspected()) {
                    cardPainter.drawMinion_Full(card.getModel(), back, art, front);
                } else {
                    cardPainter.drawMinion_Minified(card.getModel(), back, art, front, battleFullArt);
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
