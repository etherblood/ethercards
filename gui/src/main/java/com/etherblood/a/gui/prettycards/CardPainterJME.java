package com.etherblood.a.gui.prettycards;

import com.destrostudios.cardgui.samples.visualization.PaintableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class CardPainterJME {
    
    private final int width = 400;
    private final int height = 560;
    private final CardPainterAWT cardPainterAWT;

    public CardPainterJME(CardPainterAWT cardPainterAWT) {
        this.cardPainterAWT = cardPainterAWT;
    }

    public PaintableImage drawCard(CardModel cardModel){
        return drawCard(graphics -> cardPainterAWT.drawCard(graphics, cardModel, width, height));
    }

    public PaintableImage drawMinion_Full(MinionModel cardModel){
        return drawCard(graphics -> cardPainterAWT.drawMinion_Full(graphics, cardModel, width, height));
    }

    public PaintableImage drawMinion_Minified(MinionModel cardModel){
        return drawCard(graphics -> cardPainterAWT.drawMinion_Minified(graphics, cardModel, width, height));
    }

    private PaintableImage drawCard(Consumer<Graphics2D> painter){
        PaintableImage paintableImage = new PaintableImage(width, height);
        BufferedImage bufferedImage = new BufferedImage(paintableImage.getWidth(), paintableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        painter.accept(graphics);
        paintableImage.loadImage(bufferedImage, false);
        return paintableImage;
    }
}
