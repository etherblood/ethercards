package com.etherblood.a.gui.prettycards;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CardImages {

    private final String assetsPath;

    public CardImages(String assetsPath) {
        this.assetsPath = assetsPath;
    }

    public BufferedImage readAndScaleImage(String filePath, int width, int height) {
        BufferedImage image = readImage(filePath);
        if ((width != -1) && (height != -1)) {
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
            graphics.dispose();
            return resizedImage;
        }
        return image;
    }

    public BufferedImage readImage(String filePath) {
        try {
            return ImageIO.read(new File(assetsPath + filePath));
        } catch (IOException ex) {
            throw new RuntimeException(filePath, ex);
        }
    }

    public String getCardImageFilePath(CardModel cardModel) {
        String imagePath = cardModel.getTemplate().getImagePath();
        if (imagePath != null) {
            return "images/cards/" + imagePath;
        }
        return "images/cards/other.png";
    }
}
