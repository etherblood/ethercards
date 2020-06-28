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

    public PaintableImage createPaintable() {
        return new PaintableImage(width, height);
    }

    public PaintableImage drawCard(CardModel cardModel) {
        return drawCard(graphics -> cardPainterAWT.drawCard(graphics, cardModel, width, height));
    }

    public void drawMinion_Full(MinionModel cardModel, PaintableImage back, PaintableImage art, PaintableImage front) {
        BufferedImage backImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D backGraphics = (Graphics2D) backImage.getGraphics();
        BufferedImage artImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D artGraphics = (Graphics2D) artImage.getGraphics();
        BufferedImage frontImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D frontGraphics = (Graphics2D) frontImage.getGraphics();
        cardPainterAWT.drawMinion_Full(backGraphics, artGraphics, frontGraphics, cardModel, width, height);
        backGraphics.dispose();
        artGraphics.dispose();
        frontGraphics.dispose();
        back.loadImage(backImage, false);
        art.loadImage(artImage, false);
        front.loadImage(frontImage, false);
    }

    public void drawMinion_Minified(MinionModel cardModel, PaintableImage back, PaintableImage art, PaintableImage front) {
        BufferedImage backImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D backGraphics = (Graphics2D) backImage.getGraphics();
        BufferedImage artImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D artGraphics = (Graphics2D) artImage.getGraphics();
        BufferedImage frontImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D frontGraphics = (Graphics2D) frontImage.getGraphics();
        cardPainterAWT.drawMinion_Minified(backGraphics, artGraphics, frontGraphics, cardModel, width, height);
        backGraphics.dispose();
        artGraphics.dispose();
        frontGraphics.dispose();
        back.loadImage(backImage, false);
        art.loadImage(artImage, false);
        front.loadImage(frontImage, false);
    }

    private PaintableImage drawCard(Consumer<Graphics2D> painter) {
        PaintableImage paintableImage = new PaintableImage(width, height);
        BufferedImage bufferedImage = new BufferedImage(paintableImage.getWidth(), paintableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        painter.accept(graphics);
        paintableImage.loadImage(bufferedImage, false);
        return paintableImage;
    }
}
