package com.etherblood.a.gui;

import com.destrostudios.cardgui.samples.tools.deckbuilder.SimpleDeckBuilderDeckCardVisualizer;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class MyDeckBuilderDeckCardVisualizer extends SimpleDeckBuilderDeckCardVisualizer<CardModel> {

    private static final Font FONT = new Font("Tahoma", Font.BOLD, 22);
    private final CardImages images;

    public MyDeckBuilderDeckCardVisualizer(CardImages images) {
        super(4, 0.57f, 57);
        this.images = images;
    }

    @Override
    protected PaintableImage paintActualCard(CardModel cardModel) {
        PaintableImage image = new PaintableImage(400, 57);
        image.setBackground(Color.BLACK);
        int frames = cardModel.getTemplate().getFrames();
        String imagePath = images.getCardImageFilePath(cardModel);
        BufferedImage img = images.readImage(imagePath);
        PaintableImage artwork = new PaintableImage(img);
        image.paintImage(artwork, 0, 0, (artwork.getWidth() / frames), artwork.getHeight(), amountPixelWidth, 0, (image.getWidth() - amountPixelWidth), image.getHeight());
        paintText(image, amountPixelWidth + 20, (image.getHeight() / 2), false, true, cardModel.getTemplate().getName());
        return image;
    }

    @Override
    protected void paintCenteredAmount(PaintableImage image, int x, int y, int amount) {
        paintText(image, x, y, true, true, "" + amount);
    }

    private void paintText(PaintableImage image, int x, int y, boolean centeredX, boolean centeredY, String text) {
        // Overkill implementation via an own AWT image since it's just a demo
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(FONT);
        graphics.setColor(Color.WHITE);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int resultingX = x;
        if (centeredX) {
            resultingX -= (fontMetrics.stringWidth(text) / 2);
        }
        int resultingY = y;
        if (centeredY) {
            resultingY -= (fontMetrics.getHeight() / 2);
            resultingY += fontMetrics.getAscent();
        }
        graphics.drawString(text, resultingX, resultingY);
        image.paintImage(bufferedImage, 0, 0);
    }
}
