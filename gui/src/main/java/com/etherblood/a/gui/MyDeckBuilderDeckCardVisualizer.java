package com.etherblood.a.gui;

import com.destrostudios.cardgui.samples.tools.deckbuilder.SimpleDeckBuilderDeckCardVisualizer;
import com.destrostudios.cardgui.samples.visualization.PaintableImage;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
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
        String imagePath = images.getCardImageFilePath(cardModel);
        Image img = images.getCachedImage(imagePath, (image.getWidth() - amountPixelWidth), image.getHeight());
        image.paintImage(toBufferedImage(img), amountPixelWidth, 0);
        paintText(image, amountPixelWidth + 20, (image.getHeight() / 2), false, true, cardModel.getTemplate().getName());
        return image;
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
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
