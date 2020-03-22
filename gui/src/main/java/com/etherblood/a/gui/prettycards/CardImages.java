package com.etherblood.a.gui.prettycards;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Texture;
import java.awt.Image;
import java.util.HashMap;
import jme3tools.converters.ImageToAwt;

/**
 *
 * @author Carl
 */
public class CardImages {

    private final AssetManager assets;
    private static final HashMap<String, Image> IMAGE_CACHE = new HashMap<>();

    public CardImages(AssetManager assets) {
        this.assets = assets;
    }

    public Image getCachedImage(String resourcePath) {
        return getCachedImage(resourcePath, -1, -1);
    }

    public Image getCachedImage(String filePath, int width, int height) {
        String key = (filePath + "_" + width + "_" + height);
        Image image = IMAGE_CACHE.get(key);
        if (image == null) {
            Texture texture = assets.loadTexture(new TextureKey(filePath, false));
            image = ImageToAwt.convert(texture.getImage(), false, true, 0);
            if ((width != -1) && (height != -1)) {
                int scaleMode = (filePath.endsWith(".gif") ? Image.SCALE_FAST : Image.SCALE_SMOOTH);
                image = image.getScaledInstance(width, height, scaleMode);
            }
            IMAGE_CACHE.put(key, image);
        }
        return image;
    }

    public String getCardImageFilePath(CardModel cardModel) {
        String imagePath = cardModel.getTemplate().getImagePath();
        if (imagePath != null) {
            return imagePath;
        }
        return "images/cards/other.png";
    }

    public String getCardImageFilePath(MinionModel cardModel) {
        String imagePath = cardModel.getTemplate().getImagePath();
        if (imagePath != null) {
            return imagePath;
        }
        return "images/cards/other.png";
    }
}
